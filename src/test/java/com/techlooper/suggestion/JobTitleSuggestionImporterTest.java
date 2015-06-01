package com.techlooper.suggestion;

import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import com.techlooper.configuration.VietnamworksDatabaseConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VietnamworksDatabaseConfiguration.class, ElasticsearchVietnamworksConfiguration.class})
public class JobTitleSuggestionImporterTest {

    @Resource
    private JobTitleSuggestionImporter importer;

    @Test
    public void testImportJobTitle() throws Exception {
        importer.importJobTitle();
    }
}