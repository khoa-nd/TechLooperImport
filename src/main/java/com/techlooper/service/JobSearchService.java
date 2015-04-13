package com.techlooper.service;

import com.techlooper.entity.JobEntity;
import com.techlooper.pojo.Job;
import com.techlooper.repository.vietnamworks.JobSearchResultRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by NguyenDangKhoa on 4/1/15.
 */
@Service
public class JobSearchService {

    public static final int TOTAL_USER_PER_PAGE = 50;

    @Value("${scheduled.cron.companyImport.importAll}")
    private boolean isImportAll;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplateVietnamworks;

    @Resource
    private JobSearchResultRepository jobSearchResultRepository;

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

    public List<JobEntity> getSimilarJob(Set<Job> originalJobs, int pageIndex) {
        final int TOP_NUMBER_OF_JOBS = 3;
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        // They should be IT-Software jobs
        BoolQueryBuilder boolQueryBuilder = boolQuery().should(termQuery("industries.industryId", 35));
        originalJobs.stream().limit(TOP_NUMBER_OF_JOBS).forEach(job -> {
                    JobEntity vietnamworksJob = jobSearchResultRepository.findOne(job.getJobId());
                    // They should match original top 3 job's titles
                    boolQueryBuilder.should(matchQuery("jobTitle", vietnamworksJob.getJobTitle()));
                    // They should be the same as company size
                    boolQueryBuilder.should(termQuery("companySizeId", vietnamworksJob.getCompanySizeId()));
                    // They should match original job's company address
                    boolQueryBuilder.should(termQuery("address", vietnamworksJob.getAddress()));
                });
        // The query should match at least 2 conditions
        boolQueryBuilder.minimumNumberShouldMatch(2);

        searchQueryBuilder.withQuery(boolQueryBuilder);
        searchQueryBuilder.withFilter(FilterBuilders.rangeFilter("approvedDate").from("now-3w"));
        searchQueryBuilder.withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE));

        Page<JobEntity> jobEntities = elasticsearchTemplateVietnamworks.queryForPage(searchQueryBuilder.build(), JobEntity.class);
        return jobEntities.getContent();
    }

    private SearchQuery getITJobSearchQuery(int pageIndex) {
        NativeSearchQueryBuilder searchQueryBuilder = getNativeSearchQueryBuilderITJob();

        if (isImportAll) {
            searchQueryBuilder.withFilter(FilterBuilders.matchAllFilter());
        } else {
            searchQueryBuilder.withFilter(FilterBuilders.rangeFilter("approvedDate").from("now-1w"));
        }

        searchQueryBuilder.withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE));
        return searchQueryBuilder.build();
    }

    private NativeSearchQueryBuilder getNativeSearchQueryBuilderITJob() {
        return new NativeSearchQueryBuilder()
                    .withQuery(nestedQuery("industries", QueryBuilders.boolQuery()
                            .minimumNumberShouldMatch(1)
                            .should(QueryBuilders.termQuery("industries.industryId", 35))));
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
