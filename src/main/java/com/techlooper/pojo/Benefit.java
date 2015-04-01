package com.techlooper.pojo;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
public class Benefit {

    private int benefitId;

    private String benefitValue;

    public int getBenefitId() {
        return benefitId;
    }

    public void setBenefitId(int benefitId) {
        this.benefitId = benefitId;
    }

    public String getBenefitValue() {
        return benefitValue;
    }

    public void setBenefitValue(String benefitValue) {
        this.benefitValue = benefitValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Benefit benefit = (Benefit) o;

        if (benefitId != benefit.benefitId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return benefitId;
    }
}
