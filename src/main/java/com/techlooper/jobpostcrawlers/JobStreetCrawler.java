package com.techlooper.jobpostcrawlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.MessageCallback;
import com.importio.api.clientlite.data.Query;
import com.importio.api.clientlite.data.QueryMessage;
import com.techlooper.utils.PropertyManager;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by chris on 1/23/15.
 */
public class JobStreetCrawler {

    public void crawl(final int pageNumber) throws IOException, InterruptedException {
        /**
         * Because import.io queries are asynchronous, for this simple script we will use a {@see CountdownLatch}
         * to stop the script from exiting before all of our queries are returned. We are doing one query in this
         * example so we initialise it with "1"
         */
        final CountDownLatch latch = new CountDownLatch(1);

        final List<Object> dataRows = new ArrayList<Object>();

        /**
         * In order to receive the data from the queries we issue, we need to define a callback method
         * This method will receive each message that comes back from the queries, and we can take that
         * data and store it for use in our app. {@see MessageCallback}
         */
        MessageCallback messageCallback = (query1, message, progress) -> {
            if (message.getType() == QueryMessage.MessageType.MESSAGE) {
                HashMap<String, Object> resultMessage = (HashMap<String, Object>) message.getData();
                if (resultMessage.containsKey("errorType")) {
                    // In this case, we received a message, but it was an error from the external service
                    System.err.println("Got an error!");
                    System.err.println(message);
                } else {
                    // We got a message and it was not an error, so we can process the data
                    System.out.println("Got data!");
                    System.out.println(message);
                    // Save the data we got in our dataRows variable for later
                    List<Object> results = (List<Object>) resultMessage.get("results");
                    System.out.println("results = " + results);

                    ObjectMapper mapper = new ObjectMapper();

                    File jobStreetFile = new File("jobStreet.json");
                    try {
                        BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("jobStreet.page%s.json", pageNumber)),
                                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                        mapper.writeValue(writer, results);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }


                    dataRows.addAll(results);
                }
            }
            // When the query is finished, countdown the latch so the program can continue when everything is done
            if (progress.isFinished()) {
                latch.countDown();
            }
        };

        // Generate the connector GUID we are going to query
        List<UUID> connectorGuids = Arrays.asList(UUID.fromString(PropertyManager.properties.getProperty("import.io.jobs.connector.jobstreet")));
        UUID userId = UUID.fromString(PropertyManager.properties.getProperty("import.io.jobs.userId.chris"));
        ImportIO client = new ImportIO(userId, PropertyManager.properties.getProperty("import.io.jobs.apiKey.chris"));
        client.connect();


        final String urlTemplate = PropertyManager.properties.getProperty("jobstreet.queryTemplate");
        // Generate a map of inputs we wish to send
        final String queryUrl = String.format(urlTemplate, pageNumber);
        System.out.println("Query using url: " + queryUrl);

        final Map<String, Object> queryInput = new HashMap<>();
        queryInput.put("webpage/url", queryUrl);
        // Generate a query object, and specify the connector GUIDs and the input
        Query query = new Query();
        query.setConnectorGuids(connectorGuids);
        query.setInput(queryInput);
        client.query(query, messageCallback);

        // Wait on the query to be completed
        latch.await();

        // It is best practice to disconnect when you are finished sending queries and getting data - it allows us to
        // clean up resources on the client and the server
        client.disconnect();

    }
}
