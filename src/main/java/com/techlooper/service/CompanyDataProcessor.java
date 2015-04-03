package com.techlooper.service;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.entity.VietnamworksCompanyEntity;
import com.techlooper.pojo.Job;
import com.techlooper.repository.vietnamworks.VietnamworksCompanyRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by NguyenDangKhoa on 4/2/15.
 */
@Service
public class CompanyDataProcessor {


    @Resource
    private VietnamworksCompanyRepository vietnamworksCompanyRepository;

    public CompanyEntity process(JobEntity jobEntity) throws Exception {
        CompanyEntity companyEntity = new CompanyEntity();
        try {
            companyEntity.setCompanyId(jobEntity.getCompanyId());
            companyEntity.addBenefit(jobEntity.getBenefits());
            companyEntity.addSkill(jobEntity.getSkills());
            String jobURL = "/" + jobEntity.getAlias() + "-" + jobEntity.getJobId() + "-jd";
            companyEntity.addJob(new Job(jobEntity.getJobId(), jobEntity.getJobTitle(), jobURL, jobEntity.getExpiredDate(),
                    jobEntity.getNumOfViews(), jobEntity.getNumOfApplications()));
            companyEntity.addIndustry(jobEntity.getIndustries());

            VietnamworksCompanyEntity vietnamworksCompanyEntity = vietnamworksCompanyRepository.findOne(jobEntity.getCompanyId());
            if (vietnamworksCompanyEntity != null) {
                companyEntity.setCompanyLogoURL(vietnamworksCompanyEntity.getCompanyLogoURL());
                companyEntity.setCompanyName(vietnamworksCompanyEntity.getCompanyName());
                companyEntity.setCompanySizeId(vietnamworksCompanyEntity.getCompanySizeId());
                companyEntity.setWebsite(vietnamworksCompanyEntity.getWebsite());
                companyEntity.setAddress(vietnamworksCompanyEntity.getAddress());
            }
        } catch (Exception ex) {
            throw ex;
        }
        return companyEntity;
    }
}
