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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

  private static String enrichUserApi = PropertyManager.getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");

  public static void main(String[] args) throws IOException {
    Utils.sureDirectory(outputDirectory);

    FootPrint footPrint = Utils.readFootPrint(footPrintFilePath);
    queryES(footPrint);

    LOGGER.debug("DONE DONE DONE!!!!!");
  }

  private static void queryES(FootPrint footPrint) {
    Client client = Utils.esClient();

    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(PropertyManager.properties.getProperty("githubUserProfileEnricher.es.index"));
    SearchResponse response = searchRequestBuilder.setSearchType(SearchType.COUNT).execute().actionGet();

    long totalUsers = response.getHits().getTotalHits();
    long maxPageNumber = (totalUsers % 100 == 0) ? totalUsers / 100 : totalUsers / 100 + 1;

    ExecutorService executorService = Executors.newFixedThreadPool(20);
    for (int pageNumber = footPrint.getLastPageNumber(); pageNumber < maxPageNumber; pageNumber++) {
      try {
        doQuery(pageNumber, executorService);
        Utils.writeFootPrint(String.format("%sgithub.footprint.json", outputDirectory),
          FootPrint.FootPrintBuilder.footPrint().withLastPageNumber(pageNumber).build());
      }
      catch (Exception e) {
        LOGGER.error("ERROR", e);
      }
    }
    executorService.shutdown();

    client.close();
  }

  private static void doQuery(int pageNumber, ExecutorService executorService) throws IOException, InterruptedException {
    LOGGER.debug("New query ES page created {}", pageNumber);
    String filename = String.format("%sgithub.post.p.%d.json", outputDirectory, pageNumber);
    File jsonFile = new File(filename);
    ArrayNode jsonUsers = jsonFile.exists() ? (ArrayNode) Utils.readJson(jsonFile) : crawlUsersProfile(pageNumber, executorService, filename);

    executorService.execute(() -> {
      LOGGER.debug(">>>>Start posting to api<<<<");
      try {
        if (Utils.postJsonString(enrichUserApi, jsonUsers.toString()) != 204) {
          LOGGER.error("Error when posting json to api. >_<");
        }
        else {
          Files.move(Paths.get(filename), Paths.get(filename + ".ok"));
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
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(PropertyManager.properties.getProperty("githubUserProfileEnricher.es.index"));

    SearchResponse response = searchRequestBuilder.addField("profiles.GITHUB.username")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setFrom(pageNumber * 100).setSize(100).execute().actionGet();

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
      catch (InterruptedException e) {
        LOGGER.error("ERROR", e);
      }
      catch (ExecutionException e) {
        LOGGER.error("ERROR", e);
      }
    });

    Utils.writeToFile(jsonUsers, filename);

    return jsonUsers;
  }

  private static JsonNode enrichUserProfile(String username) {
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
            LOGGER.debug("Refine json ...");
            ((ObjectNode) root).putArray(fieldName).add(field.asText());
          }
        });
//        ((ObjectNode) root).put("username", username);
        profileNode[0] = root;
      }
    });
    return profileNode[0];
  }

//  private static class PostApiJob implements Runnable {
//
//    private JsonNode arrayNode;
//
//    public PostApiJob(JsonNode arrayNode) {
//      this.arrayNode = arrayNode;
//    }
//
//    public void run() {
//      LOGGER.debug(">>>>Start posting to api<<<<");
//      try {
//        if (Utils.postJsonString(enrichUserApi, arrayNode.toString()) != 204) {
//          LOGGER.error("Error when posting json {} to api {}", arrayNode, enrichUserApi);
//          System.exit(1);
//        }
//      }
//      catch (Exception e) {
//        LOGGER.error("ERROR", e);
//      }
//      LOGGER.debug(">>>>Done posting to api<<<<");
//    }
//  }
}
