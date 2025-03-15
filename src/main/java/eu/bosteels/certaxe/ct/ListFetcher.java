package eu.bosteels.certaxe.ct;

import eu.bosteels.certaxe.observability.ProgressDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service public class ListFetcher {

  private final EntryFetcher entryFetcher;
  private final ProgressDatabase progressDatabase;
  private static final Logger logger = LoggerFactory.getLogger(ListFetcher.class);

  @Autowired
  public ListFetcher(EntryFetcher entryFetcher, ProgressDatabase progressDatabase) {
    this.entryFetcher = entryFetcher;
    this.progressDatabase = progressDatabase;
  }

  public void fetch(LogList list) throws IOException, InterruptedException {
    fetch(list, Integer.MAX_VALUE);
  }

  /**
   * Fetch items from list
   * @param list The list to fetch from
   * @param indexLast  index of last item to fetch 
   * @throws IOException when fetching fails
   * @throws InterruptedException when fetching was interrupted
   */
  public void fetch(LogList list, int indexLast) throws IOException, InterruptedException {
    entryFetcher.check(list);
    logger.info("list.treeSize = {}", list.getTreeSize());

    int first = progressDatabase.getNextIndex(list);

    int max = Math.min(indexLast, list.getTreeSize());

    if (max < first) {
      logger.info("We fetched everything before {} and indexLast={} => nothing to do", first, indexLast);
      return;
    }

    Interval interval = new Interval(list, first, max);
    entryFetcher.fetch(interval);

    int fetched = max - first + 1;

    logger.info("Done fetching {} entries from {}", fetched, list);
  }
}
