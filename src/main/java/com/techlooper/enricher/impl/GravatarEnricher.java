package com.techlooper.enricher.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.techlooper.pojo.GravatarModel;
import com.techlooper.service.GravatarService;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by phuonghqh on 2/27/15.
 */
public class GravatarEnricher extends AbstractEnricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AboutMeEnricher.class);

  private GravatarService gravatarService = new GravatarService();

  public void consumeElasticSearchUsers(JsonNode users) {
    LOGGER.debug("Consume elastic search users");
    users.forEach(user -> {
      String email = user.get("_id").asText();
      GravatarModel model = gravatarService.findGravatarProfile(email);
      try {
        postTechlooper(config.at("/userQuery/query/from").asInt() + ".es", Utils.model2JsonNode(model));
      }
      catch (Exception e) {
        LOGGER.error("Could not post");
      }
    });
  }

  public void retryFailApi(Integer lineIndex, String queryUrl) {

  }
}
