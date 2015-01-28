package com.techlooper.jobpostcrawlers;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by chris on 1/23/15.
 */
public class CareerLinkCrawlerTest {

    @Test
    @Ignore("No Need to test for now")
    public void testCrawl() throws Exception {
        CareerLinkCrawler crawler = new CareerLinkCrawler();
        for(int counter = 88; counter < 89; counter++) {
            crawler.crawl(counter);
        }
    }
}
