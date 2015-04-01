package com.techlooper.service;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.entity.VietnamworksCompanyEntity;
import com.techlooper.pojo.Job;
import com.techlooper.repository.CompanyRepository;
import com.techlooper.repository.vietnamworks.VietnamworksCompanyRepository;
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
    private VietnamworksCompanyRepository vietnamworksCompanyRepository;

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

    public void runImport() {
        long totalITJob = jobSearchService.countITJob();
        long totalPage = totalITJob % JobSearchService.TOTAL_USER_PER_PAGE == 0 ?
                totalITJob / JobSearchService.TOTAL_USER_PER_PAGE : totalITJob / JobSearchService.TOTAL_USER_PER_PAGE + 1;
        int pageIndex = 0;

        while(pageIndex < totalPage) {
            List<JobEntity> jobEntities = jobSearchService.getITJob(pageIndex);

            for(JobEntity jobEntity : jobEntities) {
                CompanyEntity companyEntity = new CompanyEntity();
                try {
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
                    addCompany(companyEntity);
                    LOGGER.info("Import " + companyEntity.getCompanyName() + " OK.");
                } catch (Exception ex) {
                    LOGGER.info("Import " + companyEntity.getCompanyName() + " Fail." + ex.getMessage());
                }
            }
            pageIndex++;
        }
    }
}
