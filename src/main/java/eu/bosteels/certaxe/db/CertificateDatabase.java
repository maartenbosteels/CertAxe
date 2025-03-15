package eu.bosteels.certaxe.db;

import eu.bosteels.certaxe.certificates.Certificate;
import eu.bosteels.certaxe.certificates.CertificateAppender;
import eu.bosteels.certaxe.ct.LogList;
import eu.bosteels.certaxe.observability.ProgressDatabase;
import org.duckdb.DuckDBConnection;
import org.duckdb.SmartAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;

@SuppressWarnings("SqlDialectInspection")
@Service
public class CertificateDatabase implements CertificateAppender {

  private final Connection connection;
  private static final Logger logger = LoggerFactory.getLogger(CertificateDatabase.class);

  private final SmartAppender appender;
  private final ProgressDatabase progressDatabase;

  // TODO: create enum for signatureHashAlgorithm
  // CREATE TYPE signatureHashAlgorithm AS ENUM ('sha256, 'sha384', 'sha1', 'md5', sha512');

  private final static String CREATE_TABLE;
                // or REPLACE
  static {
    CREATE_TABLE = """
              CREATE TABLE if not exists certificate (
                ct_list                   VARCHAR,
                ct_index                  INTEGER, --// or LONG ?
                version                   INTEGER,
                serialNumberHex           VARCHAR,
                publicKeySchema           VARCHAR,
                publicKeyLength           INTEGER,
                notBefore                 TIMESTAMP_MS   NOT NULL, -- TIMESTAMP_MS  not null,
                notAfter                  TIMESTAMP_MS   NOT NULL, -- TIMESTAMP_MS  not null,
                issuer                    VARCHAR,
                issuer_cn                 VARCHAR,
                issuer_c                  VARCHAR,
                issuer_o                  VARCHAR,
                subject                   VARCHAR,
                subject_cn                VARCHAR,
                subject_c                 VARCHAR,
                subject_o                 VARCHAR,
                signatureHashAlgorithm    VARCHAR,
                sha256Fingerprint         VARCHAR,
                subjectAlternativeNames   VARCHAR[],
                domainNames               VARCHAR[],
                publicSuffixes            VARCHAR[],
                registrableNames          VARCHAR[],
                topPrivateDomains         VARCHAR[],
                tlds                      VARCHAR[],
                authorityKeyIdentifier    VARCHAR,
                subjectKeyIdentifier      VARCHAR,
                keyUsage                  VARCHAR[],
                extendedKeyUsage          VARCHAR[],
                authorityInfoAccess       VARCHAR[],
                isCa                      BOOLEAN     not null
                );
        """;
  }

//          extensionCount            INTEGER,

  public CertificateDatabase(ProgressDatabase progressDatabase) throws SQLException {
    this.progressDatabase = progressDatabase;
    // TODO: decide if we want to use a persistent database instead
    // for example to keep track of the progress
    this.connection = DriverManager.getConnection("jdbc:duckdb:cert.db");
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(CREATE_TABLE);
    }
    countRows();
    appender = new SmartAppender((DuckDBConnection) connection, "main", "certificate");
  }

  @SuppressWarnings("SqlDialectInspection")
  public long countRows() throws SQLException {
    final String query = "select count(1) count from certificate";
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
      if (rs.next()) {
        var count = rs.getLong("count");
        logger.info("We found {} certificates in the database", count);
        return count;
      }
      logger.warn("query {} did not return any rows!?", query);
      return 0;
    }
  }

  public void append(Certificate certificate, LogList list, long index) throws SQLException {
    appender.beginRow();
    appender.append(list.getFriendlyName());
    appender.append(index);
    appender.append(certificate.getVersion());
    appender.append(certificate.getSerialNumberHex());
    appender.append(certificate.getPublicKeySchema());
    appender.append(certificate.getPublicKeyLength());
    appender.appendInstant(certificate.getNotBefore());
    appender.appendInstant(certificate.getNotAfter());
    appender.append(certificate.getIssuer());
    appender.append(certificate.getIssuer_CN());
    appender.append(certificate.getIssuer_C());
    appender.append(certificate.getIssuer_O());
    appender.append(certificate.getSubject());
    appender.append(certificate.getSubject_CN());
    appender.append(certificate.getSubject_C());
    appender.append(certificate.getSubject_O());
    appender.append(certificate.getSignatureHashAlgorithm());
    appender.append(certificate.getSha256Fingerprint());
    appender.append(certificate.getSubjectAlternativeNames());
    appender.append(certificate.getDomainNames());
    appender.append(certificate.getPublicSuffixes());
    appender.append(certificate.getRegistrableNames());
    appender.append(certificate.getTopPrivateDomains());
    appender.append(certificate.getTlds());
    //extensionCount
    appender.append(certificate.getAuthorityKeyIdentifier());
    appender.append(certificate.getSubjectKeyIdentifier());
    appender.append(certificate.getKeyUsage());
    appender.append(certificate.getExtendedKeyUsage());
    appender.append(certificate.getAuthorityInfoAccess());
    appender.append(certificate.getIsCa());
    appender.endRow();
    appender.flush();
    logger.info("Appended cert {}", index);
    //Event event = new Event()
    //progressDatabase.append();

  }

  public void flush() throws SQLException {
    appender.flush();
  }
}
