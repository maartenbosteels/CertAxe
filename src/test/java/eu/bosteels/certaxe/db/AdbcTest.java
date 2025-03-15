package eu.bosteels.certaxe.db;

import org.apache.arrow.adbc.core.*;
import org.apache.arrow.adbc.driver.jdbc.JdbcDriver;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdbcTest {

  private static final Logger logger = LoggerFactory.getLogger(AdbcTest.class);

  @Test
  @Disabled
  public void test() throws AdbcException, IOException {
    final Map<String, Object> parameters = new HashMap<>();
    BufferAllocator allocator = new RootAllocator();
    parameters.put(AdbcDriver.PARAM_URL, "jdbc:duckdb:");

    //final Map<String, Object> parameters = new HashMap<>();
    //parameters.put(AdbcDriver.PARAM_URL, "jdbc:postgresql://localhost:5432/postgres");

    AdbcDatabase db = new JdbcDriver(allocator).open(parameters);
    AdbcConnection connection = db.connect();
    logger.info("connection = " + connection);
    logger.info("db = " + db);
    //AdbcStatement statement = connection.bulkIngest("certs", BulkIngestMode.APPEND);
    AdbcStatement statement = connection.createStatement();
    //statement.setOption();
    statement.setSqlQuery("create table t1 (id int, name varchar)");
    try {
      statement.executeUpdate();
    } catch (Exception e) {
      logger.info("e = " + e, e);
    }
    {
      AdbcStatement statement2 = connection.createStatement();
      statement2.setSqlQuery("insert into t1(id, name) values (100, 'John');");
      AdbcStatement.UpdateResult updateResult = statement2.executeUpdate();
      logger.info("getAffectedRows = " + updateResult.getAffectedRows());
    }
//    ArrowReader reader = rs.getReader();
//    while (reader.loadNextBatch()) {
//      Set<Long> x = reader.getDictionaryIds();
//      logger.info("x = " + x);
//
//    }

  }

  @Test
  @Disabled
  public void testPreparedStatement() throws AdbcException, IOException {
    final Map<String, Object> parameters = new HashMap<>();
    BufferAllocator allocator = new RootAllocator();
    parameters.put(AdbcDriver.PARAM_URI.getKey(), "jdbc:duckdb:");

    //final Map<String, Object> parameters = new HashMap<>();
    //parameters.put(AdbcDriver.PARAM_URL, "jdbc:postgresql://localhost:5432/postgres");

    AdbcDatabase db = new JdbcDriver(allocator).open(parameters);
    AdbcConnection connection = db.connect();
    logger.info("connection = " + connection);
    logger.info("db = " + db);
    //AdbcStatement statement = connection.bulkIngest("certs", BulkIngestMode.APPEND);
    AdbcStatement statement = connection.createStatement();
    //statement.setOption();
    statement.setSqlQuery("create table t1 (id int, name varchar)");
    statement.prepare();
  }

  public void insertArray() {
    
  }
}
