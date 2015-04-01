package com.techlooper.service;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.pojo.Job;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class})
public class CompanyServiceTest {

    @Resource
    private CompanyService companyService;

    @Resource
    private JobSearchService jobSearchService;

    @Test
    public void testAddCompany() throws Exception {
        List<JobEntity> jobEntities = jobSearchService.getJobHasBenefit(0);

        if (!jobEntities.isEmpty()) {
            JobEntity jobEntity = jobEntities.get(0);
            CompanyEntity companyEntity = new CompanyEntity();
            companyEntity.setCompanyId(jobEntity.getCompanyId());
            companyEntity.addBenefit(jobEntity.getBenefits());
            companyEntity.addSkill(jobEntity.getSkills());
            String jobURL = "/" + jobEntity.getAlias() + "-" + jobEntity.getJobId() + "-jd";
            companyEntity.addJob(new Job(jobEntity.getJobId(), jobEntity.getJobTitle(), jobURL, jobEntity.getExpiredDate()));

            companyService.addCompany(companyEntity);
        }
    }
}