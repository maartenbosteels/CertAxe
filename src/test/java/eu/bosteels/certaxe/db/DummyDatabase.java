package eu.bosteels.certaxe.db;

import org.duckdb.DuckDBAppender;
import org.duckdb.DuckDBArray;
import org.duckdb.DuckDBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DummyDatabase {

  private final Connection connection;
  private static final Logger logger = LoggerFactory.getLogger(DummyDatabase.class);

  public DummyDatabase() throws SQLException {
    this.connection = DriverManager.getConnection("jdbc:duckdb:");
    
    Statement stmt = connection.createStatement();
    stmt.execute("""
        CREATE TABLE items (
          item VARCHAR,
          value DECIMAL(10, 2),
          count INTEGER,
          names VARCHAR[]
          )""");
    stmt.close();
    listRows();
  }

  public void listRows() throws SQLException {
    @SuppressWarnings("SqlResolve")
    String query = "SELECT * FROM items";
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        String item = rs.getString("item");
        Array namesArray = rs.getArray("names");
        Object names = rs.getObject("names");
        int count = rs.getInt("count");
        logger.info("item={}, count={} names={}", item, count, names);

        logger.info("names = " + names);
        logger.info("namesArray = " + namesArray);

        if (namesArray != null) {
          if (namesArray instanceof DuckDBArray) {
            int baseType = namesArray.getBaseType();
            logger.info("baseType={} name={}", baseType, namesArray.getBaseTypeName());
            Object array = namesArray.getArray();
            if (array instanceof Object[] objectArray) {
              logger.info("objectArray = " + objectArray.length);
              for (Object o : objectArray) {
                logger.info("o = " + o);
              }
            }
            logger.info("array = " + array.getClass());
            //array[0]
          }
        }
      }
      System.out.println("done with looping over the resultset");
    }
  }

  @SuppressWarnings("SqlResolve")
  public void addNames() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("""
        INSERT INTO items(item,value,count, names)
        VALUES
           ('hammer', 42.2, 2, ['abc', 'def'])
        """);
    }
  }


  @SuppressWarnings("SqlResolve")
  public void insert() throws SQLException {
    String insert = """
      insert into items (item,value,count, names)
      values
      ('array-with-two-elements', 12, 100, [?, ?])
      """;
    try (PreparedStatement stmt = connection.prepareStatement(insert)) {
      stmt.setString(1, "John");
      stmt.setString(2, "Jack");
      int rows = stmt.executeUpdate();
      logger.info("stmt impacted {} rows", rows);
    }
  }

  @SuppressWarnings("SqlResolve")
  public void appenderAndTempTable() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("""
          CREATE TABLE items_staging (
            item VARCHAR,
            value DECIMAL(10, 2),
            count INTEGER,
            names_ VARCHAR
            )""");

      if (connection instanceof DuckDBConnection duckDBConnection) {
        DuckDBAppender appender = duckDBConnection.createAppender("main", "items_staging");
        logger.info("DuckDBAppender = " + appender);
        appender.beginRow();
        appender.append("via_appender");
        appender.append(10.2);
        appender.append(1024); //count
        appender.append("['abc.org', 'www.abc.org'] ");
        appender.endRow();
        appender.flush();
      }
      String copy = """
          insert into items select item, value, count, names_ from items_staging
          """;
      stmt.execute(copy);
    }
  }
}
