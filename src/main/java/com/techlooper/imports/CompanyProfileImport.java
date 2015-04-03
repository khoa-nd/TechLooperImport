package com.techlooper.imports;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import com.techlooper.service.CompanyService;
import com.techlooper.service.GithubAwardUserService;
import com.techlooper.utils.GithubAwardRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
public class CompanyProfileImport {

    private static Logger LOGGER = LoggerFactory.getLogger(CompanyProfileImport.class);

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ElasticsearchUserImportConfiguration.class,
                ElasticsearchVietnamworksConfiguration.class);
        CompanyService companyService = applicationContext.getBean("companyService", CompanyService.class);

        int successCompanyAdd = companyService.runImportCompany();
        companyService.removeNonITIndustryInCompanyProfile();
        companyService.removeDuplicatedSkillInCompanyProfile();
        LOGGER.info("Done Import Company " + successCompanyAdd);
    }
}
