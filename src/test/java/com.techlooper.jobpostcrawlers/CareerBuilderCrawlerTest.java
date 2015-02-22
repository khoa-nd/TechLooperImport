package com.techlooper.jobpostcrawlers;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by chris on 1/23/15.
 */
public class CareerBuilderCrawlerTest {

    @Test
    @Ignore("No Need to test for now")
    public void testCrawl() throws Exception {
        CareerBuilderCrawler crawler = new CareerBuilderCrawler();
        for (int counter = 1; counter < 53; counter++) {
            crawler.crawl(counter);
        }
    }
}
