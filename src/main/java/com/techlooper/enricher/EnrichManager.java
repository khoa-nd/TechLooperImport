package com.techlooper.enricher;

import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techlooper.service.ElasticSearchService;
import com.techlooper.service.RetryService;
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

    private static JsonNode appConfig;

    private static ExecutorService executorService;

    public static void main(String[] args) {
        try {
            appConfig = Utils.parseJson(new File(appConfigPath));
            fixedThreadPool = appConfig.get("fixedThreadPool").asInt();

            folder = appConfig.get("folder").asText();
            Utils.sureFolder(folder);

            executorService = Executors.newFixedThreadPool(fixedThreadPool);

            appConfig.get("enrichers").forEach(enricher -> {
                JsonNode eConfig = null;
                String clz = enricher.get("class").asText();
                String eConfigPath = enricher.get("configPath").asText();
                try {
                    LOGGER.debug("Make instance of {} with configPath = {}", clz, eConfigPath);
                    Enricher eInstance = (Enricher) Class.forName(clz).newInstance();
                    eInstance.initialize(executorService, eConfigPath, appConfig);

                    eConfig = eInstance.getConfig();
                    retryFromLastPrint(eInstance, eConfig);
                    enrichUser(eInstance, eConfig);
                } catch (Exception e) {
                    LOGGER.error("ERROR", e);
                }

                if (eConfig != null) {
                    LOGGER.debug("Rewrite configuration at path = {}", eConfigPath);
                    try {
                        Utils.writeToFile(eConfig, eConfigPath);
                    } catch (IOException e) {
                        LOGGER.error("Error", e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("ERROR", e);
        }
        executorService.shutdown();
    }

    private static void enrichUser(Enricher eInstance, JsonNode eConfig) throws IOException, UnirestException {
        LOGGER.debug("Enrich users from Elastic Search >>");
        ElasticSearchService elasticSearch = new ElasticSearchService(eConfig, LOGGER);
        elasticSearch.queryUserInfo((users) -> eInstance.consumeESUsers(users));
    }

    private static void retryFromLastPrint(Enricher eInstance, JsonNode eConfig) throws IOException {
        LOGGER.debug("Retry from last print >>");
        RetryService retryManager = new RetryService(eConfig, LOGGER);
        retryManager.fromFile(eInstance.getTechlooperFolder(), ".json", (path, jsonNode) -> eInstance.postTechlooper(path.toString(), jsonNode));
        retryManager.fromFailList((index, queryUrl) -> eInstance.retryFailApi(index, queryUrl));
    }
}
