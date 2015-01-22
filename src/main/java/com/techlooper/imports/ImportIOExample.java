package com.techlooper.imports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.MessageCallback;
import com.importio.api.clientlite.data.Progress;
import com.importio.api.clientlite.data.Query;
import com.importio.api.clientlite.data.QueryMessage;
import com.importio.api.clientlite.data.QueryMessage.MessageType;

/**
 * An example class for making use of the import.io Java client library
 * 
 * @author dev@import.io
 * @see https://github.com/import-io/importio-client-libs/tree/master/java
 */
public class ImportIOExample {
	
  public static void main(String[] args) throws IOException, InterruptedException {

    /**
     * To use an API key for authentication, use the following code:
     */
    ImportIO client = new ImportIO(UUID.fromString("4c46b4b2-e818-4462-bf47-bf89165c3ace"), "CZs+sxsZFF/u3dBPO6Y+yYww1AWBRbIM20cWB5f0HXhcgGl36PmpjzRgwmcDA6eSUMkWmRQ9CiNIwGK2vfuWqg==", "import.io");
    
    /**
     * Once we have started the client and authenticated, we need to connect it to the server:
     */
    client.connect();
    
    /**
     * Because import.io queries are asynchronous, for this simple script we will use a {@see CountdownLatch}
     * to stop the script from exiting before all of our queries are returned. We are doing 1 queries in this
     * example so we initialise it with "1"
     */
    final CountDownLatch latch = new CountDownLatch(1);
    
    final List<Object> dataRows = new ArrayList<Object>();
    
    /**
     * In order to receive the data from the queries we issue, we need to define a callback method
     * This method will receive each message that comes back from the queries, and we can take that
     * data and store it for use in our app. {@see MessageCallback}
     */
    MessageCallback messageCallback = new MessageCallback() {
      /**
       * This method is called every time a new message is received from the server relating to the
       * query that we issued
       */
      @SuppressWarnings("unchecked")
      public void onMessage(Query query, QueryMessage message, Progress progress) {
        if (message.getType() == MessageType.MESSAGE) {
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
            dataRows.addAll(results);
          }
        }
        // When the query is finished, countdown the latch so the program can continue when everything is done
        if ( progress.isFinished() ) {
          latch.countDown();
        }
      }
    };

    Map<String, Object> queryInput;
    Query query;
    List<UUID> connectorGuids;
    
    // Query for tile Github Search Extractor
    connectorGuids = Arrays.asList(
      UUID.fromString("e783bee1-c5db-4acd-a430-6d612310559a")
    );
    queryInput = new HashMap<String,Object>();

    //Try to get 5 pages user from Github
    for(int i = 1; i < 5; i++) {
      queryInput.put("webpage/url", "https://github.com/search?p="+i+"&q=location%3AUSA&ref=searchresults&type=Users&utf8=%E2%9C%93");
      query = new Query();
      query.setConnectorGuids(connectorGuids);
      query.setInput(queryInput);
      client.query(query, messageCallback);
      Thread.sleep(1000);
    }
    
    // Wait on all of the queries to be completed
    latch.await();
    
    // It is best practice to disconnect when you are finished sending queries and getting data - it allows us to
    // clean up resources on the client and the server
    client.disconnect();
    
    // Now we can print out the data we got
    System.out.println("All data received:");
    System.out.println(dataRows);
  }

}