package eu.bosteels.certaxe.observability;

import eu.bosteels.certaxe.ct.LogList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

  private static final Logger logger = LoggerFactory.getLogger(EventTest.class);

  @Test
  public void make() {
    var started = Instant.now();
    var finished = Instant.now().plusSeconds(35);
    Event event = new Event(started, finished, Event.Type.Fetched, "some_url", 10, 16);
    logger.info("event = " + event);
    assertThat(event.getFinished()).isEqualTo(finished);
  }

  @Test
  public void eventLogger() {
    ProgressDatabase db = new ProgressDatabase();
    EventLogger eventLogger = new EventLogger(db);
    LogList list = new LogList("my-friend", "http://some-url/");
    String url = list.getBaseURL();

    var ts = Instant.now();
    Event fetched  = new Event(ts, ts.plusMillis(523), Event.Type.Fetched, url, 10, 16);
    Event appended = new Event(ts.plusMillis(5), ts.plusMillis(37), Event.Type.Appended, url, 10, 16);
    eventLogger.log(fetched);
    eventLogger.log(appended);
  }

}