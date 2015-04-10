package com.techlooper.service;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.entity.VietnamworksCompanyEntity;
import com.techlooper.pojo.Job;
import com.techlooper.repository.CompanyRepository;
import com.techlooper.repository.vietnamworks.VietnamworksCompanyRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

/**
 * Created by NguyenDangKhoa on 4/2/15.
 */
@Service
public class CompanyDataProcessor {

    @Resource
    private VietnamworksCompanyRepository vietnamworksCompanyRepository;

    @Resource
    private CompanyRepository companyRepository;

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
                companyEntity.addJobImageURL(vietnamworksCompanyEntity.getJobImageURLs());
                companyEntity.addJobVideoURL(vietnamworksCompanyEntity.getJobVideoURLs());
            }
        } catch (Exception ex) {
            throw ex;
        }
        return companyEntity;
    }

    public CompanyEntity mergeCompanyProfile(CompanyEntity originalCompany) throws Exception {
        final String[] fields = new String[]{"companyName"};
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhraseQuery("companyName", originalCompany.getCompanyName()))
                .build();
        FacetedPage<CompanyEntity> similarCompanies = companyRepository.search(searchQuery);

        if (similarCompanies.getTotalElements() > 1) {
            List<CompanyEntity> companyEntities = similarCompanies.getContent();
            CompanyEntity latestCompany = companyEntities.stream().sorted(
                    (o1, o2) -> o2.getCompanyId().intValue() - o1.getCompanyId().intValue()).findFirst().get();
            companyEntities.stream().forEach(company -> company.setScore(scoreCompanyProfile(company)));
            CompanyEntity mostRichCompanyProfile = companyEntities.stream().sorted(
                    (companyA, companyB) -> companyB.getScore() - companyA.getScore()).findFirst().get();
            merge(latestCompany, mostRichCompanyProfile);
            return latestCompany;
        }
        return originalCompany;
    }

    private void merge(CompanyEntity latestCompany, CompanyEntity mostRichCompanyProfile) {
        if (StringUtils.isEmpty(latestCompany.getCompanyLogoURL())) {
            latestCompany.setCompanyLogoURL(mostRichCompanyProfile.getCompanyLogoURL());
        }

        if (latestCompany.getCompanySizeId() == 0) {
            latestCompany.setCompanySizeId(mostRichCompanyProfile.getCompanySizeId());
        }

        if (StringUtils.isEmpty(latestCompany.getAddress())) {
            latestCompany.setAddress(mostRichCompanyProfile.getAddress());
        }

        if (StringUtils.isEmpty(latestCompany.getWebsite())) {
            latestCompany.setWebsite(mostRichCompanyProfile.getWebsite());
        }

        if (!mostRichCompanyProfile.getBenefits().isEmpty()) {
            latestCompany.addBenefit(mostRichCompanyProfile.getBenefits());
        }

        if (!mostRichCompanyProfile.getSkills().isEmpty()) {
            latestCompany.addSkill(mostRichCompanyProfile.getSkills());
        }

        if (!mostRichCompanyProfile.getJobs().isEmpty()) {
            latestCompany.addJob(mostRichCompanyProfile.getJobs());
        }

        if (!mostRichCompanyProfile.getJobImageURLs().isEmpty()) {
            latestCompany.addJobImageURL(new ArrayList<>(mostRichCompanyProfile.getJobImageURLs()));
        }

        if (!mostRichCompanyProfile.getJobVideoURLs().isEmpty()) {
            latestCompany.addJobVideoURL(new ArrayList<>(mostRichCompanyProfile.getJobVideoURLs()));
        }
    }

    private int scoreCompanyProfile(CompanyEntity companyEntity) {
        int score = 0;

        if (StringUtils.isNotEmpty(companyEntity.getCompanyLogoURL())) {
            score++;
        }

        if (companyEntity.getCompanySizeId() > 0) {
            score++;
        }

        if (StringUtils.isNotEmpty(companyEntity.getAddress())) {
            score++;
        }

        if (StringUtils.isNotEmpty(companyEntity.getWebsite())) {
            score++;
        }

        if (!companyEntity.getBenefits().isEmpty()) {
            score += companyEntity.getBenefits().size();
        }

        if (!companyEntity.getSkills().isEmpty()) {
            score += companyEntity.getSkills().size();
        }

        if (!companyEntity.getJobs().isEmpty()) {
            score += companyEntity.getJobs().size();
        }

        if (!companyEntity.getJobImageURLs().isEmpty()) {
            score += companyEntity.getJobImageURLs().size();
        }

        if (!companyEntity.getJobVideoURLs().isEmpty()) {
            score += companyEntity.getJobVideoURLs().size();
        }

        return score;
    }
}
