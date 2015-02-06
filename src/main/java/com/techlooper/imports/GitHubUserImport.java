package com.techlooper.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class GitHubUserImport {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserImport.class);

  private static String inputDirectory = PropertyManager.getProperty("githubUserImport.inputDirectory");

  private static String addUserApi = PropertyManager.getProperty("githubUserImport.techlooper.api.addUser");

  private static int fixedThreadPool = Integer.parseInt(PropertyManager.getProperty("fixedThreadPool"));

  private static String esIndex = PropertyManager.getProperty("githubUserImport.es.index");

  public static void main(String[] args) throws IOException {
    ExecutorService executorService = Executors.newFixedThreadPool(fixedThreadPool);
    Files.walk(Paths.get(inputDirectory), FileVisitOption.FOLLOW_LINKS).filter(path -> path.toString().endsWith(".json")).parallel().forEach(filePath -> {
      if (Files.isRegularFile(filePath)) {
        sendFile(filePath, executorService);
      }
    });
    executorService.shutdown();
    LOGGER.debug("DONE DONE DONE!!!!!");
  }

  private static void sendFile(Path filePath, ExecutorService executorService) {
    LOGGER.debug("Reading file {}", filePath);
    try {
      StringBuilder builder = new StringBuilder();
      Files.readAllLines(filePath, StandardCharsets.UTF_8).forEach(builder::append);
      JsonNode users = Utils.readJson(builder.toString());
      users.forEach(user -> {
        ObjectNode node = (ObjectNode) user;
        node.put("crawlersource", "GITHUB");
        node.put("imageurl", node.get("image_url").asText());
        node.put("fullname", node.get("image_url/_alt").asText());

        node.remove("image_url");
        node.remove("image_url/_alt");
        node.remove("datejoin");
      });

      executorService.execute(() -> {
        try {
          LOGGER.debug(">>>>Start posting to api<<<<");
          int rspCode = Utils.postJsonString(addUserApi, users.toString());
          if (rspCode == HttpServletResponse.SC_NO_CONTENT) {
            Files.move(filePath, Paths.get(filePath.toString() + ".ok"));
          }
          else if (rspCode == HttpServletResponse.SC_NOT_ACCEPTABLE) {//no update
            Files.move(filePath, Paths.get(filePath.toString() + ".ignore"));
          }
          else {
            LOGGER.error("Error calling to api, error code: {} (>_<)", rspCode);
          }
        }
        catch (IOException e) {
          LOGGER.error("Error", e);
        }
        LOGGER.debug(">>>>Done posting to api<<<<");
      });
    }
    catch (Exception e) {
      LOGGER.error("Error when proccessing file", e);
    }
  }

  private static boolean exist(String username) {
    Client client = Utils.esClient();

//    QUery

    SearchResponse response = client.prepareSearch(esIndex).setSearchType(SearchType.COUNT)
                                .execute().actionGet();


    client.close();

    return false;
  }
}
