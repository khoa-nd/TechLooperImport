package com.techlooper.enricher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techlooper.es.ElasticSearch;
import com.techlooper.manager.RetryManager;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 2/9/15.
 */
public class AboutMeEnricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AboutMeEnricher.class);

  private static String configJsonPath = PropertyManager.getProperty("enricher.config");

  private static int fixedThreadPool = Integer.parseInt(PropertyManager.getProperty("fixedThreadPool"));

  private static String folder = PropertyManager.getProperty("folder");

  private static String failListPath;

  private static JsonNode config;

  private static ElasticSearch elasticSearch;

  private static ExecutorService executorService;

  private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withLocale(Locale.US);

  private static RetryManager retryManager;

  private static String techlooperFolder;

  private static String apiFolder;

  public static void main(String[] args) throws IOException {
    try {
      Utils.sureFolder(folder);

      config = Utils.parseJson(new File(configJsonPath));
      elasticSearch = new ElasticSearch(config, LOGGER);
      retryManager = new RetryManager(config, LOGGER);
      executorService = Executors.newFixedThreadPool(fixedThreadPool);

      failListPath = String.format("%s%s.txt", folder, dateTimeFormatter.print(DateTime.now()));
      Utils.sureFile(failListPath);

      techlooperFolder = folder + config.at("/techlooper/folderName").asText();
      apiFolder = folder + config.at("/api/folderName").asText();
      Utils.sureFolder(techlooperFolder, apiFolder);

      enrichProfile();
    }
    catch (Exception e) {
      LOGGER.error("ERROR", e);
      Utils.writeToFile(config, configJsonPath);
    }
    executorService.shutdown();
  }

  public static void enrichProfile() throws UnirestException, IOException {
    fromRetryManager();
    fromElasticSearch();
  }

  private static void fromRetryManager() throws IOException {
    final int[] index = {0};
    retryManager.retry(jsonNode -> postTechlooper(null, jsonNode),
      queryUrl -> consumeApiResponse(postApi(queryUrl), (index[0]++) + ".failLine"));
  }

  private static void fromElasticSearch() throws UnirestException, IOException {
    elasticSearch.queryUserInfo((users) -> prepareApiQuery(users, "/fields/profiles.GITHUB.email", "email"));
//    elasticSearch.queryUserInfo((users) -> executorService.execute(() -> prepareApiQuery(users, "/fields/profiles.GITHUB.email", "email")));
  }

  private static void prepareApiQuery(JsonNode users, String userPath, String apiTerm) {
    StringBuilder builder = new StringBuilder();
    users.forEach((user) -> {
      JsonNode field = user.at(userPath).get(0);
      Optional.ofNullable(field).ifPresent((f) -> {
        String text = field.asText();
        builder.append(",").append(text);
      });
    });
    builder.deleteCharAt(0);

    consumeApiResponse(doApiQuery(apiTerm, builder.toString()), config.at("/userQuery/from").asInt() + ".es");
  }

  public static void consumeApiResponse(JsonNode response, String source) {
    Optional.ofNullable(response)
      .ifPresent(resp -> Optional.ofNullable(consumeApiSucceedUser(resp, source))
        .ifPresent(notFoundUsers -> consumeApiFailUser(notFoundUsers, source)));
  }

  private static void consumeApiFailUser(JsonNode users, String source) {
    String filePath = String.format("%s%s.json", apiFolder, source);
    try {
      Utils.writeToFile(users, filePath);
      LOGGER.debug("Consume fail users => write to file", filePath);
    }
    catch (IOException e) {
      LOGGER.error("ERROR", e);
    }
  }

  private static JsonNode consumeApiSucceedUser(JsonNode response, String source) {
    JsonNode found = response.at("/result/found");
    if (found.at("/total_count").asInt() > 0) {
      found.at("/profiles").forEach(AboutMeEnricher::refineEnrichUser);
      postTechlooper(source, found.at("/profiles"));
    }

    JsonNode notFound = response.at("/result/not_found");
    if (notFound.at("/total_count").asInt() > 0) {
      return notFound;
    }
    return null;
  }

  private static void postTechlooper(String source, JsonNode users) {
    String url = config.at("/techlooper/apiUrl").asText();
    String jsonPath = String.format("%s%s.json", techlooperFolder, source);
    try {
      Unirest.post(url).body(users.toString()).asString().getStatus();
      if (source != null) {
        Utils.writeToFile(users, String.format("%s.ok", jsonPath));
      }
    }
    catch (Exception e) {
      LOGGER.error("Not post to {}, error: {}", url, e);
      try {
        Utils.writeToFile(users, jsonPath);
      }
      catch (IOException ex) {
        LOGGER.error("ERROR", ex);
      }
    }
  }

  private static void refineEnrichUser(JsonNode user) {
    LOGGER.debug("Refine user before post to techlooper...");
    ObjectNode wrUser = (ObjectNode) user;
    config.at("/techlooper/mapper").forEach((mapper) -> {
      String from = mapper.get("from").asText();
      String to = mapper.get("to").asText();
      wrUser.put(to, wrUser.get(from).asText());
      wrUser.remove(from);
    });
    wrUser.put("crawlersource", "ABOUTME");
    LOGGER.debug("...done refine user");
  }

  public static JsonNode doApiQuery(String field, String value) {
    String queryUrl = String.format(config.at("/api/userSearchUrl").asText(), field + "=" + value);
    return postApi(queryUrl);
  }

  private static JsonNode postApi(String queryUrl) {
    try {
      LOGGER.debug("Post about.me api by url {}", queryUrl);
      JsonNode response = Utils.parseJson(Unirest.post(queryUrl).asString().getBody().toString());
      if (response.at("/status").asInt() != HttpServletResponse.SC_OK) {
        LOGGER.debug("The query {} is not success", queryUrl);
        ((ObjectNode) config).put("failListPath", failListPath);
        Files.write(Paths.get(failListPath), Arrays.asList(queryUrl), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        return null;
      }
      return response;
    }
    catch (Exception e) {
      LOGGER.debug("Error post to api {}", queryUrl);
    }
    return null;
  }
}
