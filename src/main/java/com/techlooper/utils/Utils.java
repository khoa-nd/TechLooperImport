package com.techlooper.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import java.util.List;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class Utils {

  private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  public static JsonNode onlyOneNode(JsonNode root) {
    ArrayNode arrayNode = (ArrayNode) root;
    if (arrayNode.size() == 1) {
      return arrayNode.get(0);
    }
    return null;
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

  public static void writeToFile(List<?> list, String filenameTemplate, Object... params) throws IOException {
    if (list.size() > 0) {
      final StringBuilder builder = new StringBuilder();
      list.forEach(usn -> builder.append(",").append("\"").append(usn).append("\""));
      writeToFile(builder.deleteCharAt(0).insert(0, "[").append("]").toString(),
        String.format(filenameTemplate, params));
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

  public static String getImportIOResult(String content) {
    int resultIndex = content.indexOf("\"results\":[");
    if (resultIndex < 0) {
      LOGGER.debug("No result");
      return null;
    }
    LOGGER.debug("Extracting result from {}", content);

    int endResultIndex = content.indexOf("],\"cookies\"", resultIndex);
    String json = content.substring(resultIndex + "\"results\":[".length() - 1, endResultIndex + 1);
    if ("[]".equals(json)) {
      LOGGER.debug("Result is empty from {}", content);
      return null;
    }
    return json;
  }

  public static void writeToFile(String content, String filepath) throws IOException {
    BufferedWriter writer = Files.newBufferedWriter(Paths.get(filepath), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    writer.write(content);
    writer.close();
  }
}
