package eu.bosteels.certaxe.ct;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
@Builder
public class Entry {

  private final Interval interval;

  private int index;

  private byte[] leaf_input;
  private byte[] extra_data;

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
        //.append("interval", interval)
        .append("index", index)
        .toString();
  }

  public LogList getLogList() {
    return interval.getList();
  }
}
