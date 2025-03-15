package eu.bosteels.certaxe.observability;

import eu.bosteels.certaxe.ct.LogList;
import lombok.SneakyThrows;
import org.duckdb.DuckDBConnection;
import org.duckdb.SmartAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;

@SuppressWarnings("SqlDialectInspection")
@Service
public class ProgressDatabase {

  private final Connection connection;
  private final SmartAppender appender;
  private static final Logger logger = LoggerFactory.getLogger(ProgressDatabase.class);

  private final static String CREATE_TABLE;

  static {
    CREATE_TABLE = """
              CREATE TABLE IF NOT EXISTS event (
                started  TIMESTAMP_MS,
                finished TIMESTAMP_MS,
                type     VARCHAR,
                list     VARCHAR,
                start_index    int,
                end_index      int
              );
        """;
  }

  @SneakyThrows
  public ProgressDatabase() {
    this.connection = DriverManager.getConnection("jdbc:duckdb:progress.db");
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(CREATE_TABLE);
    }
    long rows = countRows();
    logger.info("rows = {}", rows);
    appender = new SmartAppender((DuckDBConnection) connection, "main", "event");

  }

  @SneakyThrows
  public long countRows() {
    final String query = "select count(1) count from event";
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
      if (rs.next()) {
        return rs.getLong("count");
      }
      logger.warn("query {} did not return any rows!?", query);
      return 0;
    }
  }

  @SneakyThrows
  public void append(Event event) {
    try {
      connection.beginRequest();
      appender.beginRow();
      appender.appendInstant(event.getStarted());
      appender.appendInstant(event.getFinished());
      appender.append(event.getType().name());
      appender.append(event.getListUrl());
      appender.append(event.getStart());
      appender.append(event.getEnd());
      appender.endRow();
      logger.info("row appended");
      appender.flush();
    } finally {
      //connection.commit();
      connection.endRequest();
    }
  }

  public int getNextIndex(LogList list) {
    String query = "select max(end_index) last_index from event where type = 'Appended' and list = ?";
    try (PreparedStatement ps = connection.prepareStatement(query)) {
      ps.setString(1, list.getBaseURL());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          int last_index = rs.getInt("last_index");
          logger.info("{} => last_index = {}", list.getBaseURL(), last_index);
          return last_index + 1;
        }
        logger.info("No progress found for {}", list.getFriendlyName());
        return 0;
      }
    } catch (SQLException e) {
      logger.error("Fetching progress failed: ", e);
      return 0;
    }

  }



}
