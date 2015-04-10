package com.techlooper.entity;

import com.techlooper.pojo.Benefit;
import com.techlooper.pojo.Industry;
import com.techlooper.pojo.Skill;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Long;

/**
 * Created by khoanguyendang on 01/04/15.
 */
@Document(indexName = "vietnamworks", type = "job")
public class JobEntity {

    @Id
    private Long jobId;

    @Field
    private String alias;

    @Field
    private String jobTitle;

    @Field(type = Long)
    private long companyId;

    @Field
    private Set<Benefit> benefits;

    @Field
    private Set<Skill> skills;

    @Field
    private Set<Industry> industries;

    @Field(type = FieldType.String)
    private String expiredDate;

    @Field
    private long numOfViews;

    @Field
    private long numOfApplications;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
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

    public Set<Industry> getIndustries() {
        return industries;
    }

    public void setIndustries(Set<Industry> industries) {
        this.industries = industries;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public long getNumOfViews() {
        return numOfViews;
    }

    public void setNumOfViews(long numOfViews) {
        this.numOfViews = numOfViews;
    }

    public long getNumOfApplications() {
        return numOfApplications;
    }

    public void setNumOfApplications(long numOfApplications) {
        this.numOfApplications = numOfApplications;
    }
}
