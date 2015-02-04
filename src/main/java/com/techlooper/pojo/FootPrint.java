package com.techlooper.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by phuonghqh on 2/4/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FootPrint {

  private Integer lastPageNumber = 0;

  public Integer getLastPageNumber() {
    return lastPageNumber;
  }

  public void setLastPageNumber(Integer lastPageNumber) {
    this.lastPageNumber = lastPageNumber;
  }

  public static class FootPrintBuilder {
    private FootPrint footPrint;

    private FootPrintBuilder() {
      footPrint = new FootPrint();
    }

    public FootPrintBuilder withLastPageNumber(Integer lastPageNumber) {
      footPrint.lastPageNumber = lastPageNumber;
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
