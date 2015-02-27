package com.techlooper.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by chrisshayan on 2/21/15.
 */
public class GravatarResponse {
    @JsonProperty("entry")
    private List<GravatarModel> gravatarModels;

    public List<GravatarModel> getGravatarModels() {
        return gravatarModels;
    }

    public void setGravatarModels(List<GravatarModel> gravatarModels) {
        this.gravatarModels = gravatarModels;
    }
}