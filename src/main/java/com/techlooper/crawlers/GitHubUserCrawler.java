package com.techlooper.crawlers;

import com.techlooper.utils.PropertyManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 1/22/15.
 */
public class GitHubUserCrawler {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserCrawler.class);

  private static class JobQuery extends Thread {

    private final String country;
    private final String createdFrom;
    private final String createdTo;
    private Integer pageNumber;

    public JobQuery(String country, String createdFrom, String createdTo, Integer pageNumber) {
      this.country = country;
      this.createdFrom = createdFrom;
      this.createdTo = createdTo;
      this.pageNumber = pageNumber;
    }

    public void run() {
      LOGGER.debug("Starting new thread...");

      String userId = PropertyManager.properties.getProperty("import.io.userId");
      String apiKey = PropertyManager.properties.getProperty("import.io.apiKey");
      String urlTemplate = PropertyManager.properties.getProperty("github.user.searchTemplate");
      String connectorId = PropertyManager.properties.getProperty("import.io.connector.github");
      String outputDirectory = PropertyManager.properties.getProperty("githubUserCrawler.outputDirectory");

      String period = String.format("%s..%s", createdFrom, createdTo);
      boolean tryAgain = true;
      String filename = String.format("%s%s.%s.%d.json", outputDirectory, country, period, pageNumber);
      File f = new File(filename);
      if (f.exists() && !f.isDirectory()) {
        return;
      }

      try {
        while (tryAgain) {
          String queryUrl = String.format(urlTemplate, pageNumber, country, createdFrom, createdTo);
          LOGGER.debug("Query users using url: " + queryUrl);

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
            LOGGER.debug("I/O Error getting page => try it again.");
            continue;
          }

          int resultIndex = content.indexOf("\"results\":[");
          if (resultIndex < 0) {
            LOGGER.debug("No Result => query: " + queryUrl);
            continue;
          }
          LOGGER.debug("Success => query: " + queryUrl);

          int endResultIndex = content.indexOf("],\"cookies\"", resultIndex);
          json = content.substring(resultIndex + "\"results\":[".length() - 1, endResultIndex + 1);
          if ("[]".equals(json)) {
            LOGGER.debug("Result is empty => not write to file: " + filename);
            continue;
          }

          LOGGER.debug("OK => wrote to file: " + filename);
          try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            writer.write(json);
            writer.close();
            tryAgain = false;
          }
          catch (IOException e) {
            LOGGER.error("Can not write file", e);
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("Can not do crawler", e);
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
//    final String[] countries = {"vietnam", "japan", "thailand", "singapore", "malaysia", "indonasia", "australia", "china", "india", "korea", "taiwan",
//      "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
//      "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};


    final String[] countries = {"japan"};

    int currentYear = Calendar.getInstance(Locale.US).get(Calendar.YEAR);
    ExecutorService executor = Executors.newFixedThreadPool(20);

    for (String country : countries) {
      for (int year = 2007; year <= currentYear; ++year) {
        int startMonth = 0;
        int endMonth = 11;
        int endDay = 0;
        Integer totalUsers = 1001;
        Integer maxPageNumber = 1;

        while (startMonth <= 11) {
          while (totalUsers > 1000) {
            Calendar monthD = Calendar.getInstance(Locale.US);
            monthD.set(year, endMonth, 1);

            endDay = monthD.getActualMaximum(Calendar.DAY_OF_MONTH);
            String result = count(country, String.format("%d-%02d-01", year, startMonth + 1), String.format("%d-%02d-%02d", year, endMonth + 1, endDay));
            totalUsers = Integer.parseInt(result.split(",")[0]);
            maxPageNumber = Integer.parseInt(result.split(",")[1]);
            LOGGER.debug("Max page number: {}", maxPageNumber);
            if (totalUsers > 1000) {
              LOGGER.debug("Total users are {} , paging to {} => Not min enough", totalUsers, maxPageNumber);
              endMonth = (startMonth + endMonth) / 2;
            }
            else {
              LOGGER.debug("OK => Start crawling");
              break;
            }
          }


          if (totalUsers == 0) {
            totalUsers = 1001;
            break;
          }

          // ok period, start crawling
          if (totalUsers > 0) {
            for (int pageNumber = 1; pageNumber <= maxPageNumber; ++pageNumber) {
              executor.execute(new JobQuery(country, String.format("%d-%02d-01", year, startMonth + 1),
                String.format("%d-%02d-%02d", year, endMonth + 1, endDay), pageNumber));
            }
          }

          if (endMonth >= 11) {
            break;
          }

          totalUsers = 1001;
          startMonth = endMonth + 1;
          endMonth = 11;
        }
      }
    }
  }

  private static String count(String country, String createdFrom, String createdTo) throws IOException, InterruptedException {
    String userId = PropertyManager.properties.getProperty("import.io.userId");
    String apiKey = PropertyManager.properties.getProperty("import.io.apiKey");
    String urlTemplate = PropertyManager.properties.getProperty("github.user.searchTemplate");
    String connectorId = PropertyManager.properties.getProperty("import.io.connector.github.totalUsers");

    boolean tryAgain = true;
    Integer count = 0;
    Integer maxPageNumber = 1;

    while (tryAgain) {
      String queryUrl = String.format(urlTemplate, 1, country, createdFrom, createdTo);
      LOGGER.debug("Catch total using url: " + queryUrl);

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
        LOGGER.debug("I/O Error getting page => try it again.");
        continue;
      }
      else {
        tryAgain = false;
      }

      int totalUsersIndex = content.indexOf("\"total_users\":");
      if (totalUsersIndex < 0) {
        LOGGER.debug("No total => query: " + queryUrl);
        continue;
      }
      LOGGER.debug("Extracting total-users number => query: {}", queryUrl);

      int countAt = content.indexOf(".0,", totalUsersIndex);
      count = Integer.valueOf(content.substring(totalUsersIndex + "\"total_users\":[".length() - 1, countAt));

      int maxPageNumberIndex = content.indexOf("\"max_page_number\":");
      LOGGER.debug("Extracting max-page-number number => query: {}", queryUrl);
      int maxPageNumberAt = content.indexOf(".0,", maxPageNumberIndex);
      maxPageNumber = Integer.valueOf(content.substring(maxPageNumberIndex + "\"max_page_number\":[".length() - 1, maxPageNumberAt));
    }

    LOGGER.debug("Total user for the period of {}..{}: {}", createdFrom, createdTo, count);
    return count + "," + maxPageNumber;
  }
}
