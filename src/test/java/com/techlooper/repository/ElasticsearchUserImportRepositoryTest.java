package com.techlooper.repository;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class ElasticsearchUserImportRepositoryTest {

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testConnection() throws Exception {
        assertNotNull(elasticsearchTemplate);
    }
}