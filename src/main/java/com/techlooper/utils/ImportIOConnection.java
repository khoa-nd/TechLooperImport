package com.techlooper.utils;

import com.mashape.unirest.http.Unirest;
import com.techlooper.pojo.GithubAwardResponse;

import java.net.URLEncoder;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
public class ImportIOConnection {

    public static GithubAwardResponse fetchContent(GithubAwardRequest extractor) throws Exception {
        String encodedAPIKey = URLEncoder.encode(extractor.getApiKey(), "UTF-8");
        String encodedInputUrl = URLEncoder.encode(extractor.getInputUrl(), "UTF-8");

        String requestUrl = String.format("https://api.import.io/store/data/%s/_query?_user=%s&_apikey=%s&input/webpage/url=%s",
                extractor.getConnectorId(), extractor.getUserId(), encodedAPIKey, encodedInputUrl);
        String responseContent = Unirest.get(requestUrl).asString().getBody();
        return JsonUtils.toPOJO(responseContent, GithubAwardResponse.class).get();
    }
}
