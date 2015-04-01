package com.techlooper.pojo;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
public class Industry {

    private String industryId;

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Industry industry = (Industry) o;

        if (!industryId.equals(industry.industryId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return industryId.hashCode();
    }
}
