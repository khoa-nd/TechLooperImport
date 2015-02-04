package com.techlooper.utils;

import com.techlooper.exception.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Properties;

/**
 * Created by phuonghqh on 1/21/15.
 */
public class PropertyManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertyManager.class);

  public static Properties properties = new Properties();

  static  {
    try {
      URL res = PropertyManager.class.getClassLoader().getResource("application.properties");
      properties.load(res.openStream());
      System.out.println(String.format("Loaded properties at path: %s", res.getPath()));
    }
    catch (Exception e) {
      System.err.println("Failed to load properties file");
      e.printStackTrace(System.err);
      throw new InitializationException(e.getMessage());
    }
  }

  public static String getProperty(String key) {
    String value = properties.getProperty(key);
    LOGGER.debug("Use property key: {} = {} ", key, value);
    return value;
  }
}
