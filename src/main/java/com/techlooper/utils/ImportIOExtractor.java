package com.techlooper.utils;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
public class ImportIOExtractor {

    private String connectorId;

    private String userId;

    private String apiKey;

    public ImportIOExtractor(String connectorId, String userId, String apiKey) {
        this.connectorId = connectorId;
        this.userId = userId;
        this.apiKey = apiKey;
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
}
