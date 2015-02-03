package com.techlooper.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by phuonghqh on 1/30/15.
 */
public class GitHubUserProfileEnricher {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserProfileEnricher.class);

  private static List<String> refineImportIOFields = Arrays.asList("organizations", "popular_repos", "contributed_repos");

  public static void main(String[] args) throws IOException {
//    Files.lines(Paths.get(inputFile), StandardCharsets.UTF_8).parallel().forEach(username -> {
//      try {
//        enrichUserProfile(username);
//      }
//      catch (Exception e) {
//        LOGGER.error("ERROR:", e);
//      }
//    });
    String outputDirectory = PropertyManager.properties.getProperty("githubUserProfileEnricher.outputDirectory");
    Utils.sureDirectory(outputDirectory);
    queryES();
  }

  private static void queryES() throws IOException {
    Client client = Utils.esClient();

    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(PropertyManager.properties.getProperty("githubUserProfileEnricher.es.index"));
    SearchResponse response = searchRequestBuilder.setSearchType(SearchType.COUNT).execute().actionGet();

    long totalUsers = response.getHits().getTotalHits();
    long maxPageNumber = (totalUsers % 100 == 0) ? totalUsers / 100 : totalUsers / 100 + 1;

    for (int pageNumber = 0; pageNumber < maxPageNumber; pageNumber++) {
      doQuery(pageNumber);
    }

    client.close();
  }

  private static void doQuery(int pageNumber) {
    LOGGER.debug("New query ES page created {}", pageNumber);
    Client client = Utils.esClient();
    String outputDirectory = PropertyManager.properties.getProperty("githubUserProfileEnricher.outputDirectory");
    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(PropertyManager.properties.getProperty("githubUserProfileEnricher.es.index"));

    SearchResponse response = searchRequestBuilder.addField("profiles.GITHUB.username")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setFrom(pageNumber * 100).setSize(100).execute().actionGet();
    List<String> failedUsernames = new ArrayList<>();
    List<String> successUsernames = new ArrayList<>();

    List<String> usernames = new ArrayList<>();
    response.getHits().forEach(hit -> usernames.add(hit.field("profiles.GITHUB.username").getValue()));
    usernames.parallelStream().forEach(username -> {
      try {
        String failedUsername = enrichUserProfile(username);
        if (failedUsername != null) {
          failedUsernames.add(failedUsername);
        }
        else {
          successUsernames.add(username);
        }
      }
      catch (IOException e) {
        LOGGER.error("ERROR", e);
        failedUsernames.add(username);
      }
    });
    try {
      Utils.writeToFile(successUsernames, "%sgithub.success.p.%d.json", outputDirectory, pageNumber);
      Utils.writeToFile(failedUsernames, "%sgithub.failed.p.%d.json", outputDirectory, pageNumber);
    }
    catch (Exception e) {
      LOGGER.error("Error write to files", e);
    }
    client.close();
  }

  private static String enrichUserProfile(String username) throws IOException {
    String enrichUserApi = PropertyManager.properties.getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");
    String userId = PropertyManager.properties.getProperty("githubUserProfileEnricher.userId");
    String apiKey = PropertyManager.properties.getProperty("githubUserProfileEnricher.apiKey");
    String connectorId = PropertyManager.properties.getProperty("githubUserProfileEnricher.githubProfile");
    String queryUrlTemplate = PropertyManager.properties.getProperty("githubUserProfileEnricher.queryUrlTemplate");
    String outputDirectory = PropertyManager.properties.getProperty("githubUserProfileEnricher.outputDirectory");

    boolean tryAgain = true;
    while (tryAgain) {
      String queryUrl = String.format(queryUrlTemplate, username);
      String content = Utils.postIIOAndReadContent(connectorId, userId,  apiKey, queryUrl);
      if (content == null) {
        continue;
      }

      JsonNode root = Utils.readIIOResult(content);
      if (!root.isArray()) {
        LOGGER.debug("Error result => query: {}", queryUrl);
        continue;
      }
      tryAgain = false;

      if (root.size() == 0) {
        LOGGER.debug("Empty result => query: {}", queryUrl);
        continue;
      }
      root = root.get(0);

      ObjectNode writableRoot = (ObjectNode) root;
      for (String field : refineImportIOFields) {
        JsonNode node = root.at("/" + field);
        if (!node.isArray()) {
          LOGGER.debug("Refine json {} ...", root.toString());
          String text = node.asText();
          ArrayNode jsonNodes = writableRoot.putArray(field);
          if (node.isTextual()) {
            jsonNodes.add(text);
          }
          LOGGER.debug("...to {}", root.toString());
        }
      }
      writableRoot.put("username", username);
      Utils.writeToFile(root.toString(), String.format("%sgithub.%s.post.json", outputDirectory, username));
      LOGGER.debug("OK => Post user \"{}\" to api ", username);
      ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode().add(root);
      if (Utils.postJsonString(enrichUserApi, arrayNode.toString()) != 204) {
        LOGGER.error("Error when posting json {} to api {}", arrayNode, enrichUserApi);
        return username;
      }
    }
    return null;
  }
}
