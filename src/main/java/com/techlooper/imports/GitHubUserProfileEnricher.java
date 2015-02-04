package com.techlooper.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.pojo.FootPrint;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by phuonghqh on 1/30/15.
 */
public class GitHubUserProfileEnricher {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserProfileEnricher.class);

  private static List<String> refineImportIOFields = Arrays.asList("organizations", "popular_repos", "contributed_repos");

  private static String outputDirectory = PropertyManager.getProperty("githubUserProfileEnricher.outputDirectory");

  private static String footPrintFilePath = String.format("%sgithub.footprint.json", outputDirectory);

  private static String userId = PropertyManager.getProperty("githubUserProfileEnricher.userId");

  private static String apiKey = PropertyManager.getProperty("githubUserProfileEnricher.apiKey");

  private static String connectorId = PropertyManager.getProperty("githubUserProfileEnricher.githubProfile");

  private static String queryUrlTemplate = PropertyManager.getProperty("githubUserProfileEnricher.queryUrlTemplate");

  public static void main(String[] args) throws IOException {
    Utils.sureDirectory(outputDirectory);

    FootPrint footPrint = Utils.readFootPrint(footPrintFilePath);
    queryES(footPrint);

    LOGGER.debug("DONE DONE DONE!!!!!");
  }

  private static void queryES(FootPrint footPrint) throws IOException {
    Client client = Utils.esClient();

    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(PropertyManager.properties.getProperty("githubUserProfileEnricher.es.index"));
    SearchResponse response = searchRequestBuilder.setSearchType(SearchType.COUNT).execute().actionGet();

    long totalUsers = response.getHits().getTotalHits();
    long maxPageNumber = (totalUsers % 100 == 0) ? totalUsers / 100 : totalUsers / 100 + 1;

    for (int pageNumber = footPrint.getLastPageNumber(); pageNumber < maxPageNumber; pageNumber++) {
      doQuery(pageNumber);

      Utils.writeFootPrint(String.format("%sgithub.footprint.json", outputDirectory),
        FootPrint.FootPrintBuilder.footPrint().withLastPageNumber(pageNumber).build());
    }

    client.close();
  }

  private static void doQuery(int pageNumber) throws IOException {
    LOGGER.debug("New query ES page created {}", pageNumber);
    Client client = Utils.esClient();
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(PropertyManager.properties.getProperty("githubUserProfileEnricher.es.index"));

    SearchResponse response = searchRequestBuilder.addField("profiles.GITHUB.username")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setFrom(pageNumber * 100).setSize(100).execute().actionGet();
    List<String> failedUsernames = new ArrayList<>();

    List<String> usernames = new ArrayList<>();
    response.getHits().forEach(hit -> usernames.add(hit.field("profiles.GITHUB.username").getValue()));
    ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
    usernames.parallelStream().forEach(username -> {
      try {
        JsonNode profile = enrichUserProfile(username);
        Optional.ofNullable(profile).ifPresent(arrayNode::add);
        if (profile == null) {
          failedUsernames.add(username);
        }
      }
      catch (IOException e) {
        LOGGER.error("ERROR", e);
        failedUsernames.add(username);
      }
    });

    LOGGER.debug(">>>>Start posting to api<<<<");
    String enrichUserApi = PropertyManager.properties.getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");
    if (Utils.postJsonString(enrichUserApi, arrayNode.toString()) != 204) {
      LOGGER.error("Error when posting json {} to api {}", arrayNode, enrichUserApi);
    }

    Utils.writeToFile(arrayNode, String.format("%sgithub.post.p.%d.json", outputDirectory, pageNumber));
    if (failedUsernames.size() > 0) {
      Utils.writeToFile(failedUsernames, String.format("%sgithub.failed.p.%d.json", outputDirectory, pageNumber));
    }

    client.close();
  }

  private static JsonNode enrichUserProfile(String username) throws IOException {
    String queryUrl = String.format(queryUrlTemplate, username);
    final JsonNode[] profileNode = {null};
    LOGGER.debug("Do import.io query {}", queryUrl);
    Utils.doIIOQuery(connectorId, userId, apiKey, queryUrl, node -> {
      LOGGER.debug("Result from query {} is {}", queryUrl, node);
      if (node.size() == 1) {
        JsonNode root = node.get(0);
        refineImportIOFields.forEach(fieldName -> {
          JsonNode field = root.at("/" + fieldName);
          if (field.isTextual()) {
            LOGGER.debug("Refine json {} ...", root.toString());
            ((ObjectNode) root).putArray(fieldName).add(field.asText());
            LOGGER.debug("...to {}", root.toString());
          }
        });
//        ((ObjectNode) root).put("username", username);
        profileNode[0] = root;
      }
    });
    return profileNode[0];
  }
}
