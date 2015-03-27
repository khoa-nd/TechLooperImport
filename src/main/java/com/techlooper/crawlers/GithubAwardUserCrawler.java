package com.techlooper.crawlers;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
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
//TODO : At the moment we deploy to server to run once to see how it will work. If nothing, we should implement
//1. Install it as a cron job to run everyweek
//2. Auto retry on users have been enriched fail due to ImportIO error
public class GithubAwardUserCrawler {

    private static Logger LOGGER = LoggerFactory.getLogger(GithubAwardUserCrawler.class);

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ElasticsearchUserImportConfiguration.class);
        GithubAwardUserService githubAwardUserService =
                applicationContext.getBean("githubAwardUserService", GithubAwardUserService.class);

        Environment environment = applicationContext.getEnvironment();
        String connectorId = environment.getProperty("githubAwardRankingEnricher.connectorId");
        String userId = environment.getProperty("githubAwardRankingEnricher.userId");
        String apiKey = environment.getProperty("githubAwardRankingEnricher.apiKey");
        String inputUrl = environment.getProperty("githubAwardRankingEnricher.inputUrl");

        final String[] countries = {"vietnam", "japan", "thailand", "myanmar", "singapore", "malaysia", "indonesia","cambodia", "australia", "china", "india", "korea", "taiwan",
            "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
            "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};
        GithubAwardRequest githubAwardRequest = new GithubAwardRequest(connectorId, userId, apiKey, inputUrl);

        for (String country : countries) {
            int countSuccess = githubAwardUserService.enrichUserImportByCountry(country, githubAwardRequest);
            LOGGER.info("Enrich " + countSuccess + " users in " + country + " successfully");
        }
    }
}
