package com.techlooper.imports;

import com.techlooper.utils.PropertyManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by phuonghqh on 1/22/15.
 */
public class GitHubImportJob {

  private static final Map<String, Boolean> hasNextPage = new HashMap<>();

//  private static final CountDownLatch latch = new CountDownLatch(1000000);

  public static void main(String[] args) throws IOException, InterruptedException {

    final String[] countries = {"vietnam", "japan", "thailand", "singapore", "malaysia", "indonasia", "australia", "china", "india", "korea", "taiwan",
            "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
            "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};

    int currentYear = Calendar.getInstance(Locale.US).get(Calendar.YEAR);

    for(String country : countries) {
      for (int year = 2007; year <= currentYear; ++year) {
        doQuery(country, year + "-01-01", year + "-06-30");
        Thread.sleep(2000);
        doQuery(country, year + "-07-01", year + "-12-31");
        Thread.sleep(2000);
      }
      Thread.sleep(5000);
    }
  }

  private static void doQuery(String country, String createdFrom, String createdTo) throws IOException, InterruptedException {
    String userId = PropertyManager.properties.getProperty("import.io.userId");
    String apiKey = PropertyManager.properties.getProperty("import.io.apiKey");
    String urlTemplate = PropertyManager.properties.getProperty("github.user.searchTemplate");
    String connectorId = PropertyManager.properties.getProperty("import.io.connector.github");

    String period = createdFrom + ".." + createdTo;
    boolean hasNext = true;

    for (int pageNumber = 1; pageNumber < 101 && hasNext; ) {
      String queryUrl = String.format(urlTemplate, pageNumber, country, createdFrom, createdTo);
      System.out.println("Query using url: " + queryUrl);

      HttpClient httpClient = HttpClients.createDefault();
      HttpPost p = new HttpPost(String.format("https://api.import.io/store/data/%s/_query?_user=%s&_apikey=%s",
              connectorId, userId, URLEncoder.encode(apiKey, "UTF-8")));

      String json = String.format("{ \"input\": {\"webpage/url\": \"%s\"} }", queryUrl);
      p.setEntity(new StringEntity(json, ContentType.create("application/json")));

      HttpResponse r = httpClient.execute(p);
      BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent(), "UTF-8"));
      StringBuilder content = new StringBuilder();
      String line = "";
      while ((line = rd.readLine()) != null) {
        content.append(line);
      }

      if (content.indexOf("I/O Error getting page.") > 0) {
        System.out.println("I/O Error getting page => try it again.");
        continue;
      } else {
        ++pageNumber;
      }

      int resultIndex = content.indexOf("\"results\":[");
      if (resultIndex < 0) {
        System.out.println("No Result => query: " + queryUrl);
        hasNext = false;
        continue;
      }
      System.out.println("Success => query: " + queryUrl);

      int endResultIndex = content.indexOf("],\"cookies\"", resultIndex);
      json = content.substring(resultIndex + "\"results\":[".length() - 1, endResultIndex + 1);
      String filename = country + "." + period + "." + (pageNumber - 1) + ".json";
      if ("[]".equals(json)) {
        hasNext = false;
        System.out.println("Result is empty => not write to file: " + filename);
        continue;
      }

      System.out.println("OK => wrote to file: " + filename);
      try {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        writer.write(json);
        writer.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
  }
}
