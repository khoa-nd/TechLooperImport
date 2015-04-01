package com.techlooper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.entity.UserImportEntity;
import com.techlooper.pojo.GithubAwardResponse;
import com.techlooper.pojo.SocialProvider;
import com.techlooper.repository.UserImportRepository;
import com.techlooper.utils.GithubAwardRequest;
import com.techlooper.utils.ImportIOConnection;
import com.techlooper.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
@Service
public class GithubAwardUserService {

  private static Logger LOGGER = LoggerFactory.getLogger(GithubAwardUserService.class);

  private static final int TOTAL_USER_PER_PAGE = 50;

  @Resource
  private ElasticsearchTemplate elasticsearchTemplate;

  @Resource
  private UserImportRepository userImportRepository;

  @Resource
  private GithubAwardDataProcessor githubAwardDataProcessor;

  @Value("${githubAwardUserImporter.search.connectorId}")
  private String searchUserConnectorId;

  @Value("${githubAwardUserImporter.search.userId}")
  private String searchUserUserId;

  @Value("${githubAwardUserImporter.search.apiKey}")
  private String searchUserApiKey;

  @Value("${githubAwardUserImporter.search.url}")
  private String searchUserApiUrlTemplate;

  @Value("${es.index}")
  private String esIndex;

  @Value("${githubUserProfileEnricher.githubProfile}")
  private String userProfileConnectorId;

  @Value("${githubUserProfileEnricher.userId}")
  private String userProfileUserId;

  @Value("${githubUserProfileEnricher.apiKey}")
  private String userProfileApiKey;

  @Value("${githubUserProfileEnricher.queryUrlTemplate}")
  private String userProfileApiUrlTemplate;

  @Value("${techlooper.api.addAllUsers}")
  private String techlooperAddAllUserUrl;

  private static List<String> refineUserProfile = Arrays.asList("organizations", "popular_repos", "contributed_repos");

  public List<UserImportEntity> getUserByCountry(String country, int pageIndex) {
    SearchQuery searchQuery = new NativeSearchQueryBuilder()
      .withQuery(nestedQuery("profiles", QueryBuilders.matchQuery("profiles.GITHUB.location", country)))
      .withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE))
      .build();

    Page<UserImportEntity> result = elasticsearchTemplate.queryForPage(searchQuery, UserImportEntity.class);
    return result.getContent();
  }

  public int getTotalPageOfUserByCountry(String country) {
    SearchQuery searchQuery = new NativeSearchQueryBuilder()
      .withQuery(nestedQuery("profiles", QueryBuilders.matchQuery("profiles.GITHUB.location", country)))
      .withPageable(new PageRequest(0, TOTAL_USER_PER_PAGE))
      .build();

    Page<UserImportEntity> result = elasticsearchTemplate.queryForPage(searchQuery, UserImportEntity.class);
    return result.getTotalPages();
  }

  public int enrichUserImportByCountry(String country, GithubAwardRequest extractor) throws Exception {
    int totalPage = getTotalPageOfUserByCountry(country);
    int pageIndex = 0;
    int countSuccess = 0;
    while (pageIndex < totalPage) {
      List<UserImportEntity> userImportEntities = getUserByCountry(country, pageIndex);
      for (UserImportEntity userImportEntity : userImportEntities) {
        Map<String, Object> profile = (Map<String, Object>) userImportEntity.getProfiles().get(SocialProvider.GITHUB);

        if (profile == null) {
          continue;
        }

        String githubUsername = (String) profile.get("username");
        extractor.setInputUrl(String.format(extractor.getInputUrl(), githubUsername));
        try {
          GithubAwardResponse githubAwardResponse = ImportIOConnection.fetchContent(extractor);

          if (StringUtils.isNotEmpty(githubAwardResponse.getError())) {
            LOGGER.info("Enrich User " + githubUsername + " Fail Due To ImportID. " + githubAwardResponse.getError());
            continue;
          }
          else if (githubAwardResponse.getResults().isEmpty()) {
            LOGGER.info("User " + githubUsername + " Doesn't Have Information.");
            continue;
          }

          githubAwardDataProcessor.process(userImportEntity, githubAwardResponse);
          userImportRepository.save(userImportEntity);
          countSuccess++;
          LOGGER.info("User " + githubUsername + " Has Been Enriched Successfully.");
        }
        catch (Exception ex) {
          LOGGER.error("Enrich User " + githubUsername + " Fail", ex);
        }
        finally {
          Thread.sleep(2000);
        }
      }
      pageIndex++;
    }
    return countSuccess;
  }

  public void crawlNewUser(String country) {
    int pageNumber = 1;
    final boolean[] contCrawl = {true};
    while (contCrawl[0]) {
      String queryUrl = String.format(searchUserApiUrlTemplate, country, pageNumber++);
      LOGGER.debug("Do import.io by query {}", queryUrl);
      Utils.doIIOQuery(searchUserConnectorId, searchUserUserId, searchUserApiKey, queryUrl, users -> {
        contCrawl[0] = users.size() > 0;
        if (contCrawl[0]) {
          crawlNewUser(users);
        }
      });
    }
    LOGGER.debug("Stop crawling at page {}", pageNumber);
  }

  private void crawlNewUser(JsonNode users) {
    final ArrayNode usersJsonArray = JsonNodeFactory.instance.arrayNode();
    users.forEach(user -> {
      String username = user.get("username/_text").asText();
      SearchQuery searchQuery = new NativeSearchQueryBuilder().withSearchType(SearchType.COUNT)
        .withTypes("user").withIndices(esIndex)
        .withFilter(FilterBuilders.nestedFilter("profiles", FilterBuilders.termFilter("profiles.GITHUB.username", username)))
        .build();
      if (elasticsearchTemplate.count(searchQuery) == 0) {
        LOGGER.debug("New user {} detected", username);
        crawlNewProfile(username, usersJsonArray);
      }
    });
    try {
      int status = Utils.postAndGetStatus(techlooperAddAllUserUrl, usersJsonArray);
      LOGGER.debug("Posted to {}, status = {}", techlooperAddAllUserUrl, status);
    }
    catch (IOException e) {
      LOGGER.error("Failed post to {}", techlooperAddAllUserUrl);
      String failedPath = String.format("gaw-techlooper.users.%s.fail.json",
        DateTimeFormat.forPattern("yyyyMMdd-HHmmss").print(System.currentTimeMillis()));
      try {
        FileUtils.write(new java.io.File(failedPath), usersJsonArray.toString(), StandardCharsets.UTF_8, true);
      }
      catch (IOException ex) {
        LOGGER.error("Can not save json {}", usersJsonArray);
      }
    }
  }

  private void crawlNewProfile(String username, ArrayNode usersJsonArray) {
    String queryUrl = String.format(userProfileApiUrlTemplate, username);
    Utils.doIIOQuery(userProfileConnectorId, userProfileUserId, userProfileApiKey, queryUrl, users -> {
      if (users.size() == 1) {
        JsonNode user = users.get(0);
        refineUserProfile.forEach(fieldName -> {
          JsonNode field = user.at("/" + fieldName);
          if (field.isTextual()) {
            LOGGER.debug("Refine field {}...", fieldName);
            ((ObjectNode) user).putArray(fieldName).add(field.asText());
            LOGGER.debug("...done refine field {}", fieldName);
          }
        });
        ((ObjectNode) user).put("username", username);
        usersJsonArray.add(user);
        LOGGER.debug("Accepted user {}", username);
      }
      else {
        LOGGER.debug("Not real user {}", username);
      }
    });
  }
}
