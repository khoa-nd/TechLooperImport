package com.techlooper.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

/**
 * Created by phuonghqh on 1/27/15.
 */
public class Utils {

  public static int postJsonString(String url, String jsonString) throws IOException {
    HttpClient httpClient = HttpClients.createDefault();
    HttpPost post = new HttpPost(url);
    post.setEntity(new StringEntity(jsonString, ContentType.create("application/json")));
    HttpResponse response = httpClient.execute(post);
    return response.getStatusLine().getStatusCode();
  }
}
