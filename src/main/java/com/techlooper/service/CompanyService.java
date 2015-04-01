package com.techlooper.service;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
@Service
public class CompanyService {

    @Resource
    private CompanyRepository companyRepository;

    public void addCompany(CompanyEntity company) {
        CompanyEntity existCompany = companyRepository.findOne(company.getCompanyId());

        if (existCompany == null) {
            companyRepository.save(company);
        } else {
            existCompany.getBenefits().addAll(company.getBenefits());
            existCompany.getSkills().addAll(company.getSkills());
            existCompany.getJobs().addAll(company.getJobs());
            companyRepository.save(existCompany);
        }
    }
}
