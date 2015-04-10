package com.techlooper.entity;

import com.techlooper.pojo.Benefit;
import com.techlooper.pojo.Industry;
import com.techlooper.pojo.Job;
import com.techlooper.pojo.Skill;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by khoanguyendang on 01/04/15.
 */
@Document(indexName = "techlooper", type = "company")
public class CompanyEntity {

    @Id
    private Long companyId;

    @Field
    private String companyLogoURL;

    @Field
    private String companyName;

    @Field
    private int companySizeId;

    @Field
    private String website;

    @Field
    private String address;

    @Field
    private Set<Benefit> benefits;

    @Field
    private Set<Skill> skills;

    @Field
    private Set<Job> jobs;

    @Field
    private Set<Industry> industries;

    @Field
    private Set<String> jobImageURLs;

    @Field
    private Set<String> jobVideoURLs;

    private int score;

    @Field
    private String companyProfile;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyLogoURL() {
        return companyLogoURL;
    }

    public void setCompanyLogoURL(String companyLogoURL) {
        this.companyLogoURL = companyLogoURL;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getCompanySizeId() {
        return companySizeId;
    }

    public void setCompanySizeId(int companySizeId) {
        this.companySizeId = companySizeId;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Benefit> getBenefits() {
        return benefits;
    }

    public void setBenefits(Set<Benefit> benefits) {
        this.benefits = benefits;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    public Set<Job> getJobs() {
        return jobs;
    }

    public void setJobs(Set<Job> jobs) {
        this.jobs = jobs;
    }

    public Set<Industry> getIndustries() {
        return industries;
    }

    public void setIndustries(Set<Industry> industries) {
        this.industries = industries;
    }

    public Set<String> getJobImageURLs() {
        return jobImageURLs;
    }

    public void setJobImageURLs(Set<String> jobImageURLs) {
        this.jobImageURLs = jobImageURLs;
    }

    public Set<String> getJobVideoURLs() {
        return jobVideoURLs;
    }

    public void setJobVideoURLs(Set<String> jobVideoURLs) {
        this.jobVideoURLs = jobVideoURLs;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCompanyProfile() {
        return companyProfile;
    }

    public void setCompanyProfile(String companyProfile) {
        this.companyProfile = companyProfile;
    }

    public void addBenefit(Set<Benefit> benefits) {
        if (this.benefits == null) {
            this.benefits = new HashSet<>();
        }
        this.benefits.addAll(benefits);
    }

    public void addSkill(Set<Skill> skills) {
        if (this.skills == null) {
            this.skills = new HashSet<>();
        }
        this.skills.addAll(skills);
    }

    public void addJob(Set<Job> jobs) {
        if (this.jobs == null) {
            this.jobs = new HashSet<>();
        }
        this.jobs.addAll(jobs);
    }

    public void addJob(Job job) {
        if (this.jobs == null) {
            this.jobs = new HashSet<>();
        }
        this.jobs.add(job);
    }

    public void addIndustry(Set<Industry> industries) {
        if (this.industries == null) {
            this.industries = new HashSet<>();
        }
        this.industries.addAll(industries);
    }

    public void addJobImageURL(List<String> jobImageURLs) {
        if (this.jobImageURLs == null) {
            this.jobImageURLs = new HashSet<>();
        }
        this.jobImageURLs.addAll(jobImageURLs);
    }

    public void addJobVideoURL(List<String> jobVideoURLs) {
        if (this.jobVideoURLs == null) {
            this.jobVideoURLs = new HashSet<>();
        }
        this.jobVideoURLs.addAll(jobVideoURLs);
    }
}
