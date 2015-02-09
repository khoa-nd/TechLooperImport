package com.techlooper.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.techlooper.pojo.FootPrint;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class Utils {

  private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  private static String iioFailsPath = PropertyManager.getProperty("iio.fails");

  public static JsonNode readJson(File file) throws IOException {
    return new ObjectMapper().readTree(file);
  }

//  public static FootPrint[] loadFootPrints(String filePath) {
//    File file = new File(filePath);
//    if (file.exists()) {
//      try {
//        return new ObjectMapper().readValue(file, FootPrint[].class);
//      }
//      catch (Exception e) {
//        LOGGER.error("ERROR", e);
//      }
//    }
//    return new FootPrint[]{};
//  }

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
        String content = Utils.postIIOAndReadContent(connectorId, userId, apiKey, queryUrl);
        JsonNode root = Utils.readIIOResult(content);
        if (!root.isArray()) {
          LOGGER.debug("Error result => query: {}", queryUrl);
          String errorText = Optional.ofNullable(Utils.readJson(content).at("/error").asText()).orElse("");
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

  public static String postIIOAndReadContent(String connectorId, String userId, String apiKey, String queryUrl) throws UnsupportedEncodingException {
    return postAndReadContent(String.format("https://api.import.io/store/data/%s/_query?_user=%s&_apikey=%s",
      connectorId, userId, URLEncoder.encode(apiKey, "UTF-8")), Utils.toIOQueryUrl(queryUrl));
  }

  public static String postAndReadContent(String url, String json) {
    HttpClient httpClient = HttpClients.createDefault();
    HttpPost post = new HttpPost(url);
    post.setEntity(new StringEntity(json, ContentType.create("application/json", "UTF-8")));
    String content = null;
    try {
      HttpResponse response = httpClient.execute(post);
      content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }
    catch (Exception err) {
      LOGGER.error("ERROR", err);
    }
    return content;
  }

  public static void sureDirectory(String dirPath) {
    File dir = new File(dirPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  public static void writeToFile(JsonNode root, String filePath) throws IOException {
    new ObjectMapper().writeValue(new java.io.File(filePath), root);
  }

  public static JsonNode readIIOResult(String iioContent) throws IOException {
    return readJson(iioContent).at("/results");
  }

  public static String toIOQueryUrl(String queryUrl) {
    return JsonNodeFactory.instance.objectNode().set("input",
      JsonNodeFactory.instance.objectNode().put("webpage/url", queryUrl)).toString();
  }

  public static JsonNode readJson(String json) throws IOException {
    return new ObjectMapper().readTree(json);
  }

  public static void writeToFile(List<?> list, String filepath) throws IOException {
    if (list.size() > 0) {
      final StringBuilder builder = new StringBuilder();
      list.forEach(usn -> builder.append(",").append("\"").append(usn).append("\""));
      writeToFile(builder.deleteCharAt(0).insert(0, "[").append("]").toString(), filepath);
    }
  }

  public static Client esClient() {
    Settings settings = ImmutableSettings.settingsBuilder()
      .put("cluster.name", PropertyManager.properties.getProperty("es.userimport.cluster.name")).build();

    TransportClient client = new TransportClient(settings);
    String host = PropertyManager.properties.getProperty("es.userimport.host");
    client.addTransportAddress(new InetSocketTransportAddress(host.split(":")[0], Integer.valueOf(host.split(":")[1])));
    return client;
  }

  public static int postJsonString(String url, String jsonString) throws IOException {
    HttpClient httpClient = HttpClients.createDefault();
    HttpPost post = new HttpPost(url);
    post.setEntity(new StringEntity(jsonString, ContentType.create("application/json", StandardCharsets.UTF_8)));
    HttpResponse response = httpClient.execute(post);
    return response.getStatusLine().getStatusCode();
  }

  public static void writeToFile(String content, String filepath) throws IOException {
    BufferedWriter writer = Files.newBufferedWriter(Paths.get(filepath), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    writer.write(content);
    writer.close();
  }
  //    Files.lines(Paths.get(inputFile), StandardCharsets.UTF_8).parallel().forEach(username -> {
//      try {
//        enrichUserProfile(username);
//      }
//      catch (Exception e) {
//        LOGGER.error("ERROR:", e);
//      }
//    });
}
