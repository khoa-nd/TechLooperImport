package com.techlooper.linkedin;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by NguyenDangKhoa on 6/19/15.
 */
@Service
public class SkillCrawler {

    private final String LINKEDIN_SKILL_API_ENDPOINT = "https://www.linkedin.com/ta/skill?query=";

    public SkillResultList crawl(String query) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        SkillResultList skillResultList =
                restTemplate.getForObject(new URI(LINKEDIN_SKILL_API_ENDPOINT + query), SkillResultList.class);
        return skillResultList;
    }
}
