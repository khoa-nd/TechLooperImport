package com.techlooper.repository;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.repository.vietnamworks.JobSearchResultRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class JobSearchResultRepositoryTest {

    @Resource
    private JobSearchResultRepository jobSearchResultRepository;

    @Test
    public void testConnection() throws Exception {
        assertNotNull(jobSearchResultRepository);
    }

    @Test
    public void testCountJob() throws Exception {
        long count = jobSearchResultRepository.count();
        assertTrue(count > 0);
    }
}