package com.techlooper.suggestion;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Service
public class JobTitleSuggestionIndexer {

    @Resource
    private ElasticsearchTemplate elasticsearchTemplateVietnamworks;

    public void index(List<String> jobTitles) {
        List<IndexQuery> indexQueries = jobTitles.stream().map(
                jobTitle -> new JobTitleSuggestionEntityBuilder(jobTitle)
                                .suggest(new String[]{jobTitle}, jobTitle).buildIndex()).collect(Collectors.toList());

        elasticsearchTemplateVietnamworks.bulkIndex(indexQueries);
        elasticsearchTemplateVietnamworks.refresh(JobTitleSuggestionEntity.class, true);
    }
}
