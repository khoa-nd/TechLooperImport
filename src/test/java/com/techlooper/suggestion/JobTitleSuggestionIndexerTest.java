package com.techlooper.suggestion;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ElasticsearchVietnamworksConfiguration.class})
public class JobTitleSuggestionIndexerTest {

    @Resource
    private JobTitleSuggestionIndexer jobTitleSuggestionIndexer;

    @Test
    public void testIndex() throws Exception {
        List<String> jobTitles = new ArrayList<>();
        jobTitles.add("Java Developer");
        jobTitles.add("Kế toán trưởng");
        jobTitleSuggestionIndexer.index(jobTitles);
    }
}