package com.techlooper.service;

import com.techlooper.entity.UserImportEntity;
import com.techlooper.pojo.GithubAwardResponse;
import com.techlooper.pojo.SocialProvider;
import com.techlooper.repository.UserImportRepository;
import com.techlooper.utils.GithubAwardRequest;
import com.techlooper.utils.ImportIOConnection;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
@Service
public class GithubAwardUserService {

    private static Logger LOGGER = LoggerFactory.getLogger(GithubAwardUserService.class);

    private static final int TOTAL_USER_PER_PAGE = 50;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Resource
    private UserImportRepository userImportRepository;

    @Resource
    private GithubAwardDataProcessor githubAwardDataProcessor;

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

    public int enrichUserImportByCountry(String country, GithubAwardRequest extractor) throws Exception {
        int totalPage = getTotalPageOfUserByCountry(country);
        int pageIndex = 0;
        int countSuccess = 0;
        while (pageIndex < totalPage) {
            List<UserImportEntity> userImportEntities = getUserByCountry(country, pageIndex);
            for (UserImportEntity userImportEntity : userImportEntities) {
                Map<String, Object> profile = (Map<String, Object>) userImportEntity.getProfiles().get(SocialProvider.GITHUB);

                if (profile == null) {
                    continue;
                }

                String githubUsername = (String) profile.get("username");
                extractor.setInputUrl(String.format(extractor.getInputUrl(), githubUsername));
                try {
                    GithubAwardResponse githubAwardResponse = ImportIOConnection.fetchContent(extractor);

                    if (StringUtils.isNotEmpty(githubAwardResponse.getError())) {
                        LOGGER.error("Enrich User " + githubUsername + " Fail Due To ImportID. " + githubAwardResponse.getError());
                        continue;
                    } else if (githubAwardResponse.getResults().isEmpty()) {
                        LOGGER.error("User " + githubUsername + " Doesn't Have Information.");
                        continue;
                    }

                    githubAwardDataProcessor.process(userImportEntity, githubAwardResponse);
                    userImportRepository.save(userImportEntity);
                    countSuccess++;
                    LOGGER.info("User " + githubUsername + " Has Been Enriched Successfully.");
                } catch (Exception ex) {
                    LOGGER.error("Enrich User " + githubUsername + " Fail", ex);
                } finally {
                    Thread.sleep(2000);
                }
            }
            pageIndex++;
        }
        return countSuccess;
    }
}
