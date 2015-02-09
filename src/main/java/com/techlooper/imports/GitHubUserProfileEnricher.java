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
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.NestedFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 1/30/15.
 */
public class GitHubUserProfileEnricher {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserProfileEnricher.class);

  private static List<String> refineImportIOFields = Arrays.asList("organizations", "popular_repos", "contributed_repos");

  private static String outputDirectory = PropertyManager.getProperty("githubUserProfileEnricher.outputDirectory");

  private static String footPrintFilePath = PropertyManager.getProperty("footPrintFile");//String.format("%sgithub.footprint.json", outputDirectory);

  private static String userId = PropertyManager.getProperty("githubUserProfileEnricher.userId");

  private static String apiKey = PropertyManager.getProperty("githubUserProfileEnricher.apiKey");

  private static String connectorId = PropertyManager.getProperty("githubUserProfileEnricher.githubProfile");

  private static String queryUrlTemplate = PropertyManager.getProperty("githubUserProfileEnricher.queryUrlTemplate");

  private static String enrichUserApi = PropertyManager.getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");

  private static String esIndex = PropertyManager.getProperty("githubUserProfileEnricher.es.index");

  private static int fixedThreadPool = Integer.parseInt(PropertyManager.getProperty("fixedThreadPool"));

  private static int pageSize = Integer.parseInt(PropertyManager.getProperty("pageSize"));

  private static boolean retry = Boolean.parseBoolean(PropertyManager.getProperty("retry"));

  private static boolean filterUsersHasntDescription = Boolean.parseBoolean(PropertyManager.getProperty("githubUserProfileEnricher.filterUsersHasntDescription"));

  private static String iioFailsPath = PropertyManager.getProperty("iio.fails");

  private static FilterBuilder userHasntDescriptionFilterBuilder = FilterBuilders.nestedFilter("profiles",
    FilterBuilders.notFilter(FilterBuilders.existsFilter("description"))).cache(true);

  public static void main(String[] args) throws IOException {
    Utils.sureDirectory(outputDirectory);
    ExecutorService executorService = Executors.newFixedThreadPool(fixedThreadPool);
    if (retry) {
      doRetry(executorService);
    }
    else {
      FootPrint footPrint = Utils.readFootPrint(footPrintFilePath);
      queryES(footPrint, executorService);
    }
    executorService.shutdown();

    LOGGER.debug("DONE DONE DONE!!!!!");
  }

  private static void queryES(FootPrint footPrint, ExecutorService executorService) throws IOException {
    Client client = Utils.esClient();
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(esIndex).setSearchType(SearchType.COUNT);

    if (filterUsersHasntDescription) {
      searchRequestBuilder.setPostFilter(userHasntDescriptionFilterBuilder);
    }

    SearchResponse response = searchRequestBuilder.setSearchType(SearchType.COUNT).execute().actionGet();

    long totalUsers = response.getHits().getTotalHits();
    long maxPageNumber = (totalUsers % pageSize == 0) ? totalUsers / pageSize : totalUsers / pageSize + 1;

    int lastPageNumber = footPrint.getLastPageNumber();
    for (int pageNumber = lastPageNumber; pageNumber < maxPageNumber; pageNumber++) {
      try {
        doQuery(pageNumber, executorService);
        Utils.writeFootPrint(footPrintFilePath, FootPrint.FootPrintBuilder.footPrint().withLastPageNumber(pageNumber).build());
      }
      catch (Exception e) {
        LOGGER.error("ERROR", e);
      }
    }

    client.close();
  }

  private static void doRetry(ExecutorService executorService) throws IOException {
    doJsonFiles(executorService);
    doIIOFailsFiles(executorService);
  }

  private static void doIIOFailsFiles(ExecutorService executorService) throws IOException {
    ArrayNode jsonUsers = JsonNodeFactory.instance.arrayNode();
    List<String> queries = Files.readAllLines(Paths.get(iioFailsPath));
    List<String> successQueries = new ArrayList<>();
    queries.forEach(queryUrl -> {
      LOGGER.debug("Retry query {}", queryUrl);
      String username = queryUrl.substring(queryUrl.lastIndexOf("/") + 1, queryUrl.length());
      JsonNode usProfile = enrichUserProfile(username);
      jsonUsers.add(usProfile);
      successQueries.add(queryUrl);
    });
    Files.write(Paths.get(iioFailsPath + ".ok"), successQueries, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

    Path postFile = Paths.get(iioFailsPath + ".json");
    Files.write(postFile, Arrays.asList(jsonUsers.toString()), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    postUsers2API(executorService, postFile.toString(), jsonUsers);
  }

  private static void doJsonFiles(ExecutorService executorService) throws IOException {
    Files.walk(Paths.get(outputDirectory), FileVisitOption.FOLLOW_LINKS).filter(path -> path.toString().endsWith(".json"))
      .map(filePath -> new File(filePath.toString())).forEach(jsonFile -> {
      if (Files.isRegularFile(Paths.get(jsonFile.getPath()))) {
        try {
          asyncPostFile(executorService, jsonFile);
        }
        catch (Exception e) {
          LOGGER.error("Error", e);
        }
      }
    });
  }

  private static void asyncPostFile(ExecutorService executorService, File jsonFile) throws IOException {
    String jsonFilePath = jsonFile.getPath();
    LOGGER.debug("Posting file {}", jsonFilePath);
    ArrayNode jsonUsers = (ArrayNode) Utils.readJson(jsonFile);
    postUsers2API(executorService, jsonFilePath, jsonUsers);
  }

  private static void doQuery(int pageNumber, ExecutorService executorService) throws IOException, InterruptedException {
    LOGGER.debug("New query ES page created {}", pageNumber);
    String filename = String.format("%sgithub.post.p.%d.json", outputDirectory, pageNumber);
    if (new File(filename + ".ok").exists()) {
      LOGGER.debug("Done done done!!");
      return;
    }

    File jsonFile = new File(filename);
    ArrayNode jsonUsers = jsonFile.exists() ? (ArrayNode) Utils.readJson(jsonFile) : crawlUsersProfile(pageNumber, executorService, filename);
    if (jsonUsers.size() == 0) {
      return;
    }

    postUsers2API(executorService, filename, jsonUsers);
  }

  private static void postUsers2API(ExecutorService executorService, String filename, ArrayNode jsonUsers) {
    if (jsonUsers.size() == 0) {
      return;
    }
    executorService.execute(() -> {
      try {
        LOGGER.debug(">>>>Start posting to api<<<<");
        int respCode = Utils.postJsonString(enrichUserApi, jsonUsers.toString());
        if (respCode == HttpServletResponse.SC_NO_CONTENT) {
          Files.move(Paths.get(filename), Paths.get(String.format("%s.ok", filename)));
        }
        else {
          LOGGER.error("Error {} when posting users to api. >_<", respCode);
        }
      }
      catch (Exception e) {
        LOGGER.error("ERROR", e);
      }
      LOGGER.debug(">>>>Done posting to api<<<<");
    });
  }

  private static ArrayNode crawlUsersProfile(int pageNumber, ExecutorService executorService, String filename) throws InterruptedException, IOException {
    Client client = Utils.esClient();
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(esIndex).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setFrom(pageNumber * pageSize).setSize(pageSize).addField("profiles.GITHUB.username");
    if (filterUsersHasntDescription) {
      searchRequestBuilder.setPostFilter(userHasntDescriptionFilterBuilder);
    }

    SearchResponse response = searchRequestBuilder.execute().actionGet();

    List<String> usernames = new ArrayList<>();
    response.getHits().forEach(hit -> usernames.add(hit.field("profiles.GITHUB.username").getValue()));

    List<Callable<JsonNode>> jobs = new ArrayList<>();
    usernames.forEach(username -> jobs.add(() -> enrichUserProfile(username)));
    client.close();

    ArrayNode jsonUsers = JsonNodeFactory.instance.arrayNode();
    executorService.invokeAll(jobs).forEach(future -> {
      try {
        JsonNode profile = future.get();
        Optional.ofNullable(profile).ifPresent(jsonUsers::add);
      }
      catch (Exception e) {
        LOGGER.error("ERROR", e);
      }
    });

    if (jsonUsers.size() > 0) {
      Utils.writeToFile(jsonUsers, filename);
    }

    return jsonUsers;
  }

  private static JsonNode enrichUserProfile(String username) {
    String queryUrl = String.format(queryUrlTemplate, username);
    final JsonNode[] profileNode = {null};
    LOGGER.debug("Do import.io query {}", queryUrl);
    Utils.doIIOQuery(connectorId, userId, apiKey, queryUrl, node -> {
      if (node.size() == 1) {
        JsonNode root = node.get(0);
        refineImportIOFields.forEach(fieldName -> {
          JsonNode field = root.at("/" + fieldName);
          if (field.isTextual()) {
            LOGGER.debug("Refine field [{}]...", fieldName);
            ((ObjectNode) root).putArray(fieldName).add(field.asText());
            LOGGER.debug("...done refine field [{}]", fieldName);
          }
        });
        ((ObjectNode) root).put("username", username);
        profileNode[0] = root;
      }
    });
    return profileNode[0];
  }
}
