package com.techlooper.imports;

import com.techlooper.utils.PropertyManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by phuonghqh on 1/30/15.
 */
public class GitHubUserProfileEnricher {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserProfileEnricher.class);

  public static void main(String[] args) throws IOException {
    String inputFile = PropertyManager.properties.getProperty("githubUserProfileEnricher.inputFile");

    Files.lines(Paths.get(inputFile), StandardCharsets.UTF_8).parallel().forEach(username -> {
      try {
        enrichUserProfile(username);
      }
      catch (Exception e) {
        LOGGER.error("ERROR", e);
      }
    });
  }

  private static void enrichUserProfile(String username) throws UnsupportedEncodingException {
    String enrichUserApi = PropertyManager.properties.getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");
    String userId = PropertyManager.properties.getProperty("githubUserProfileEnricher.userId");
    String apiKey = PropertyManager.properties.getProperty("githubUserProfileEnricher.apiKey");
    String connectorId = PropertyManager.properties.getProperty("githubUserProfileEnricher.githubProfile");
    String queryUrlTemplate = PropertyManager.properties.getProperty("githubUserProfileEnricher.queryUrlTemplate");

    boolean tryAgain = true;

    while (tryAgain) {
      HttpClient httpClient = HttpClients.createDefault();
      HttpPost post = new HttpPost(String.format("https://api.import.io/store/data/%s/_query?_user=%s&_apikey=%s",
        connectorId, userId, URLEncoder.encode(apiKey, "UTF-8")));

      String queryUrl = String.format(queryUrlTemplate, username);
      String json = String.format("{ \"input\": {\"webpage/url\": \"%s\"} }", queryUrl);
      post.setEntity(new StringEntity(json, ContentType.create("application/json", "UTF-8")));

      try {
        HttpResponse response = httpClient.execute(post);
        String jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
      }
      catch (Exception err) {
        LOGGER.error("ERROR", err);
        continue;
      }

      tryAgain = false;
    }
  }
}
