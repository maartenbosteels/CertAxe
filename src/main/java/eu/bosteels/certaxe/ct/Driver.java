package eu.bosteels.certaxe.ct;

import eu.bosteels.certaxe.certificates.EntryConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * The Driver manages the ListFetcher and the EntryConsumer
 */
@Service public class Driver {

  private final EntryConsumer entryConsumer;
  private final ListFetcher listFetcher;

  private static final Logger logger = LoggerFactory.getLogger(Driver.class);

  @Autowired
  public Driver(EntryConsumer entryConsumer, ListFetcher listFetcher) {
    this.entryConsumer = entryConsumer;
    this.listFetcher = listFetcher;
  }


  public void start(LogList list) {
    var downloader = Executors.newFixedThreadPool(1);
    downloader.submit(() -> {
      try {
        listFetcher.fetch(list);
      } catch (IOException e) {
        logger.error("Failed to fetch {}", list);
        throw new RuntimeException(e);

      } catch (InterruptedException e) {
        logger.error("Interrupted while fetching {}", list);
        //throw new RuntimeException(e);
      }
    });
    logger.info("downloader started");

    var consumer = Executors.newFixedThreadPool(5);
    consumer.submit(() -> {
      try {
        entryConsumer.start();
        logger.info("entryConsumer is done ??");
      } catch (InterruptedException e) {
        logger.error("Interrupted while consuming {}", list);
        //throw new RuntimeException(e);
      }
    });
    logger.info("consumer started");

  }

  public void stopFetching(LogList list) {
    // TODO: stop executors, tbd how
  }

}
