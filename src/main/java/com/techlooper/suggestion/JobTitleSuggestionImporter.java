package com.techlooper.suggestion;

import com.techlooper.configuration.ElasticsearchVietnamworksConfiguration;
import com.techlooper.configuration.VietnamworksDatabaseConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by NguyenDangKhoa on 6/1/15.
 */
@Service
public class JobTitleSuggestionImporter {

    private static final int MAX_ITEM_PER_PAGE = 1000;

    @Resource
    private JobTitleReader jobTitleReader;

    @Resource
    private JobTitleSuggestionIndexer jobTitleSuggestionIndexer;

    public void importJobTitle() throws Exception {
        int totalNumberOfUsers = jobTitleReader.getTotalNumberOfRegistrationUsers();
        int totalNumberOfPages = totalNumberOfUsers % MAX_ITEM_PER_PAGE == 0 ?  totalNumberOfUsers / MAX_ITEM_PER_PAGE :
                totalNumberOfUsers / MAX_ITEM_PER_PAGE + 1;
        int pageIndex = 0;
        while (pageIndex < totalNumberOfPages) {
            List<String> jobTitles = jobTitleReader.readJobTitle(pageIndex * MAX_ITEM_PER_PAGE, MAX_ITEM_PER_PAGE);
            jobTitleSuggestionIndexer.index(jobTitles);
            pageIndex++;
            Thread.sleep(3000);
        }
    }
}
