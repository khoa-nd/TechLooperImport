package com.techlooper.repository.vietnamworks;

import com.techlooper.entity.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by khoanguyendang on 01/04/15.
 */
@Repository
public interface JobSearchResultRepository extends ElasticsearchRepository<JobEntity, String> {
}
