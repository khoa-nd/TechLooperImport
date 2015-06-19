package com.techlooper.linkedin;

import org.junit.Test;

import static org.junit.Assert.*;

public class SkillCrawlerTest {

    @Test
    public void testCrawl() throws Exception {
        SkillCrawler skillCrawler = new SkillCrawler();
        SkillResultList skillResultList = skillCrawler.crawl("Java");
        assertTrue(skillResultList.getResultList().size() > 0);
    }
}