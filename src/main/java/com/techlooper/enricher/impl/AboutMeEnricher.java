package com.techlooper.enricher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by phuonghqh on 2/9/15.
 */
public class AboutMeEnricher extends AbstractEnricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AboutMeEnricher.class);

    public static final int STATUS_OK = 200;

    private int apiErrorTimes = 0;

    private void doApiQuery(JsonNode users, String userPath, String apiTerm) {
        StringBuilder builder = new StringBuilder();
        users.forEach((user) -> {
            JsonNode field = user.at(userPath).get(0);
            Optional.ofNullable(field).ifPresent(f -> {
                String text = field.asText();
                builder.append(",").append(text);
            });
        });
        builder.deleteCharAt(0);

        consumeApiResponse(doApiQuery(apiTerm, builder.toString()), config.at("/userQuery/query/from").asInt() + ".es");
    }

    private void consumeApiResponse(JsonNode response, String source) {
        Optional.ofNullable(response)
                .ifPresent(resp -> Optional.ofNullable(consumeApiSucceedUser(resp, source))
                        .ifPresent(notFoundUsers -> consumeApiFailUser(notFoundUsers, source)));
    }

    private void consumeApiFailUser(JsonNode users, String source) {
        String filePath = String.format("%s%s.json", apiFolder, source);
        try {
            Utils.writeToFile(users, filePath);
            LOGGER.warn("Consume fail users => write to file = {}", filePath);
        } catch (IOException e) {
            LOGGER.error("ERROR", e);
        }
    }

    private JsonNode consumeApiSucceedUser(JsonNode response, String source) {
        JsonNode found = response.at("/result/found");
        if (found.at("/total_count").asInt() > 0) {
            found.at("/profiles").forEach(this::refineEnrichUser);
            postTechlooper(source, found.at("/profiles"));
        } else {
            LOGGER.warn("No user found on AboutMe with source = {}", source);
        }

        JsonNode notFound = response.at("/result/not_found");
        if (notFound.at("/total_count").asInt() > 0) {
            return notFound;
        }
        return null;
    }

    public void retryFailApi(Integer lineIndex, String queryUrl) {
        String failListName = Paths.get(config.get("failListPath").asText()).getFileName().toString();
        consumeApiResponse(postApi(queryUrl), String.format("%d.%s", lineIndex, failListName));
    }

    private void refineEnrichUser(JsonNode user) {
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

    public JsonNode doApiQuery(String field, String value) {
        String queryUrl = String.format(config.at("/api/userSearchUrl").asText(), field + "=" + value);
        return postApi(queryUrl);
    }

    private JsonNode postApi(String queryUrl) {
        try {
            JsonNode response = null;
            boolean cont = true;
            while (cont) {
                LOGGER.debug("Post about.me api by url = {}", queryUrl);
                response = Utils.parseJson(Unirest.post(queryUrl).asString().getBody().toString());
                if (response.at("/status").asInt() != STATUS_OK) {
                    LOGGER.error("The query is not success {}", queryUrl);
                    ((ObjectNode) config).put("failListPath", failListPath);
                    Utils.sureFile(failListPath);
                    Files.write(Paths.get(failListPath), Arrays.asList(queryUrl), StandardCharsets.UTF_8, StandardOpenOption.APPEND);

                    if (apiErrorTimes++ > 20) {
                        LOGGER.warn("Post to api and get error {} times => Should stop here..", apiErrorTimes);
//          throw new ShouldHaltException("Should stop posting to API here...");
                    }
                } else {
                    cont = false;
                }
            }
            return response;
        } catch (IOException e) {
            LOGGER.error("Error post to api , queryUrl = {}", queryUrl, e);
        } catch (UnirestException e) {
            LOGGER.error("Error post to api , queryUrl = {}", queryUrl, e);
        }
        return null;
    }

    public void consumeESUsers(JsonNode users) {
        LOGGER.debug("Consume elastic search users");
        doApiQuery(users, "/fields/profiles.GITHUB.email", "email");
    }

}
