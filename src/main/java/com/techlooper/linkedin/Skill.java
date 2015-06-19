package com.techlooper.linkedin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by NguyenDangKhoa on 6/19/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Skill {

    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
