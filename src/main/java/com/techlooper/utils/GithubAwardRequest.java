package com.techlooper.utils;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
public class GithubAwardRequest {

    private String connectorId;

    private String userId;

    private String apiKey;

    private String inputUrl;

    public GithubAwardRequest(String connectorId, String userId, String apiKey, String inputUrl) {
        this.connectorId = connectorId;
        this.userId = userId;
        this.apiKey = apiKey;
        this.inputUrl = inputUrl;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getInputUrl() {
        return inputUrl;
    }

    public void setInputUrl(String inputUrl) {
        this.inputUrl = inputUrl;
    }
}
