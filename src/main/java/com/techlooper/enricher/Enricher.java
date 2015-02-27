package com.techlooper.enricher;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Created by phuonghqh on 2/11/15.
 */
public interface Enricher {

    void initialize(ExecutorService executorService, String configPath, JsonNode appConfig) throws IOException;

    void consumeElasticSearchUsers(JsonNode users);

    JsonNode getConfig();

    String getTechlooperFolder();

    void postTechlooper(String source, JsonNode jsonNode);

    void retryFailApi(Integer lineIndex, String queryUrl);
}
