package com.techlooper.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAwardResponse {

    private String pageUrl;

    private String error;

    private List<GithubAwardModel> results;

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public List<GithubAwardModel> getResults() {
        return results;
    }

    public void setResults(List<GithubAwardModel> results) {
        this.results = results;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
