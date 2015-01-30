package com.techlooper.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class Utils {

  private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

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
