package com.techlooper.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.MessageCallback;
import com.importio.api.clientlite.data.Query;
import com.importio.api.clientlite.data.QueryMessage;
import com.techlooper.utils.PropertyManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by phuonghqh on 1/22/15.
 */
public class GitHubImportJob {

  private static final Boolean[] hasNextPage = {Boolean.TRUE};

  private static Integer interval = 0;

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 2) {
      System.out.println("Usage example: mvn clean install -Dcountry=vietnam -DoutputDirectory=/techlooper/github/vietnam/");
      return;
    }

    String country = args[0];
    String output = args[1].endsWith("/") ? args[1] : args[1] + "/";

    UUID userId = UUID.fromString(PropertyManager.properties.getProperty("import.io.userId"));
    ImportIO client = new ImportIO(userId, PropertyManager.properties.getProperty("import.io.apiKey"));
    client.connect();

    CountDownLatch latch = new CountDownLatch(1);

    MessageCallback messageCallback = (query, message, progress) -> {
      if (message.getType() == QueryMessage.MessageType.MESSAGE) {
        HashMap<String, Object> resultMessage = (HashMap<String, Object>) message.getData();
        if (((List) resultMessage.get("results")).size() == 0 || resultMessage.containsKey("errorType")) {
          System.out.println("Error => Stop query: " + query.getInput().get("webpage/url"));
          synchronized (hasNextPage) {
            hasNextPage[0] = Boolean.FALSE;
          }
        }
        else {
          List<Object> results = (List<Object>) resultMessage.get("results");
          System.out.println("Result size: " + results.size());
          if (results.size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                synchronized (interval) {
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(output + interval++ + ".json"),
                      StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                    mapper.writeValue(writer, results);
                    writer.close();
                }
            }
            catch (IOException e) {
              e.printStackTrace(System.err);
            }
          }

        }
      }
      
      if (progress.isFinished()) {
        latch.countDown();
      }
    };

    Map<String, Object> queryInput = new HashMap<>();
    List<UUID> connectorGuids = Arrays.asList(UUID.fromString(PropertyManager.properties.getProperty("import.io.connector.github")));

    String urlTemplate = PropertyManager.properties.getProperty("github.user.searchTemplate");
    int currentYear = Calendar.getInstance(Locale.US).get(Calendar.YEAR);
    for (int year = 2007; year <= currentYear; ++year) {
      String createdFrom = year + "-01-01";
      String createdTo = year + "-06-30";
      doQuery(country, client, messageCallback, queryInput, connectorGuids, urlTemplate, createdFrom, createdTo);
      createdFrom = year + "-07-01";
      createdTo = year + "-12-31";
      doQuery(country, client, messageCallback, queryInput, connectorGuids, urlTemplate, createdFrom, createdTo);
    }

    latch.await();

    client.disconnect();
  }

  private static void doQuery(String country, ImportIO client, MessageCallback messageCallback,
                              Map<String, Object> queryInput, List<UUID> connectorGuids, String urlTemplate,
                              String createdFrom, String createdTo) throws IOException, InterruptedException {
    synchronized (hasNextPage) {
      hasNextPage[0] = Boolean.TRUE;
    }

    for (int i = 1; i < 101 && hasNextPage[0]; i++) {
      String queryUrl = String.format(urlTemplate, i, country, createdFrom, createdTo);
      System.out.println("Query using url: " + queryUrl);
      queryInput.put("webpage/url", queryUrl);
      Query query = new Query();
      query.setConnectorGuids(connectorGuids);
      query.setInput(queryInput);
      client.query(query, messageCallback);
      Thread.sleep(3000);
    }
  }
}
