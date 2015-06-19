package com.techlooper.linkedin;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Service
public class SkillSuggestionIndexer {

    @Resource
    private ElasticsearchTemplate elasticsearchTemplateVietnamworks;

    public void index(List<Skill> skills) {
        List<IndexQuery> indexQueries = skills.stream().map(
                skill -> new SkillSuggestionEntityBuilder(skill.getDisplayName())
                                .suggest(new String[]{skill.getDisplayName()}, skill.getDisplayName()).buildIndex()).collect(Collectors.toList());

        if (!indexQueries.isEmpty()) {
            elasticsearchTemplateVietnamworks.bulkIndex(indexQueries);
            elasticsearchTemplateVietnamworks.refresh(SkillSuggestionEntity.class, true);
        }
    }
}
