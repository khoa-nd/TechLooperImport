package com.techlooper.imports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by NguyenDangKhoa on 2/9/15.
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackages = "com.techlooper")
public class VietnamworksUserImport {

  public static void main(String[] args) throws Throwable {
    SpringApplication app = new SpringApplication(VietnamworksUserImport.class);
    app.run();
  }

}
