package com.techlooper.pojo;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
public class Skill {

    private int skillId;

    private String skillName;

    private int skillWeight;

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getSkillWeight() {
        return skillWeight;
    }

    public void setSkillWeight(int skillWeight) {
        this.skillWeight = skillWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Skill skill = (Skill) o;

        if (skillId != skill.skillId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return skillId;
    }
}