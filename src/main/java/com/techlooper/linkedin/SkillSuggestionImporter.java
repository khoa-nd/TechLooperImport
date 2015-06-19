package com.techlooper.linkedin;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Service
public class SkillSuggestionImporter {

    @Resource
    private SkillCrawler skillCrawler;

    @Resource
    private SkillSuggestionIndexer skillSuggestionIndexer;

    private static final String[] alphabets =
            new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    private static final String[] customAlphabets =
            new String[]{" ", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};


    public void importLinkedInSkill() throws Exception {
        for (String character1 : alphabets) {
            StringBuilder queryBuilder = new StringBuilder(character1);
            for (String character2 : customAlphabets) {
                queryBuilder.append(character2);
                for (String character3 : customAlphabets) {
                    queryBuilder.append(character3);
                    for (String character4 : customAlphabets) {
                        queryBuilder.append(character4);
                        SkillResultList skillResultList = skillCrawler.crawl(queryBuilder.toString());
                        if (skillResultList != null && skillResultList.getResultList().size() > 0) {
                            skillSuggestionIndexer.index(skillResultList.getResultList());
                        }
                    }
                }
            }
        }
    }
}
