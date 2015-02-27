package com.techlooper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by phuonghqh on 2/9/15.
 */
public class ElasticSearchService {

  private JsonNode config;

  private Logger logger;

  public ElasticSearchService(JsonNode config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  public void queryUserInfo(Consumer<JsonNode> userConsumer) throws UnirestException, IOException {
    int totalUsers = getTotalUsers();
    JsonNode query = config.at("/userQuery");
    int size = query.at("/query/size").asInt();
    int from;

    do {
      from = query.at("/query/from").asInt();
      logger.debug("Query users by url {}, from {}", query.at("/url").asText(), from);
      JsonNode response = Utils.parseJson(Unirest.post(query.at("/url").asText())
        .basicAuth(query.at("/username").asText(), query.at("/password").asText())
        .body(query.at("/query").toString())
        .asString().getBody());

      JsonNode users = response.at("/hits/hits");
      if (users.size() == 0) {
        ((ObjectNode) query.at("/query")).put("from", from - size);
        logger.debug("Last page reach at from {}", query.at("/query/from").asInt());
        break;
      }

      logger.debug("Accept users size: {}", users.size());
      userConsumer.accept(users);

      from = query.at("/query/from").asInt() + size;
      ((ObjectNode) query.at("/query")).put("from", from);
    }
    while (from <= totalUsers);
    logger.debug("Done query from ElasticSearch");
  }

  private int getTotalUsers() throws IOException, UnirestException {
    JsonNode query = config.at("/userCountQuery");
    JsonNode response = Utils.parseJson(Unirest.post(query.at("/url").asText())
      .basicAuth(query.at("/username").asText(), query.at("/password").asText())
      .asString().getBody());
    int totalUsers = response.at("/hits/total").asInt();
    logger.debug("Total user is {}", totalUsers);
    return totalUsers;
  }
}

