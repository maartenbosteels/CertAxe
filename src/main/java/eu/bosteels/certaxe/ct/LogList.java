package eu.bosteels.certaxe.ct;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Instant;

@Data
public class LogList {

  private final String baseURL;

  private String friendlyName;

  // https://ct.googleapis.com/logs/xenon2018/ct/v1/get-sth

  private int treeSize;
  private Instant timestamp;
  private String rootHash;
  private String treeHeadSignature;

  public LogList(String friendlyName, String baseURL) {
    this.baseURL = baseURL.endsWith("/") ? baseURL : baseURL + "/";
    this.friendlyName = friendlyName;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("baseURL", baseURL)
        .append("friendlyName", friendlyName)
        .append("treeSize", treeSize)
        .append("timestamp", timestamp)
        .toString();
  }
}
