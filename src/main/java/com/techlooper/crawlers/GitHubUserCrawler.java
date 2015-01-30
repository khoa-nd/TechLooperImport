package com.techlooper.crawlers;

import com.techlooper.utils.PropertyManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 1/22/15.
 */
public class GitHubUserCrawler {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserCrawler.class);

  private static PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();

  private static enum DIVISION {NOT, BINARY}

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

      String userId = PropertyManager.properties.getProperty("githubUserCrawler.import.io.userId");
      String apiKey = PropertyManager.properties.getProperty("githubUserCrawler.import.io.apiKey");
      String urlTemplate = PropertyManager.properties.getProperty("githubUserCrawler.user.searchTemplate");
      String connectorId = PropertyManager.properties.getProperty("githubUserCrawler.import.io.connector.github");
      String outputDirectory = PropertyManager.properties.getProperty("githubUserCrawler.outputDirectory");

      String period = String.format("%s..%s", createdFrom, createdTo);
      boolean tryAgain = true;
      File dir = new File(outputDirectory);
      if (!dir.exists()) {
        dir.mkdirs();
      }

      String filename = String.format("%s%s.%s.%d.json", outputDirectory, country, period, pageNumber);
      File f = new File(filename);
      if (f.exists() && !f.isDirectory()) {
        return;
      }

      try {
        while (tryAgain) {
          String queryUrl = String.format(urlTemplate, pageNumber, country, createdFrom, createdTo);
          LOGGER.debug("Query users using url: " + queryUrl);

//          pool.
//            HttpClients.custom().setConnectionManager(clientConnectionManager);
          HttpClient httpClient = HttpClients.custom().setConnectionManager(clientConnectionManager).build();
//          HttpClient httpClient = HttpClients.createDefault();

          HttpPost p = new HttpPost(String.format("https://api.import.io/store/data/%s/_query?_user=%s&_apikey=%s",
            connectorId, userId, URLEncoder.encode(apiKey, "UTF-8")));

          String json = String.format("{ \"input\": {\"webpage/url\": \"%s\"} }", queryUrl);
          p.setEntity(new StringEntity(json, ContentType.create("application/json")));

          HttpResponse r = null;

          try {
            r = httpClient.execute(p);
          }
          catch (Exception e) {
            LOGGER.error("ERROR", e);
            continue;
          }

          String content = EntityUtils.toString(r.getEntity());
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

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
//    final String[] countries = {"vietnam"x, "japan"x, "thailand"x, "singapore"x, "malaysia"x, "indonasia"x, "australia"x, "china"x, "india", "korea", "taiwan",
//      "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
//      "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};


    final String[] countries = {"china"};

    ExecutorService executor = Executors.newFixedThreadPool(20);

    for (String country : countries) {
      doCountry(country, executor);
    }

    executor.shutdown();
    LOGGER.debug("DONE DONE DONE!!!!!");
  }

  private static DateTime divRange(DateTime from, DateTime to, DIVISION div) {
    if (div == DIVISION.NOT) {
      return null;
    }

    DateTime right = null;
    if (from.getYear() < to.getYear()) {
      right = to.minusYears((to.getYear() - from.getYear()) / 2 + 1);
    }
    else if (from.getMonthOfYear() < to.getMonthOfYear()) {
      right = to.minusMonths((to.getMonthOfYear() - from.getMonthOfYear()) / 2 + 1);
    }
    else if (from.getDayOfMonth() < to.getDayOfMonth()) {
      right = to.minusDays((to.getDayOfMonth() - from.getDayOfMonth()) / 2 + 1);
    }
    return right;
  }

  private static void doCountry(String country, ExecutorService executor) throws IOException, InterruptedException, ParseException {
    int currentYear = Calendar.getInstance(Locale.US).get(Calendar.YEAR);
    boolean stop = false;
    String fromTo = String.format("2007-01-01..%d-12-31", currentYear);
    DIVISION div = DIVISION.NOT;
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd").withLocale(Locale.US);

    do {
      DateTime from = dateTimeFormatter.parseDateTime(fromTo.split("\\..")[0]);
      DateTime to = dateTimeFormatter.parseDateTime(fromTo.split("\\..")[1]);

      DateTime toDiv = divRange(from, to, div);
      if (toDiv == null) {
        LOGGER.debug("From {} - To {} cant be divided.", from, to);
      }
      else {
        to = toDiv;
      }

      String count = count(country, String.format("%d-%02d-%02d", from.getYear(), from.getMonthOfYear(), from.getDayOfMonth()),
        String.format("%d-%02d-%02d", to.getYear(), to.getMonthOfYear(), to.getDayOfMonth()));

      Integer totalUsers = Integer.parseInt(count.split(",")[0]);
      Integer maxPageNumber = Integer.parseInt(count.split(",")[1]);

      LOGGER.debug("Total users are {} , paging to {} ", totalUsers, maxPageNumber);
      if (from.isEqual(to)) {
        totalUsers = 1000;
        maxPageNumber = 100;
        LOGGER.debug("  => Not min enough but we should start crawling.");
      }

      if (totalUsers <= 1000) {
        LOGGER.debug("  => Start crawling");
        //////////////////////////
        // start crawling here //
        /////////////////////////
        for (int pageNumber = 1; pageNumber <= maxPageNumber; ++pageNumber) {
          executor.execute(new JobQuery(country, from.toString("yyyy-MM-dd"), to.toString("yyyy-MM-dd"), pageNumber));
        }

        String lastDate = currentYear + "-12-31";
        stop = lastDate.equals(to.toString("yyyy-MM-dd"));

        div = DIVISION.NOT;
        from = to.plusDays(1);
        to = dateTimeFormatter.parseDateTime(lastDate);
      }
      else {
        div = DIVISION.BINARY;
        LOGGER.debug("  => Not min enough");
      }

      fromTo = String.format("%s..%s", from.toString("yyyy-MM-dd"), to.toString("yyyy-MM-dd"));
    }
    while (!stop);
  }

  private static String count(String country, String createdFrom, String createdTo) throws IOException, InterruptedException {
    String userId = PropertyManager.properties.getProperty("githubUserCrawler.import.io.userId");
    String apiKey = PropertyManager.properties.getProperty("githubUserCrawler.import.io.apiKey");
    String urlTemplate = PropertyManager.properties.getProperty("githubUserCrawler.user.searchTemplate");
    String connectorId = PropertyManager.properties.getProperty("githubUserCrawler.import.io.connector.github.totalUsers");

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
      p.setEntity(new StringEntity(json, ContentType.create("application/json", "UTF-8")));

      HttpResponse r = null;
      try {
        r = httpClient.execute(p);
      }
      catch (Exception err) {
        LOGGER.error("ERROR", err);
        continue;
      }

      BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent(), "UTF-8"));
      StringBuilder content = new StringBuilder();
      String line = "";
      while ((line = rd.readLine()) != null) {
        content.append(line);
      }
//      System.out.println(content);

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

      LOGGER.debug("Extracting max-page-number number => query: {}", queryUrl);
      maxPageNumber = count / 10 + (count % 10 > 0 ? 1 : 0);//Integer.valueOf(content.substring(maxPageNumberIndex + "\"max_page_number\":[".length() - 1, maxPageNumberAt));
    }

    LOGGER.debug("Total user for the period of {}..{}: {}", createdFrom, createdTo, count);
    return count + "," + maxPageNumber;
  }
}
