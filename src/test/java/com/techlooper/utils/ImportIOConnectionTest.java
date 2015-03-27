package com.techlooper.utils;

import com.techlooper.pojo.GithubAwardResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class ImportIOConnectionTest {

    @Test
    public void testFetchContent() throws Exception {
        String inputUrl = "http://github-awards.com/users/chrisshayan";
        GithubAwardRequest githubAwardRequestInfo = new GithubAwardRequest("5fc2f5f9-026a-46f2-bb91-00d84ab8a2fd",
                "4c46b4b2-e818-4462-bf47-bf89165c3ace",
                "CZs+sxsZFF/u3dBPO6Y+yYww1AWBRbIM20cWB5f0HXhcgGl36PmpjzRgwmcDA6eSUMkWmRQ9CiNIwGK2vfuWqg==", inputUrl);

        GithubAwardResponse response = ImportIOConnection.fetchContent(githubAwardRequestInfo);
        assertNull(response.getError());
        assertTrue(response.getResults().size() > 0);
    }

    @Test
    public void testFetchError() throws Exception {
        String inputUrl = "http://github-awards.com/users/chrisshayan";
        GithubAwardRequest githubAwardRequestInfo = new GithubAwardRequest("5fc2f5f9-026a-46f2-bb91-00d84ab8a2fd",
                "4c46b4b2-e818-4462-bf47-bf89165c3ace",
                "WRONG API KEY", inputUrl);
        GithubAwardResponse response = ImportIOConnection.fetchContent(githubAwardRequestInfo);
        assertNotNull(response.getError());
    }
}