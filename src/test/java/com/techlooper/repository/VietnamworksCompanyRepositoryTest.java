package com.techlooper.repository;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.repository.vietnamworks.VietnamworksCompanyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class VietnamworksCompanyRepositoryTest {

    @Resource
    private VietnamworksCompanyRepository vietnamworksCompanyRepository;

    @Test
    public void testConnection() throws Exception {
        assertNotNull(vietnamworksCompanyRepository);
    }

    @Test
    public void testCountCompany() throws Exception {
        long count = vietnamworksCompanyRepository.count();
        assertTrue(count > 0);
    }

}