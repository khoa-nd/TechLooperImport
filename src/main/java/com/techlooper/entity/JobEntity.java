package com.techlooper.entity;

import com.techlooper.pojo.Benefit;
import com.techlooper.pojo.Skill;
import org.elasticsearch.common.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Long;

/**
 * Created by khoanguyendang on 01/04/15.
 */
@Document(indexName = "vietnamworks", type = "job")
public class JobEntity {

    @Id
    private String jobId;

    @Field
    private String alias;

    @Field
    private String jobTitle;

    @Field(type = Long)
    private long companyId;

    @Field
    private List<Benefit> benefits;

    @Field
    private List<Skill> skills;

    @Field(type = FieldType.String)
    private String expiredDate;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
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

    public List<Benefit> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<Benefit> benefits) {
        this.benefits = benefits;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }
}
