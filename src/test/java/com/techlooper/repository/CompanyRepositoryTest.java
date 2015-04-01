package com.techlooper.repository;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class CompanyRepositoryTest {

    @Resource
    private CompanyRepository companyRepository;

    @Test
    public void testConnection() throws Exception {
        assertNotNull(companyRepository);
    }

    @Test
    public void testCountCompany() throws Exception {
        long count = companyRepository.count();
        assertTrue(count > 0);
    }
}