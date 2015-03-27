package com.techlooper.service;

import com.techlooper.entity.UserImportEntity;
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
 * Created by NguyenDangKhoa on 3/27/15.
 */
@Service
public class GithubAwardUserService {

    private static final int TOTAL_USER_PER_PAGE = 50;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    public List<UserImportEntity> getUserByCountry(String country, int pageIndex) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(nestedQuery("profiles", QueryBuilders.matchQuery("profiles.GITHUB.location", country)))
                .withPageable(new PageRequest(pageIndex, TOTAL_USER_PER_PAGE))
                .build();

        Page<UserImportEntity> result = elasticsearchTemplate.queryForPage(searchQuery, UserImportEntity.class);
        return result.getContent();
    }

    public int getTotalPageOfUserByCountry(String country) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(nestedQuery("profiles", QueryBuilders.matchQuery("profiles.GITHUB.location", country)))
                .withPageable(new PageRequest(0, TOTAL_USER_PER_PAGE))
                .build();

        Page<UserImportEntity> result = elasticsearchTemplate.queryForPage(searchQuery, UserImportEntity.class);
        return result.getTotalPages();
    }
}
