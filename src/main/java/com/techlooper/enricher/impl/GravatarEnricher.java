package com.techlooper.enricher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techlooper.service.GravatarService;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by phuonghqh on 2/27/15.
 */
public class GravatarEnricher extends AbstractEnricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GravatarEnricher.class);

  private GravatarService gravatarService = new GravatarService();

  public void consumeElasticSearchUsers(JsonNode users) {
    LOGGER.debug("Consume elastic search users");
    gravatarService.setConfig(config);
    ArrayNode techlooperUsers = JsonNodeFactory.instance.arrayNode();
    users.forEach(user -> Optional.ofNullable(refineUser(user)).ifPresent(techlooperUsers::add));
    postTechlooper(config.at("/userQuery/query/from").asInt() + ".es", techlooperUsers);
  }

  private JsonNode refineUser(JsonNode user) {
    String email = user.get("_id").asText();
    final JsonNode[] gravatarUser = {null};
    Optional.ofNullable(gravatarService.findProfile(email)).ifPresent(jsonNode -> {
      try {
        if (jsonNode.isObject()) {
          LOGGER.debug("Refine json from Gravatar, email {} ...", email);
          ObjectNode wrUser = (ObjectNode) jsonNode;
          if (jsonNode.get("entry").get(0).at("/name").isArray()) {
            ((ObjectNode) jsonNode.get("entry").get(0)).remove("name");
          }
          wrUser.put("email", email);
          wrUser.put("crawlersource", "GRAVATAR");
          gravatarUser[0] = jsonNode;
        }
        else {
          LOGGER.debug(jsonNode.toString() + ": " +  email);
        }
      }
      catch (Exception e) {
        LOGGER.error("Could not refine json", e);
      }
    });
    return gravatarUser[0];
  }

  public void retryFailApi(Integer lineIndex, String queryUrl) {

  }
}
