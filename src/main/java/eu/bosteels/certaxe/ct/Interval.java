package eu.bosteels.certaxe.ct;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URI;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

@Data
public class Interval {

  private LogList list;
  private int start;
  private int end;

  private final String CTL_INFO = "%sct/v1/get-sth";
  private final String DOWNLOAD = "%sct/v1/get-entries?start=%d&end=%d";

  public Interval(LogList list, int start, int end) {
    if (end < start) {
      throw new IllegalArgumentException("start=%d should be <= end=%d".formatted(start, end));
    }
    this.list = list;
    this.start = start;
    this.end = end;
  }

  public Interval slice(int from) {
    return new Interval(list, from, end);
  }

  public long size() {
    return end - start + 1;
  }

  public String downloadUrl() {
    return DOWNLOAD.formatted(list.getBaseURL(), start, end);
  }

  public URI downloadUri() {
    return URI.create(downloadUrl());
  }

  @Override
  public String toString() {

    return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
        .append("list", list.getFriendlyName())
        .append("start", start)
        .append("end", end)
        .toString();
  }
}
