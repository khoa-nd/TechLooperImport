package com.techlooper.enricher;

import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techlooper.es.ElasticSearch;
import com.techlooper.manager.RetryManager;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 2/11/15.
 */
public class EnrichManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichManager.class);

  private static String appConfigPath = PropertyManager.getProperty("applicationConfigPath");

  private static int fixedThreadPool = 0;

  private static String folder;

  private static String techlooperFolder;

  private static JsonNode appConfig;

  private static ExecutorService executorService;

  public static void main(String[] args) throws IOException {
    try {
      appConfig = Utils.parseJson(new File(appConfigPath));
      fixedThreadPool = appConfig.get("fixedThreadPool").asInt();

      folder = appConfig.get("folder").asText();
      techlooperFolder = folder + appConfig.at("/techlooper/folderName").asText();
      Utils.sureFolder(folder, techlooperFolder);

      executorService = Executors.newFixedThreadPool(fixedThreadPool);

      appConfig.get("enrichers").forEach(enricher -> {
        try {
          String clz = enricher.get("class").asText();
          String configPath = enricher.get("configPath").asText();
          LOGGER.debug("Make instance of {} with config {}", clz, configPath);
          Enricher eInstance = (Enricher) Class.forName(clz).newInstance();
          eInstance.initialize(executorService, configPath, appConfig);
          doEnrich(eInstance, eInstance.getConfig());
        }
        catch (Exception e) {
          LOGGER.error("ERROR", e);
        }
      });
    }
    catch (Exception e) {
      LOGGER.error("ERROR", e);
    }
    executorService.shutdown();
  }

  private static void doEnrich(Enricher eInstance, JsonNode eConfig) throws IOException, UnirestException {
    RetryManager retryManager = new RetryManager(eConfig, LOGGER);
    retryManager.fromFile(techlooperFolder, ".json", (path, jsonNode) -> eInstance.postTechlooper(path.toString(), jsonNode));
    retryManager.fromFailList((index, queryUrl) -> eInstance.retryFailApi(index, queryUrl));

    ElasticSearch elasticSearch = new ElasticSearch(eConfig, LOGGER);
    elasticSearch.queryUserInfo((users) -> eInstance.consumeESUsers(users));
  }
}
