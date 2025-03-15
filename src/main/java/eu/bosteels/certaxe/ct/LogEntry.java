package eu.bosteels.certaxe.ct;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

@Data
public class LogEntry {

  @JsonProperty(value = "leaf_input")
  private String leaf;

  @JsonProperty(value = "extra_data")
  private String data;

  @Override
  public String toString() {
    return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
        .append("leaf", StringUtils.abbreviate(leaf,8))
        .append("data", StringUtils.abbreviate(data, 8))
        .toString();
  }
}
