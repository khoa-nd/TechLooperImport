package com.techlooper.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techlooper.pojo.FootPrint;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class Utils {

  private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  private static String iioFailsPath = PropertyManager.getProperty("iio.fails");

  public static Optional<String> emptyStringOptional(String str) {
    return Optional.ofNullable(str).filter( s -> !s.isEmpty());
  }

  public static JsonNode parseJson(File file) throws IOException {
    return new ObjectMapper().readTree(file);
  }

  public static FootPrint readFootPrint(String filePath) {
    LOGGER.debug("Read foot-print at {}", filePath);
    File file = new File(filePath);
    if (file.exists()) {
      try {
        return new ObjectMapper().readValue(file, FootPrint.class);
      }
      catch (Exception e) {
        LOGGER.error("ERROR", e);
      }
    }
    return FootPrint.FootPrintBuilder.footPrint().build();
  }

  public static void writeFootPrint(String filePath, FootPrint footPrint) throws IOException {
    LOGGER.debug("Save foot-print to {}", filePath);
    new ObjectMapper().writeValue(new File(filePath), footPrint);
  }

  public static void doIIOQuery(String connectorId, String userId, String apiKey, String queryUrl,
                                Consumer<JsonNode> consumer) {
    try {
      boolean tryAgain = true;
      while (tryAgain) {
        LOGGER.debug("Query using url: " + queryUrl);
        String content = postIIOAndReadContent(connectorId, userId, apiKey, queryUrl);
        JsonNode root = readIIOResult(content);
        if (!root.isArray()) {
          LOGGER.debug("Error result => query: {}", queryUrl);
          String errorText = Optional.ofNullable(parseJson(content).at("/error").asText()).orElse("");
          if (errorText.contains("HTTP 404")) {
            LOGGER.error("HTTP 404 => Break loop query {}", queryUrl);
            break;
          }
          LOGGER.error("I/O Error getting page. => Try again query {}", queryUrl);
          continue;
        }
        tryAgain = false;

        if (root.size() == 0) {
          LOGGER.debug("Empty result, query {}", queryUrl);
        }

        LOGGER.debug("OK => Consuming query {}...", queryUrl);
        consumer.accept(root);
      }
    }
    catch (Exception e) {
      try {
        File iioFailFile = new File(iioFailsPath);
        if (iioFailFile.exists()) {
          Files.write(Paths.get(iioFailsPath), Arrays.asList(queryUrl), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
        else {
          File parentFile = iioFailFile.getParentFile();
          if (!parentFile.exists()) {
            parentFile.mkdirs();
          }
          Files.write(Paths.get(iioFailsPath), Arrays.asList(queryUrl), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
      }
      catch (IOException ex) {
        LOGGER.error("Can not write to fail file {}", iioFailsPath);
      }
      LOGGER.error("Can not do crawler {}", queryUrl, e);
    }
  }

  public static String postIIOAndReadContent(String connectorId, String userId, String apiKey, String queryUrl) throws UnirestException, UnsupportedEncodingException {
    String url = String.format("https://api.import.io/store/data/%s/_query?_user=%s&_apikey=%s",
      connectorId, userId, URLEncoder.encode(apiKey, "UTF-8"));
    LOGGER.debug("Request IIO by query url {}", queryUrl);
    queryUrl = JsonNodeFactory.instance.objectNode()
      .set("input", JsonNodeFactory.instance.objectNode()
      .put("webpage/url", queryUrl)).toString();
    return Unirest.post(url).body(new com.mashape.unirest.http.JsonNode(queryUrl)).asString().getBody();
  }

  public static void sureFolder(String... dirPaths) {
    Arrays.stream(dirPaths).forEach(path -> {
      File dir = new File(path);
      if (!dir.exists()) {
        dir.mkdirs();
      }
    });
  }

  public static void writeToFile(JsonNode root, String filePath) throws IOException {
    new ObjectMapper().writeValue(new java.io.File(filePath), root);
  }

  public static JsonNode readIIOResult(String iioContent) throws IOException {
    return parseJson(iioContent).at("/results");
  }

  public static JsonNode parseJson(String json) throws IOException {
    return new ObjectMapper().readTree(json);
  }

  public static Client esClient() {
    Settings settings = ImmutableSettings.settingsBuilder()
      .put("cluster.name", PropertyManager.properties.getProperty("es.userimport.cluster.name")).build();

    TransportClient client = new TransportClient(settings);
    String host = PropertyManager.properties.getProperty("es.userimport.host");
    client.addTransportAddress(new InetSocketTransportAddress(host.split(":")[0], Integer.valueOf(host.split(":")[1])));
    return client;
  }

  public static int postAndGetStatus(String url, String json) throws IOException, UnirestException {
    int rsp = Unirest.post(url).body(json).asString().getStatus();
    LOGGER.debug("Response code of url {} ", rsp);
    return rsp;
  }


  public static int postAndGetStatus(String url, JsonNode jsonNode) throws IOException, UnirestException {
    return postAndGetStatus(url, jsonNode.toString());
  }

  public static void sureFile(String failListPath) throws IOException {
    File file = new File(failListPath);
    if (file.exists()) {
      return;
    }

    File parent = new File(file.getParent());
    if (!parent.exists()) {
      parent.mkdirs();
    }

    file.createNewFile();
  }
}