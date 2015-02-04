package com.techlooper.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class GitHubUserImport {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserImport.class);

  private static String inputDirectory = PropertyManager.properties.getProperty("githubUserImport.inputDirectory");

  private static String addUserApi = PropertyManager.properties.getProperty("githubUserImport.techlooper.api.addUser");

  public static void main(String[] args) throws IOException {
    LOGGER.info("Configuration info: \n  - InputDirectory: {}\n  - AddUserApi: {}", inputDirectory, addUserApi);

    Files.walk(Paths.get(inputDirectory), FileVisitOption.FOLLOW_LINKS).parallel().forEach(filePath -> {
      if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".json")) {
        LOGGER.debug("Reading file {}", filePath);
        try {
          StringBuilder builder = new StringBuilder();
          Files.readAllLines(filePath, StandardCharsets.UTF_8).forEach(builder::append);

          JsonNode root = Utils.readJson(builder.toString());
          root.forEach(n -> {
            ObjectNode node = (ObjectNode) n;
            node.put("crawlersource", "GITHUB");
            node.put("imageurl", node.get("image_url").asText());
            node.put("fullname", node.get("image_url/_alt").asText());

            node.remove("image_url");
            node.remove("image_url/_alt");
          });

          LOGGER.debug("Posting json: {} to Api", root);

          String json = root.toString();
          int code = Utils.postJsonString(addUserApi, json);
          if (code == 204) {
            Files.move(filePath, Paths.get(filePath.toString() + ".ok"));
            LOGGER.debug("Successful import json: {} to Url: {}", json, addUserApi);
          }
          else {
            LOGGER.error("Error import json: {} to Url: {}", json, addUserApi);
          }
        }
        catch (IOException e) {
          LOGGER.error("Error when reading file", e);
        }
      }
    });

    LOGGER.debug("DONE DONE DONE!!!!!");
  }
}
