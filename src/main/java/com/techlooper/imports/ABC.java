package com.techlooper.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by phuonghqh on 1/29/15.
 */
public class ABC {

  private static Calendar calendar = Calendar.getInstance(Locale.US);

  public static void main(String[] args) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode root = objectMapper.readTree("{\"address\":{\"zip\":\"98040\", \"city\":\"Mercer Island\"}}");
    System.out.println((ArrayNode)root);
//    System.out.println(root.at("/address/city").asText());
//    ((ObjectNode)root.at("/address")).putArray("city").add("HCM").add("HN");
//    System.out.println(root.at("/address/zip").isTextual());
  }
}
