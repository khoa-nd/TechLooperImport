package com.techlooper.repository;

import com.techlooper.entity.UserImportEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImportRepository extends ElasticsearchRepository<UserImportEntity, String> {

}