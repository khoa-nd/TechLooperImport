package com.techlooper.configuration;

import com.techlooper.service.SlideShareService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by chrisshayan on 3/1/15.
 */
@Configuration
@PropertySource("classpath:slideshare.properties")
public class SlideShareConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static SlideShareService slideShareService() {
        return new SlideShareService();
    }
}
