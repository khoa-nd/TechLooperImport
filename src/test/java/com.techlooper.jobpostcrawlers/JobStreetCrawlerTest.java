package com.techlooper.jobpostcrawlers;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by chris on 1/23/15.
 */
public class JobStreetCrawlerTest {

    @Test
    @Ignore("No Need to test for now")
    public void testCrawl() throws Exception {
        JobStreetCrawler crawler = new JobStreetCrawler();
        for (int counter = 200; counter < 229; counter++) {
            crawler.crawl(counter);
        }
    }
}
