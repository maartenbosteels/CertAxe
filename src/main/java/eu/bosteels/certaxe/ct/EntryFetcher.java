package eu.bosteels.certaxe.ct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

@Service
public class EntryFetcher {

  private final BlockingQueue<Entry> queue;
  private final HttpClient client;
  private final ObjectMapper objectMapper;

  private static final Logger logger = LoggerFactory.getLogger(EntryFetcher.class);

  @Autowired
  public EntryFetcher(BlockingQueue<Entry> queue) {
    this.queue = queue;
    this.client = HttpClient
        .newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    this.objectMapper = new ObjectMapper();
  }

  public void fetch(Interval interval) throws IOException, InterruptedException {
    boolean done = false;
    while (!done) {
      int start = (int) interval.getStart();
      int fetched = do_fetch(interval);
      if (fetched >= interval.size()) {
        done = true;
      } else {
        interval = interval.slice(start + fetched);
      }
    }
  }

  public int do_fetch(Interval interval) throws IOException, InterruptedException {
    logger.debug("Starting to fetch {}", interval);
    var request = HttpRequest.newBuilder()
        .GET()
        .uri(interval.downloadUri())
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    //logger.debug("response = " + response.body());

    LogEntries entries = objectMapper.readValue(response.body(), LogEntries.class);
    logger.debug("Received {} entries",  entries.size());
    int index = interval.getStart();
    for (LogEntry logEntry : entries.getEntries()) {
      logger.debug("entry = {}", logEntry);
      Entry entry = Entry.builder()
          .interval(interval)
          .leaf_input(Base64.decode(logEntry.getLeaf()))
          .extra_data(Base64.decode(logEntry.getData()))
          .index(index++)
          .build();
      //logger.debug("Adding to queue: {}", entry);
      queue.put(entry);
    }
    logger.info("Added {} entries to the queue. queue.size = {}", entries.size(), queue.size());
    return entries.size();

  }

  public void check(LogList logList) throws IOException, InterruptedException {
    HttpClient client = HttpClient
        .newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    var uri = URI.create(logList.getBaseURL() + "/ct/v1/get-sth");
    var request = HttpRequest.newBuilder()
        .GET()
        .uri(uri)
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(response.body());
    logger.info("jsonNode = " + jsonNode);
    Instant timestamp = Instant.ofEpochMilli(jsonNode.get("timestamp").asLong());
    logList.setTreeSize(jsonNode.get("tree_size").asInt());
    logList.setRootHash(jsonNode.get("sha256_root_hash").asText());
    logList.setTreeHeadSignature(jsonNode.get("tree_head_signature").asText());
    logList.setTimestamp(timestamp);
  }

}
