package eu.bosteels.certaxe.observability;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.between;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;

@SuppressWarnings("SqlDialectInspection")
public class EventLogger {

  private static final Logger logger = LoggerFactory.getLogger(EventLogger.class);
  private static final String PATTERN_FORMAT = "yyyy-mm-dd HH:MM:SS";

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT)
      .withZone(ZoneId.systemDefault());

  private final ProgressDatabase progressDatabase;

  public EventLogger(ProgressDatabase progressDatabase) {
    this.progressDatabase = progressDatabase;
  }


  public void log(Event event) {
    logger.atInfo().setMessage("{} : {} : {} ms - start:{} end:{} url:{}")
        .addArgument(() -> formatter.format(event.getStarted()))
        .addArgument(() -> rightPad(event.getType().name(), 8))
        .addArgument(() -> leftPad(""+ between(event.getStarted(), event.getFinished()).toMillis(), 5))
        .addArgument(event.getStart())
        .addArgument(event.getEnd())
        .addArgument(event.getListUrl())
        .log();
    progressDatabase.append(event);
  }


}
