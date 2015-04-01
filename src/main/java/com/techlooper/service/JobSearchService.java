package com.techlooper.service;

import com.techlooper.entity.JobEntity;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
@Service
public class JobSearchService {

    public static final int TOTAL_USER_PER_PAGE = 50;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplateVietnamworks;

    public List<JobEntity> getITJob(int pageIndex) {
        SearchQuery searchQuery = getITJobSearchQuery(pageIndex);
        Page<JobEntity> jobEntities = elasticsearchTemplateVietnamworks.queryForPage(searchQuery, JobEntity.class);
        return jobEntities.getContent();
    }

    public long countITJob() {
        SearchQuery searchQuery = getITJobSearchQuery(0);
        Page<JobEntity> jobEntities = elasticsearchTemplateVietnamworks.queryForPage(searchQuery, JobEntity.class);
        return jobEntities.getTotalElements();
    }

    public List<JobEntity> getJobHasBenefit(int pageIndex) {
        SearchQuery searchQuery = getJobHasBenefitSearchQuery(pageIndex);
        Page<JobEntity> jobEntities = elasticsearchTemplateVietnamworks.queryForPage(searchQuery, JobEntity.class);
        return jobEntities.getContent();
    }

    public List<JobEntity> getJobHasSkill(int pageIndex) {
        SearchQuery searchQuery = getJobHasSkillSearchQuery(pageIndex);
        Page<JobEntity> jobEntities = elasticsearchTemplateVietnamworks.queryForPage(searchQuery, JobEntity.class);
        return jobEntities.getContent();
    }

    private SearchQuery getITJobSearchQuery(int pageIndex) {
        return new NativeSearchQueryBuilder()
                .withQuery(nestedQuery("industries", QueryBuilders.boolQuery()
                        .minimumNumberShouldMatch(1)
                        .should(QueryBuilders.termQuery("industries.industryId", 35))
                        .should(QueryBuilders.termQuery("industries.industryId", 55))
                        .should(QueryBuilders.termQuery("industries.industryId", 57))))
                .withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE))
                .build();
    }

    private SearchQuery getJobHasBenefitSearchQuery(int pageIndex) {
        return new NativeSearchQueryBuilder()
                .withQuery(nestedQuery("benefits", QueryBuilders.filteredQuery(
                        QueryBuilders.matchAllQuery(),
                        FilterBuilders.existsFilter("benefits.benefitId"))))
                .withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE))
                .build();
    }

    private SearchQuery getJobHasSkillSearchQuery(int pageIndex) {
        return new NativeSearchQueryBuilder()
                .withQuery(nestedQuery("skills", QueryBuilders.filteredQuery(
                        QueryBuilders.matchAllQuery(),
                        FilterBuilders.existsFilter("skills.skillId"))))
                .withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE))
                .build();
    }
}
