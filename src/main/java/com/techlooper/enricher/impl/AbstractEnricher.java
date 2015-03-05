package com.techlooper.enricher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.techlooper.enricher.Enricher;
import com.techlooper.utils.Utils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * Created by phuonghqh on 2/11/15.
 */
public abstract class AbstractEnricher implements Enricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEnricher.class);

  protected String configPath;

  protected String folder;

  protected String ioFolder;

  protected String failListPath;

  protected JsonNode config;

  protected JsonNode appConfig;

  protected ExecutorService executorService;

  protected DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withLocale(Locale.US);

  protected String techlooperFolder;

  protected String apiFolder;

  public void postTechlooper(String source, JsonNode users) {
    if (users.size() == 0) {
      LOGGER.debug("No user => not post to techlooper");
      return;
    }
    String url = config.at("/techlooper/apiUrl").asText();
    String jsonPath = String.format("%s.%s.json", techlooperFolder, source);
    int statusCode = 0;
    try {
      if (new File(source).exists()) {
        jsonPath = source;
      }

      statusCode = Utils.postAndGetStatus(url, users);// Unirest.post(url).header("Content-Type", "application/json").body(users.toString()).asString().getStatus();
      if (statusCode == HttpServletResponse.SC_NO_CONTENT) {
        if (new File(jsonPath).exists()) {
          Files.move(Paths.get(jsonPath), Paths.get(jsonPath + ".ok"));
        }
        else {
          Utils.writeToFile(users, String.format("%s.ok", jsonPath));
        }
      }
      else {
//        jsonPath.lastIndexOf(".")
        Utils.writeToFile(users, String.format("%s.%d.json", jsonPath.substring(0, FilenameUtils.indexOfExtension(jsonPath)), statusCode));
      }
    }
    catch (Exception e) {
      LOGGER.error("Not post to url = {}, error= {}", url, e);
      try {
        Utils.writeToFile(users, String.format("%s.%d.json", jsonPath.substring(0, FilenameUtils.indexOfExtension(jsonPath)), statusCode));
//        Utils.writeToFile(users, String.format(jsonPath, statusCode));
      }
      catch (IOException ex) {
        LOGGER.error("ERROR", ex);
      }
    }
  }

  public void initialize(ExecutorService executorService, String configPath, JsonNode appConfig) throws IOException {
    LOGGER.debug("Initialize AboutMeEnricher with config {}", configPath);
    try {
      this.executorService = executorService;
      this.appConfig = appConfig;
      this.config = Utils.parseJson(new File(configPath));
      this.configPath = configPath;

      prepareProperties();
    }
    catch (Exception e) {
      LOGGER.error("ERROR", e);
      Utils.writeToFile(config, this.configPath);
    }
  }

  private void prepareProperties() {
    folder = this.appConfig.get("folder").asText();
    ioFolder = folder + config.get("folderName").asText();
    failListPath = String.format("%s%s.txt", ioFolder, dateTimeFormatter.print(DateTime.now()));

    techlooperFolder = ioFolder + config.at("/techlooper/folderName").asText();
    apiFolder = ioFolder + config.at("/api/folderName").asText();
    Utils.sureFolder(techlooperFolder, apiFolder);
  }

  public String getTechlooperFolder() {
    return techlooperFolder;
  }

  public JsonNode getConfig() {
    return config;
  }

//  public static void main(String[] arg) {
//    String filename = "./abc.json";
//    System.out.println(filename.substring(0, FilenameUtils.indexOfExtension(filename)));
//  }
}
