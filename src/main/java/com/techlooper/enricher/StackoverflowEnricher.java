package com.techlooper.enricher;

import com.techlooper.utils.PropertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by phuonghqh on 2/11/15.
 */
public class StackoverflowEnricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackoverflowEnricher.class);

    private static String configJsonPath = PropertyManager.getProperty("stackoverflow.config");

    public static void main(String[] args) {

    }
}
