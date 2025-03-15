package eu.bosteels.certaxe.ct;

import eu.bosteels.certaxe.observability.Event;
import eu.bosteels.certaxe.observability.ProgressDatabase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

class ListFetcherTest {

  public static final int CAPACITY = 40;

  private final BlockingQueue<Entry> queue = new LinkedBlockingQueue<>(CAPACITY);
  private static final Logger logger = LoggerFactory.getLogger(EntryFetcherTest.class);
  private final EntryFetcher fetcher = new EntryFetcher(queue);

  private final ProgressDatabase progressDatabase = new ProgressDatabase();
  private final LogList xenon = new LogList("xenon2018", "http://ct.googleapis.com/logs/xenon2018/");
  private final ListFetcher listFetcher = new ListFetcher(fetcher, progressDatabase);

  @Test
  public void progress() throws IOException, InterruptedException {
    var started = Instant.now();
    var finished = Instant.now().plusSeconds(35);
    Event event = new Event(started, finished, Event.Type.Appended, xenon.getBaseURL(), 10, 16);
    progressDatabase.append(event);
    listFetcher.fetch(xenon, 20);
    logger.info("=> queue has {} items", queue.size());
    // TODO
    //assertThat(queue.size()).isEqualTo(4);
  }

  @Test
  public void listInfo() throws IOException, InterruptedException {
    fetcher.check(xenon);
  }

  @Test
  public void nothingToDo() throws IOException, InterruptedException {
    var started = Instant.now();
    var finished = Instant.now().plusSeconds(35);
    Event event = new Event(started, finished, Event.Type.Appended, xenon.getBaseURL(), 10, 16);
    progressDatabase.append(event);
    listFetcher.fetch(xenon, 16);
    logger.info("=> queue has {} items", queue.size());
    assertThat(queue.size()).isEqualTo(0);
  }

}