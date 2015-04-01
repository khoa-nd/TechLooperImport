package com.techlooper.service;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.entity.JobEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class JobSearchServiceTest {

    @Resource
    private JobSearchService jobSearchService;

    @Test
    public void testGetITJob() throws Exception {
        List<JobEntity> itJobs = jobSearchService.getITJob(0);
        assertTrue(itJobs.size() > 0);
    }

    @Test
    public void testCountITJob() throws Exception {
        long count = jobSearchService.countITJob();
        assertTrue(count > 0);
    }

    @Test
    public void testGetJobHasBenefit() throws Exception {
        List<JobEntity> hasBenefitJobs = jobSearchService.getJobHasBenefit(0);
        assertTrue(hasBenefitJobs.size() > 0);
    }

    @Test
    public void testGetJobHasSkill() throws Exception {
        List<JobEntity> hasSkillJobs = jobSearchService.getJobHasSkill(0);
        assertTrue(hasSkillJobs.size() > 0);
    }
}