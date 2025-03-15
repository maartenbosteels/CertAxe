package eu.bosteels.certaxe.observability;

import eu.bosteels.certaxe.ct.Entry;
import eu.bosteels.certaxe.ct.Interval;
import lombok.Data;

import java.time.Instant;

@Data
public class Event {

  public enum Type { Fetched, Appended }

  private final Instant started;
  private final Instant finished;
  private final Type type;
  private final String listUrl;
  private final int start;
  private final int end;

  public static Event appended(Instant start, Entry entry) {
    return new Event(start, Instant.now(), Event.Type.Appended, entry.getLogList().getBaseURL(), entry.getIndex(), entry.getIndex());
  }

  public static Event fetched(Instant start, Interval interval) {
    return new Event(start, Instant.now(), Event.Type.Appended, interval.getList().getBaseURL(), interval.getStart(), interval.getEnd());
  }

}
