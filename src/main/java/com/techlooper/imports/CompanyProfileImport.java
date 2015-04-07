package com.techlooper.imports;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
@EnableScheduling
@PropertySources({
        @PropertySource("classpath:application.properties")})
public class CompanyProfileImport {

    private static Logger LOGGER = LoggerFactory.getLogger(CompanyProfileImport.class);

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ElasticsearchUserImportConfiguration.class,
                ElasticsearchVietnamworksConfiguration.class, CompanyProfileImport.class);
    }
}
