package com.techlooper.service;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.entity.VietnamworksCompanyEntity;
import com.techlooper.pojo.Job;
import com.techlooper.repository.vietnamworks.VietnamworksCompanyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchUserImportConfiguration.class, ElasticsearchVietnamworksConfiguration.class})
public class CompanyServiceTest {

    @Resource
    private CompanyService companyService;

    @Resource
    private JobSearchService jobSearchService;

    @Resource
    private VietnamworksCompanyRepository vietnamworksCompanyRepository;

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
            companyEntity.addIndustry(jobEntity.getIndustries());

            VietnamworksCompanyEntity vietnamworksCompanyEntity = vietnamworksCompanyRepository.findOne(jobEntity.getCompanyId());
            if (vietnamworksCompanyEntity != null) {
                companyEntity.setCompanyLogoURL(vietnamworksCompanyEntity.getCompanyLogoURL());
                companyEntity.setCompanyName(vietnamworksCompanyEntity.getCompanyName());
                companyEntity.setCompanySizeId(vietnamworksCompanyEntity.getCompanySizeId());
                companyEntity.setWebsite(vietnamworksCompanyEntity.getWebsite());
                companyEntity.setAddress(vietnamworksCompanyEntity.getAddress());
            }
            companyService.addCompany(companyEntity);
        }
    }
}