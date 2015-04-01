package com.techlooper.entity;

import com.techlooper.pojo.Benefit;
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
    private Set<Benefit> benefits;

    @Field
    private Set<Skill> skills;

    @Field
    private Set<Job> jobs;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
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

    public void addBenefit(List<Benefit> benefits) {
        if (this.benefits == null) {
            this.benefits = new HashSet<>();
        }
        this.benefits.addAll(benefits);
    }

    public void addSkill(List<Skill> skills) {
        if (this.skills == null) {
            this.skills = new HashSet<>();
        }
        this.skills.addAll(skills);
    }

    public void addJob(Job job) {
        if (this.jobs == null) {
            this.jobs = new HashSet<>();
        }
        this.jobs.add(job);
    }
}
