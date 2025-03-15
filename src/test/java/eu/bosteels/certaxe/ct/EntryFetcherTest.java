package eu.bosteels.certaxe.ct;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EntryFetcherTest {

  private final BlockingQueue<Entry> queue = new LinkedBlockingQueue<>(110);
  private static final Logger logger = LoggerFactory.getLogger(EntryFetcherTest.class);
  private final EntryFetcher fetcher = new EntryFetcher(queue);

  private final LogList xenon = new LogList("xenon2018", "http://ct.googleapis.com/logs/xenon2018/");

  @Test
  public void fetch_five() throws IOException, InterruptedException {
    queue.clear();
    Interval interval = new Interval(xenon, 1, 5);
    fetcher.fetch(interval);
    assertThat(queue.size()).isEqualTo(5);
  }

  @Test
  public void fetch_100() throws IOException, InterruptedException {
    queue.clear();
    Interval interval = new Interval(xenon, 100, 200);
    fetcher.fetch(interval);
    assertThat(queue.size()).isEqualTo(101);
  }

  @Test
  public void uri() {
    Interval interval = new Interval(xenon, 1, 5);
    var uri = interval.downloadUri();
    logger.info("interval.downloadUri = " + uri);
    var expected = URI.create("http://ct.googleapis.com/logs/xenon2018/ct/v1/get-entries?start=1&end=5");
    assertThat(uri).isEqualTo(expected);
  }

  @Test public  void check() throws IOException, InterruptedException {
    logger.info("BEFORE: xenon = " + xenon);
    fetcher.check(xenon);
    logger.info("AFTER: xenon = " + xenon);
    assertThat(xenon.getTreeSize()).isGreaterThan(5);
    assertThat(xenon.getTimestamp()).isAfter("2023-11-01T00:00:00.000Z");
  }
}