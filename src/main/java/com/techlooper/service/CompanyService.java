package com.techlooper.service;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
@Service
public class CompanyService {

    private static Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private JobSearchService jobSearchService;

    @Resource
    private CompanyDataProcessor companyDataProcessor;

    public void addCompany(CompanyEntity company) {
        CompanyEntity existCompany = companyRepository.findOne(company.getCompanyId());

        if (existCompany == null) {
            companyRepository.save(company);
        } else {
            existCompany.addBenefit(company.getBenefits());
            existCompany.addSkill(company.getSkills());
            existCompany.addJob(company.getJobs());
            existCompany.addIndustry(company.getIndustries());
            companyRepository.save(existCompany);
        }
    }

    public int runImportCompany() {
        long totalITJob = jobSearchService.countITJob();
        long totalPage = totalITJob % JobSearchService.TOTAL_USER_PER_PAGE == 0 ?
                totalITJob / JobSearchService.TOTAL_USER_PER_PAGE : totalITJob / JobSearchService.TOTAL_USER_PER_PAGE + 1;
        int pageIndex = 0;
        int successCompanyAdd = 0;

        while (pageIndex < totalPage) {
            List<JobEntity> jobEntities = jobSearchService.getITJob(pageIndex);

            for (JobEntity jobEntity : jobEntities) {
                try {
                    CompanyEntity companyEntity = companyDataProcessor.process(jobEntity);
                    addCompany(companyEntity);
                    LOGGER.info("Import " + companyEntity.getCompanyName() + " OK.");
                    successCompanyAdd++;
                } catch (Exception ex) {
                    LOGGER.info("Import " + jobEntity.getCompanyId() + " Fail." + ex.getMessage());
                }
            }
            pageIndex++;
        }
        return successCompanyAdd;
    }
}
