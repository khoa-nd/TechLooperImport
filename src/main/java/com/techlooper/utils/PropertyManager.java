package com.techlooper.utils;

import com.techlooper.exception.InitializationException;

import java.net.URL;
import java.util.Properties;

/**
 * Created by phuonghqh on 1/21/15.
 */
public class PropertyManager {

  public static Properties properties = new Properties();

  static  {
    try {
      URL res = PropertyManager.class.getClassLoader().getResource("application.properties");
      properties.load(res.openStream());
      System.out.println(String.format("Loaded properties at path: {}", res.getPath()));
    }
    catch (Exception e) {
      System.err.println("Failed to load properties file");
      e.printStackTrace(System.err);
      throw new InitializationException(e.getMessage());
    }
  }
}
