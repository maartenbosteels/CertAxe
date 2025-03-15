package eu.bosteels.certaxe.db;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

class DummyDatabaseTest {

  private static final Logger logger = LoggerFactory.getLogger(DummyDatabaseTest.class);

  @Test
  public void create() throws SQLException {
    DummyDatabase db = new DummyDatabase();
    //db.add();
    db.addNames();
    db.listRows();
    
  }

  @Test
  public void arrays() throws SQLException {
    logger.info("info");
    DummyDatabase db = new DummyDatabase();
    db.insert();
    db.listRows();
  }

  @Test
  public void appenderAndTempTable() throws SQLException {
    DummyDatabase db = new DummyDatabase();
    db.appenderAndTempTable();
    db.listRows();
  }

  @Test
  public void logging() throws SQLException {
    logger.debug("debug");
    logger.info("info");
    logger.warn("warn");
    logger.error("error");
  }

  @Test
  public void arrow_stuff() throws IOException {
    // see https://duckdb.org/docs/archive/0.9.2/api/java#arrow-import
    // I don't understand how to use it
    System.out.println(Integer.MAX_VALUE);
  }

}