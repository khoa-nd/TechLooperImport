package com.techlooper.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.MessageCallback;
import com.importio.api.clientlite.data.Query;
import com.importio.api.clientlite.data.QueryMessage;
import com.techlooper.utils.PropertyManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by phuonghqh on 1/22/15.
 */
public class GitHubImportJob {

  private static final Boolean[] hasNextPage = {Boolean.TRUE};

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 2) {
      System.out.println("Usage example: mvn clean install -Dcountry=vietnam -Doutput=vietnam-users.json");
      return;
    }

    String country = args[0];
    String output = args[1];

    UUID userId = UUID.fromString(PropertyManager.properties.getProperty("import.io.userId"));
    ImportIO client = new ImportIO(userId, PropertyManager.properties.getProperty("import.io.apiKey"));
    client.connect();

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> dataRows = new ArrayList<>();
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
          dataRows.addAll(results);
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

    System.out.println("All data received:");
    System.out.println(dataRows.size());
    System.out.println(dataRows);

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(new File(output), dataRows);
    System.exit(0);
  }

  private static void doQuery(String country, ImportIO client, MessageCallback messageCallback,
                              Map<String, Object> queryInput, List<UUID> connectorGuids, String urlTemplate,
                              String createdFrom, String createdTo) throws IOException {
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
    }
  }
}
