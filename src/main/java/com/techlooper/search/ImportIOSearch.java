package com.techlooper.search;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

/**
 * Created by phuonghqh on 2/11/15.
 */
public class ImportIOSearch {

  private JsonNode config;

  private Logger logger;

  public ImportIOSearch(JsonNode config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  public JsonNode doCrawler(String configPath) {
    logger.debug("Do crawler using configPath {}", configPath);
    JsonNode iio = config.at(configPath);

    return null;
  }

}
