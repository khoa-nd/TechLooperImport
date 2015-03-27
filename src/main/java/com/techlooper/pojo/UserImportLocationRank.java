package com.techlooper.pojo;

import java.util.List;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
public class UserImportLocationRank {

    private String updatedTime;

    private int repos;

    private int stars;

    private List<LocationRank> locations;

    public UserImportLocationRank(String updatedTime, int repos, int stars, List<LocationRank> locations) {
        this.updatedTime = updatedTime;
        this.repos = repos;
        this.stars = stars;
        this.locations = locations;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
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

    public List<LocationRank> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationRank> locations) {
        this.locations = locations;
    }

    public static class LocationRank {

        private String type;

        private String name;

        private int rank;

        private int total;

        public LocationRank(String type, String name, int rank, int total) {
            this.type = type;
            this.name = name;
            this.rank = rank;
            this.total = total;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}
