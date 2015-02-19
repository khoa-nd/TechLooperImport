package com.techlooper.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by phuonghqh on 2/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FootPrint {

  private Integer lastPageNumber = 0;

  private Map<String, String> crawlers = new HashMap<>();

  public Integer getLastPageNumber() {
    return lastPageNumber;
  }

  public void setLastPageNumber(Integer lastPageNumber) {
    this.lastPageNumber = lastPageNumber;
  }

  public Map<String, String> getCrawlers() {
    return crawlers;
  }

  public void setCrawlers(Map<String, String> crawlers) {
    this.crawlers = crawlers;
  }

  public static class FootPrintBuilder {
    private FootPrint footPrint;

    public FootPrintBuilder withCrawler(String country, String fromTo) {
      footPrint.crawlers.put(country, fromTo);
      return this;
    }

    public static FootPrintBuilder footPrint(FootPrint footPrint) {
      FootPrintBuilder builder = new FootPrintBuilder();
      builder.footPrint = footPrint;
      return builder;
    }

    private FootPrintBuilder() {
      footPrint = new FootPrint();
    }

    public FootPrintBuilder withLastPageNumber(Integer lastPageNumber) {
      footPrint.lastPageNumber = lastPageNumber;
      return this;
    }

    public FootPrintBuilder withCrawlers(Map<String, String> crawlers) {
      footPrint.crawlers = crawlers;
      return this;
    }

    public static FootPrintBuilder footPrint() {
      return new FootPrintBuilder();
    }

    public FootPrint build() {
      return footPrint;
    }
  }
}
