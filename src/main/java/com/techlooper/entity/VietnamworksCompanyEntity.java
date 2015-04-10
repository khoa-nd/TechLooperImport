package com.techlooper.entity;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
@Document(indexName = "employerInformation", type = "company")
public class VietnamworksCompanyEntity {

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
    private List<String> jobImageURLs;

    @Field
    private List<String> jobVideoURLs;

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

    public List<String> getJobImageURLs() {
        return jobImageURLs;
    }

    public void setJobImageURLs(List<String> jobImageURLs) {
        this.jobImageURLs = jobImageURLs;
    }

    public List<String> getJobVideoURLs() {
        return jobVideoURLs;
    }

    public void setJobVideoURLs(List<String> jobVideoURLs) {
        this.jobVideoURLs = jobVideoURLs;
    }

    public String getCompanyProfile() {
        return companyProfile;
    }

    public void setCompanyProfile(String companyProfile) {
        this.companyProfile = companyProfile;
    }
}
