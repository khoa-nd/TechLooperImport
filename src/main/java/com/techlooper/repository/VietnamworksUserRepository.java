package com.techlooper.repository;

import com.techlooper.pojo.VietnamworksUser;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by NguyenDangKhoa on 2/10/15.
 */
@Repository("vietnamworksUserRepository")
public class VietnamworksUserRepository {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final String resumeSqlQuery = "select r.* from tblresume r inner join tblregistrationinfo i on r.userid = i.userid where resumeid in (:resumeIds) and i.isactive = 1";
    private final String totalUserSqlQuery = "select count(distinct resumeid) from tblresume_industry where industryid = 35";
    private final String getResumeListSqlQuery = "select resumeid from tblresume_industry where industryid = 35 limit ?, ?";
    private final String totalRegisteredUserSqlQuery = "SELECT COUNT(userid) FROM tblregistrationinfo";
    private final String getJobTitleQuery = "SELECT DISTINCT jobTitle FROM tblregistrationinfo WHERE jobTitle IS NOT NULL LIMIT ?, ?";


    public int getTotalUser() {
        return jdbcTemplate.queryForInt(totalUserSqlQuery);
    }

    public List<Long> getResumeList(int fromIndex, int pageSize) {
        return jdbcTemplate.queryForList(getResumeListSqlQuery, Long.class, fromIndex, pageSize);
    }

    public List<VietnamworksUser> getUsersByResumeId(List<Long> resumeIds) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("resumeIds", resumeIds);
        return namedParameterJdbcTemplate.query(resumeSqlQuery, parameterMap,
                new BeanPropertyRowMapper<VietnamworksUser>(VietnamworksUser.class));
    }

    public int getTotalNumberOfRegistrationUsers() {
        return jdbcTemplate.queryForInt(totalRegisteredUserSqlQuery);
    }

    public List<String> getJobTitles(int fromIndex, int pageSize) {
        return jdbcTemplate.queryForList(getJobTitleQuery, String.class, fromIndex, pageSize);
    }
}
