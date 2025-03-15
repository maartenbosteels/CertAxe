package eu.bosteels.certaxe.observability;

import eu.bosteels.certaxe.ct.LogList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressDatabaseTest {

  private static final Logger logger = LoggerFactory.getLogger(ProgressDatabaseTest.class);

  @Test
  public void getNextIndex() {
    ProgressDatabase db = new ProgressDatabase();
    LogList list = new LogList("my-friend", "http://some-url/");
    String url = list.getBaseURL();

    var ts = Instant.now();
    Event fetched  = new Event(ts, ts.plusMillis(523), Event.Type.Fetched, url, 10, 16);
    Event appended = new Event(ts.plusMillis(5), ts.plusMillis(37), Event.Type.Appended, url, 10, 16);
    db.append(fetched);
    db.append(appended);
    long count = db.countRows();
    logger.info("count = {}", count);
    int next = db.getNextIndex(list);
    logger.info("next = {}", next);
    assertThat(next).isEqualTo(17);
  }

}