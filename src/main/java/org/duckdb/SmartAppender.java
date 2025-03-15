package org.duckdb;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;

@SuppressWarnings("ConstantValue")
public class SmartAppender extends DuckDBAppender {


  private final String NULL = null;
  public SmartAppender(DuckDBConnection con, String schemaName, String tableName) throws SQLException {
    super(con, schemaName, tableName);
  }

  public void append(Collection<String> collection) throws SQLException {
    if (collection == null) {
      append(NULL);
    } else {
      append(collection.toString());
    }
  }

  public void appendInstant(Instant instant) throws SQLException {
    if (instant == null) {
      append(NULL);
      //DuckDBNative.duckdb_jdbc_appender_append_null(appender_ref);
    } else {
      long timeInMicros = instant.toEpochMilli() * 1000L;
      DuckDBNative.duckdb_jdbc_appender_append_timestamp(appender_ref, timeInMicros);
    }
  }


  public void append(Integer integer) throws SQLException {
    if (integer == null) {
      append(NULL);
    } else {
      super.append(integer);
    }
  }



}
