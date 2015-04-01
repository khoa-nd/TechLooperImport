package com.techlooper.configuration;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.Resource;

/**
 * Created by NguyenDangKhoa on 28/01/15.
 */
@Configuration
@ComponentScan(basePackages = "com.techlooper.service")
@EnableElasticsearchRepositories(basePackages = "com.techlooper.repository.vietnamworks",
        elasticsearchTemplateRef = "elasticsearchTemplateVietnamworks")
@PropertySources({
        @PropertySource("classpath:application.properties")
})
public class ElasticsearchVietnamworksConfiguration {

    @Resource
    private Environment environment;

    @Resource
    private TransportClient vietnamworksTransportClient;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public FactoryBean<TransportClient> vietnamworksTransportClient() throws Exception {
        TransportClientFactoryBean factory = new TransportClientFactoryBean();
        factory.setClusterName(environment.getProperty("es.vietnamworks.cluster.name"));
        factory.setClusterNodes(environment.getProperty("es.vietnamworks.host"));
        return factory;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplateVietnamworks() {
        return new ElasticsearchTemplate(vietnamworksTransportClient);
    }

}
