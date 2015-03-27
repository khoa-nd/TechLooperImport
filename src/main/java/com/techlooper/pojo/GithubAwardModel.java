package com.techlooper.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAwardModel {

    private String country;

    private String city;

    private String language;

    private int repos;

    private int stars;

    @JsonProperty("city_rank")
    private String cityRank;

    @JsonProperty("country_rank")
    private String countryRank;

    @JsonProperty("worldwide_rank")
    private String worldwideRank;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getRepos() {
        return repos;
    }

    public void setRepos(int repos) {
        this.repos = repos;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getCityRank() {
        return cityRank;
    }

    public void setCityRank(String cityRank) {
        this.cityRank = cityRank;
    }

    public String getCountryRank() {
        return countryRank;
    }

    public void setCountryRank(String countryRank) {
        this.countryRank = countryRank;
    }

    public String getWorldwideRank() {
        return worldwideRank;
    }

    public void setWorldwideRank(String worldwideRank) {
        this.worldwideRank = worldwideRank;
    }
}
