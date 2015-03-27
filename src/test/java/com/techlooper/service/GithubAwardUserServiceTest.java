package com.techlooper.service;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.entity.UserImportEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class GithubAwardUserServiceTest {

    @Resource
    private GithubAwardUserService githubAwardUserService;

    @Test
    public void testGetUserByCountry() throws Exception {
        List<UserImportEntity> userImportEntities = githubAwardUserService.getUserByCountry("vietnam", 0);
        assertTrue(userImportEntities.size() > 0);
    }

    @Test
    public void testCountTotalNumberOfUserByCountry() throws Exception {
        long total = githubAwardUserService.getTotalPageOfUserByCountry("vietnam");
        assertTrue(total > 0);
    }
}