package eu.bosteels.certaxe.ct;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LogEntries {

  @JsonProperty(value = "entries")
  private List<LogEntry> entries;

  public int size() {
    return entries.size();
  }

  @Override
  public String toString() {
    return "LogEntries with " + entries.size() + " entries}";
  }
}
