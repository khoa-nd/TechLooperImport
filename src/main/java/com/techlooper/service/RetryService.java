package com.techlooper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

/**
 * Created by phuonghqh on 2/10/15.
 */
public class RetryService {

    private Logger logger;

    private JsonNode config;

    public RetryService(JsonNode config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void fromFile(String folder, String ext, BiConsumer<Path, JsonNode> consumer) throws IOException {
        logger.debug("Recover from file ext: {}", ext);
        Files.find(Paths.get(folder), 1, (path, attrs) -> {
            if (attrs.isRegularFile()) {
                return path.toString().endsWith(ext);
            }
            return false;
        }).forEach(path -> {
            try {
                consumer.accept(path, Utils.parseJson(new File(path.toString())));
            } catch (IOException e) {
                logger.error("ERROR", e);
            }
        });
    }

    public void fromFailList(BiConsumer<Integer, String> consumer) throws IOException {
        String failListPath = config.at("/failListPath").asText();
        if (new File(failListPath).length() == 0) {
            logger.debug("Empty fail list at {}", failListPath);
            return;
        }

        String doneFilePath = failListPath + ".done";
        if (new File(doneFilePath).exists()) {
            logger.debug("Already done at {}", doneFilePath);
            return;
        }

        logger.debug("Recover from file {}", failListPath);
        final Integer[] index = {0};
        Files.lines(Paths.get(failListPath), StandardCharsets.UTF_8).forEach(queryUrl -> consumer.accept(index[0]++, queryUrl));

        logger.debug("Done file {}", failListPath);
        ((ObjectNode) config).remove("failListPath");
        Files.move(Paths.get(failListPath), Paths.get(doneFilePath));
    }
}
