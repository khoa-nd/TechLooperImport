package com.techlooper.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by phuonghqh on 2/10/15.
 */
public class RetryManager {

  private Logger logger;

  private JsonNode config;

  private static String folder = PropertyManager.getProperty("folder");

  public RetryManager(JsonNode config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  public void retry(Consumer<JsonNode> unsuceedTechlooperConsumer, Consumer<String> failListConsumer) {
    Optional.ofNullable(unsuceedTechlooperConsumer).ifPresent(consumer -> {
      logger.debug("Retry from unsuceedTechlooper");
      try {
        fromFile(".json").parallel().forEach(path -> {
          try {
            consumer.accept(Utils.parseJson(new File(path.toString())));
          }
          catch (IOException e) {
            logger.error("ERROR", e);
          }
        });
      }
      catch (IOException e) {
        logger.error("ERROR", e);
      }
    });

    Optional.ofNullable(failListConsumer).ifPresent(consumer -> {
      logger.debug("Retry from fail list");
      try {
        fromFailList(consumer);
      }
      catch (IOException e) {
        logger.error("ERROR", e);
      }
    });
  }

  public Stream<Path> fromFile(String ext) throws IOException {
    return Files.find(Paths.get(folder), 1, (path, attrs) -> {
      if (attrs.isRegularFile()) {
        return path.toString().endsWith(ext);
      }
      return false;
    });
  }

  public void fromFailList(Consumer<String> consumer) throws IOException {
    String failListPath = config.at("/failListPath").asText();
    if (new File(failListPath).length() == 0) {
      logger.debug("Empty fail list at {}", failListPath);
      return;
    }

    logger.debug("Recover from file {}", failListPath);
    Path target = Paths.get(failListPath + ".done");
    Files.move(Paths.get(failListPath), target);
    Files.lines(target, StandardCharsets.UTF_8).forEach(consumer::accept);

    logger.debug("Done file {}", target);
    ((ObjectNode) config).remove("failListPath");
  }
}
