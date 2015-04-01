package com.techlooper.pojo;

import java.util.Date;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
public class Job {

    private String jobId;

    private String jobTitle;

    private String jobURL;

    private String expiredDate;

    public Job() {
    }

    public Job(String jobId, String jobTitle, String jobURL, String expiredDate) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobURL = jobURL;
        this.expiredDate = expiredDate;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobURL() {
        return jobURL;
    }

    public void setJobURL(String jobURL) {
        this.jobURL = jobURL;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (!jobId.equals(job.jobId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return jobId.hashCode();
    }
}
