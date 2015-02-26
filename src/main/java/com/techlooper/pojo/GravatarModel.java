package com.techlooper.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by chrisshayan on 2/21/15.
 *
 * @see For complete version of the JSON have a look at https://en.gravatar.com/205e460b479e2e5b48aec07710c08d50.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GravatarModel {
    private String profileUrl, thumbnailUrl, preferredUsername, displayName, aboutMe;

    @JsonProperty("photos")
    private List<PhotoModel> photos;

    @JsonProperty("profileBackground")
    private ProfileBackgroundModel profileBackground;

    public ProfileBackgroundModel getProfileBackground() {
        return profileBackground;
    }

    public void setProfileBackground(ProfileBackgroundModel profileBackground) {
        this.profileBackground = profileBackground;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public List<PhotoModel> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoModel> photos) {
        this.photos = photos;
    }
}
