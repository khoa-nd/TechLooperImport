package com.techlooper.configuration;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.*;
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
@EnableElasticsearchRepositories(basePackages = "com.techlooper.repository")
@PropertySources({
        @PropertySource("classpath:application.properties")
})
public class ElasticsearchUserImportConfiguration {

    @Resource
    private Environment environment;

    @Resource
    private TransportClient transportClient;

    @Bean
    public FactoryBean<TransportClient> transportClient() throws Exception {
        TransportClientFactoryBean factory = new TransportClientFactoryBean();
        factory.setClusterName(environment.getProperty("es.userimport.cluster.name"));
        factory.setClusterNodes(environment.getProperty("es.userimport.host"));
        return factory;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(transportClient);
    }
}
