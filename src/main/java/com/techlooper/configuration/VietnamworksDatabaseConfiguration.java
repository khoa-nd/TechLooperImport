package com.techlooper.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by NguyenDangKhoa on 2/9/15.
 */
@Configuration
@PropertySources({
        @PropertySource("classpath:application.properties")})
public class VietnamworksDatabaseConfiguration {

  @Value("${vietnamworks.db.connectionUrl}")
  private String connectionUrl;

  @Value("${vietnamworks.db.username}")
  private String username;

  @Value("${vietnamworks.db.password}")
  private String password;

  @Bean(name = "vnwDataSource")
  public DataSource dataSource() throws SQLException {
    DataSource ds = new DriverManagerDataSource(connectionUrl, username, password);
    if (ds.getConnection() == null) {
      throw new SQLException();
    }
    return ds;
  }

  @Bean(name = "vnwJdbcTemplate")
  public JdbcTemplate jdbcTemplate(DataSource vnwDataSource) {
    return new JdbcTemplate(vnwDataSource);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

}
