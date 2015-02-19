package com.techlooper.utils;

import com.techlooper.exception.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by phuonghqh on 1/21/15.
 */
public class PropertyManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertyManager.class);

  public static Properties properties = new Properties();

  static {
    try {
      URL res = PropertyManager.class.getClassLoader().getResource("application.properties");
      properties.load(res.openStream());
      LOGGER.debug("Loaded properties at path: {}", res.getPath());
    }
    catch (Exception e) {
      LOGGER.error("Failed to load properties file", e);
      throw new InitializationException(e.getMessage());
    }
    overrideProperties();
  }

  private static void overrideProperties() {
    try {
      overrideProperties(PropertyManager.class.getClassLoader().getResource("override.properties").openStream());
    }
    catch (Exception e) {
      LOGGER.debug("Failed to override properties file");
    }
  }

  public static void overrideProperties(InputStream inputStream) {
    try {
      properties.load(inputStream);
    }
    catch (Exception e) {
      LOGGER.debug("Failed to override properties file");
    }
  }

  public static String getProperty(String key) {
    String value = properties.getProperty(key);
    LOGGER.debug("Use property key: {} = {} ", key, value);
    return value;
  }
}
