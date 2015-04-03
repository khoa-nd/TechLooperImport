package com.techlooper.service;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.JobEntity;
import com.techlooper.pojo.Industry;
import com.techlooper.pojo.Job;
import com.techlooper.pojo.Skill;
import com.techlooper.repository.CompanyRepository;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
@Service
public class CompanyService {

    private static Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    public static final int TOTAL_USER_PER_PAGE = 50;

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
            Set<Job> tmp = company.getJobs();
            existCompany.getJobs().removeAll(company.getJobs());
            existCompany.addJob(tmp);
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

    private NativeSearchQueryBuilder getCompanySearchAllQuery() {
        return new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery());
    }

    public long countTotalNumberOfCompany() {
        SearchQuery searchQuery = getCompanySearchAllQuery().build();
        return companyRepository.search(searchQuery).getTotalElements();
    }

    public void removeNonITIndustryInCompanyProfile() {
        NativeSearchQueryBuilder searchQueryBuilder = getCompanySearchAllQuery();
        long total = countTotalNumberOfCompany();
        int pageIndex = 0;
        int successRemoved = 0;
        Set<Industry> itIndustries = new HashSet<>();
        Industry softwareIndustry = new Industry();
        softwareIndustry.setIndustryId("35");
        itIndustries.add(softwareIndustry);
        Industry hardwareIndustry = new Industry();
        hardwareIndustry.setIndustryId("55");
        itIndustries.add(hardwareIndustry);
        Industry internetIndustry = new Industry();
        internetIndustry.setIndustryId("57");
        itIndustries.add(internetIndustry);

        while(pageIndex < total) {
            FacetedPage<CompanyEntity> companyEntities = companyRepository.search(
                    searchQueryBuilder.withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE)).build());
            List<CompanyEntity> companies = companyEntities.getContent();

            for(CompanyEntity company : companies) {
                Set<Industry> industries = company.getIndustries();
                industries.retainAll(itIndustries);
                company.setIndustries(industries);

                companyRepository.save(company);
                successRemoved++;
                LOGGER.info("Company #" + successRemoved + " : " + company.getCompanyName() + " has been removed non-it industry");
            }
            pageIndex++;
        }
    }

    public void removeDuplicatedSkillInCompanyProfile() {
        NativeSearchQueryBuilder searchQueryBuilder = getCompanySearchAllQuery();
        long total = countTotalNumberOfCompany();
        int pageIndex = 0;
        int successRemoved = 0;

        while(pageIndex < total) {
            FacetedPage<CompanyEntity> companyEntities = companyRepository.search(
                    searchQueryBuilder.withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE)).build());
            List<CompanyEntity> companies = companyEntities.getContent();

            for(CompanyEntity company : companies) {
                Set<Skill> skills = company.getSkills();
                if (skills != null && !skills.isEmpty()) {
                    Set<Skill> cloneSkill = new HashSet<>(skills);
                    for(Skill skill : cloneSkill) {
                        if (skill.getSkillName().contains(";") || skill.getSkillName().contains(",")
                                || skill.getSkillName().contains("/")) {
                            processSkillList(skills, skill);
                        }
                    }
                    company.setSkills(skills);
                    companyRepository.save(company);
                    successRemoved++;
                    LOGGER.info("Company #" + successRemoved + " : " + company.getCompanyName() + " has been removed duplicated skills");
                }
            }
            pageIndex++;
        }
    }

    private void processSkillList(Set<Skill> skills, Skill skill) {
        String[] tokens = skill.getSkillName().trim().split(";");
        for(int i = 0; i < tokens.length; i++) {
            Skill newSkill = new Skill();
            newSkill.setSkillId(skill.getSkillId() + i + 9999);
            newSkill.setSkillName(tokens[i]);
            newSkill.setSkillWeight(skill.getSkillWeight());
            skills.add(newSkill);
        }
        skills.remove(skill);
    }
}
