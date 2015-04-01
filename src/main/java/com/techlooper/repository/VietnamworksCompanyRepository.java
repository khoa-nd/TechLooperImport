package com.techlooper.repository;

import com.techlooper.entity.CompanyEntity;
import com.techlooper.entity.VietnamworksCompanyEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by khoanguyendang on 01/04/15.
 */
@Repository
public interface VietnamworksCompanyRepository extends ElasticsearchRepository<VietnamworksCompanyEntity, Long> {
}
