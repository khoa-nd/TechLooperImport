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

    public static void main(String[] args) throws IOException, InterruptedException {
        final String[] countries = {"thailand", "singapore", "malaysia", "indonasia", "australia", "china", "india", "korea", "taiwan",
                "spain", "ukraine", "poland", "russia", "bulgaria", "turkey", "greece", "serbia", "romania", "belarus", "lithuania", "estonia",
                "italy", "portugal", "colombia", "brazil", "chile", "argentina", "venezuela", "bolivia", "mexico"};

        for (final String country : countries) {
            crawlPerCountry(country);
            Thread.sleep(3000);
        }
    }

    private static void crawlPerCountry(String country) throws IOException, InterruptedException {
        UUID userId = UUID.fromString(PropertyManager.properties.getProperty("import.io.userId"));
        ImportIO client = new ImportIO(userId, PropertyManager.properties.getProperty("import.io.apiKey"));
        client.connect();

        CountDownLatch latch = new CountDownLatch(1);

        MessageCallback messageCallback = (query, message, progress) -> {
            if (message.getType() == QueryMessage.MessageType.MESSAGE) {
                HashMap<String, Object> resultMessage = (HashMap<String, Object>) message.getData();
                String queryUrl = query.getInput().get("webpage/url").toString();
                if (((List) resultMessage.get("results")).size() == 0 || resultMessage.containsKey("errorType")) {
                    System.out.println("Error => Stop query: " + queryUrl);
                } else {
                    List<Object> results = (List<Object>) resultMessage.get("results");

                    System.out.println("Success => query: " + queryUrl);
                    System.out.println("Result size: " + results.size());
                    if (results.size() > 0) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String page = queryUrl.substring(queryUrl.indexOf("p=") + "p=".length(), queryUrl.indexOf("p=") + 3);
                            String period = queryUrl.substring(queryUrl.indexOf("created:") + "created:".length(), queryUrl.indexOf("created:") + 30);
                            BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("%s.%s.%s.json", country, period, page)),
                                    StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                            mapper.writeValue(writer, results);
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }

            if (progress.isFinished()) {
                System.out.println("Process is finished.");
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

//    latch.await();

        client.disconnect();
    }

    private static void doQuery(String country, ImportIO client, MessageCallback messageCallback,
                                Map<String, Object> queryInput, List<UUID> connectorGuids, String urlTemplate,
                                String createdFrom, String createdTo) throws IOException, InterruptedException {
        for (int pageNumber = 1; pageNumber < 101 ; pageNumber++) {
            String queryUrl = String.format(urlTemplate, pageNumber, country, createdFrom, createdTo);
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
