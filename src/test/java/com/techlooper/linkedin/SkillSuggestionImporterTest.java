package com.techlooper.linkedin;

import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchVietnamworksConfiguration.class})
public class SkillSuggestionImporterTest {

    @Resource
    private SkillSuggestionImporter skillSuggestionImporter;

    @Test
    public void testImportLinkedInSkill() throws Exception {
        skillSuggestionImporter.importLinkedInSkill();
    }
}