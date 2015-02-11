package com.techlooper.enricher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by phuonghqh on 2/11/15.
 */
public class GitHubEnricher extends AbstractEnricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GitHubEnricher.class);

  public void consumeESUsers(JsonNode users) {
    LOGGER.debug("Consume elastic search users");

  }

  public void retryFailApi(Integer lineIndex, String queryUrl) {

  }
}
