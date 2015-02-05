package com.techlooper.crawlers;

import com.techlooper.pojo.FootPrint;
import com.techlooper.utils.PropertyManager;
import com.techlooper.utils.Utils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phuonghqh on 1/22/15.
 */
public class GitHubUserCrawler {

  private static Logger LOGGER = LoggerFactory.getLogger(GitHubUserCrawler.class);

//  private static PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();

  private static enum DIVISION {NOT, BINARY}

  ;

  private static String outputDirectory = PropertyManager.getProperty("githubUserCrawler.outputDirectory");

  private static String footPrintFilePath = PropertyManager.getProperty("footPrintFile");//String.format("%sgithub.footprint.json", outputDirectory);

  private static String userId = PropertyManager.getProperty("githubUserCrawler.import.io.userId");

  private static String apiKey = PropertyManager.getProperty("githubUserCrawler.import.io.apiKey");

  private static String urlTemplate = PropertyManager.getProperty("githubUserCrawler.user.searchTemplate");

  private static String totalUsersConnectorId = PropertyManager.getProperty("githubUserCrawler.import.io.connector.github.totalUsers");

  private static String userConnectorId = PropertyManager.getProperty("githubUserCrawler.import.io.connector.github");

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    Utils.sureDirectory(outputDirectory);
//    final String[] countries = {"vietnam"x, "japan"x, "thailand"x, "singapore"x, "malaysia"x, "indonesia"x, "australia"x, "china"x, "india"x, "korea", "taiwan",
//      "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
//      "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};

    final String[] countries = {"vietnam"};

    FootPrint footPrint = Utils.readFootPrint(footPrintFilePath);

    ExecutorService executor = Executors.newFixedThreadPool(20);

    for (String country : countries) {
      doCountry(country, executor, footPrint);
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
      LOGGER.debug("Divide YEAR");
      right = to.withYear((from.getYear() + to.getYear()) / 2)
        .monthOfYear().withMaximumValue()
        .dayOfMonth().withMaximumValue();
    }
    else if (from.getMonthOfYear() < to.getMonthOfYear()) {
      LOGGER.debug("Divide MONTH");
      right = to.withMonthOfYear((from.getMonthOfYear() + to.getMonthOfYear()) / 2).dayOfMonth().withMaximumValue();
    }
    else if (from.getDayOfMonth() < to.getDayOfMonth()) {
      LOGGER.debug("Divide DAY");
      right = to.withDayOfMonth((from.getDayOfMonth() + to.getDayOfMonth()) / 2);
    }
    return right;
  }

  private static void doCountry(String country, ExecutorService executor, FootPrint footPrint) throws IOException, InterruptedException, ParseException {
    int currentYear = Calendar.getInstance(Locale.US).get(Calendar.YEAR);
    boolean stop = false;

    String fromTo = Optional.ofNullable(footPrint.getCrawlers().get(country))
      .orElse(String.format("2007-01-01..%d-12-31", currentYear));

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

      Utils.writeFootPrint(footPrintFilePath,
        FootPrint.FootPrintBuilder.footPrint(footPrint).withCrawler(country, fromTo).build());
      fromTo = String.format("%s..%s", from.toString("yyyy-MM-dd"), to.toString("yyyy-MM-dd"));
    }
    while (!stop);
  }

  private static String count(String country, String createdFrom, String createdTo) throws IOException, InterruptedException {
    final Integer[] count = {0};
    final Integer[] maxPageNumber = {1};

    String queryUrl = String.format(urlTemplate, 1, country, createdFrom, createdTo);
    LOGGER.debug("Do import.io query {}", queryUrl);
    Utils.doIIOQuery(totalUsersConnectorId, userId, apiKey, queryUrl, countInfo -> {
      LOGGER.debug("Result from query {} is {}", queryUrl, countInfo);
      countInfo = countInfo.get(0);
      count[0] = countInfo.get("total_users").asInt();
      maxPageNumber[0] = count[0] / 10 + (count[0] % 10 > 0 ? 1 : 0);
    });

    LOGGER.debug("Total user for the period of {}..{}: {} , max-page: ", createdFrom, createdTo, count[0], maxPageNumber[0]);
    return count[0] + "," + maxPageNumber[0];
  }

  private static class JobQuery implements Runnable {

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
      String period = String.format("%s..%s", createdFrom, createdTo);
      String filename = String.format("%s%s.%s.%d.json", outputDirectory, country, period, pageNumber);
      File f = new File(filename);
      if (f.exists() && !f.isDirectory()) {
        return;
      }

      String queryUrl = String.format(urlTemplate, pageNumber, country, createdFrom, createdTo);
      LOGGER.debug("Do import.io query {}", queryUrl);
      Utils.doIIOQuery(userConnectorId, userId, apiKey, queryUrl, users -> {
        LOGGER.debug("Result from query {} is {}", queryUrl, users);
        try {
          LOGGER.debug("OK => Write to file: " + filename);
          Utils.writeToFile(users, filename);
        }
        catch (IOException e) {
          LOGGER.error("ERROR", e);
        }
      });
    }
  }
}
