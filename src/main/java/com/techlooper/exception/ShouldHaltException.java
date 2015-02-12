package com.techlooper.exception;

/**
 * Created by phuonghqh on 2/12/15.
 */
public class ShouldHaltException extends RuntimeException {

  public ShouldHaltException(String error) {
    super(error);
  }
}

