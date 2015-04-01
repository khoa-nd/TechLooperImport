package com.techlooper.main;

import com.techlooper.configuration.ElasticsearchUserImportConfiguration;
import com.techlooper.service.GithubAwardUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by phuonghqh on 3/30/15.
 */
public class GitHubAwardUserProfileMain {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubAwardUserProfileMain.class);

  public static void main(String[] args) throws Throwable {
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ElasticsearchUserImportConfiguration.class);
    GithubAwardUserService githubAwardUserService = applicationContext.getBean(GithubAwardUserService.class);

    final String[] countries = {"vietnam", "laos", "japan", "thailand", "myanmar", "singapore"};
//    final String[] countries = {"vietnam", "laos", "japan", "thailand", "myanmar", "singapore", "malaysia", "indonesia","cambodia", "australia", "china", "india", "korea", "taiwan",
//      "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
//      "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};
    for (String country : countries) {
      LOGGER.debug("Processing country {}", country);
      githubAwardUserService.crawlNewUser(country);
    }
  }
}
