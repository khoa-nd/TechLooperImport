package com.techlooper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;

/**
 * Created by phuonghqh on 2/11/15.
 */
public class ImportIOService {

  private JsonNode config;

  private Logger logger;

  public ImportIOService(JsonNode config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  public JsonNode doCrawler(String configPath) {
    logger.debug("Do crawler using configPath {}", configPath);
    JsonNode iio = config.at(configPath);
//    Utils.doIIOQuery(iio.get());

    return null;
  }

}
