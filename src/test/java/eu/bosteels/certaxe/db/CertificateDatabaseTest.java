package eu.bosteels.certaxe.db;

import eu.bosteels.certaxe.certificates.Certificate;
import eu.bosteels.certaxe.ct.LogList;
import eu.bosteels.certaxe.observability.ProgressDatabase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

import static eu.bosteels.certaxe.certificates.CertificateReader.readTestCertificate;
import static org.assertj.core.api.Assertions.assertThat;

class CertificateDatabaseTest {

  private static final Logger logger = LoggerFactory.getLogger(CertificateDatabaseTest.class);

  @Test
  public void init() throws SQLException {
    CertificateDatabase db = new CertificateDatabase(new ProgressDatabase());
    //assertThat(db.countRows()).isEqualTo(0);
  }

  @Test
  public void insert() throws SQLException, CertificateException, IOException {
    CertificateDatabase db = new CertificateDatabase(new ProgressDatabase());
    X509Certificate cert = readTestCertificate("blackanddecker.be.pem");
    Certificate certificate = Certificate.from(cert);
    logger.info("info = {}", certificate.prettyString());
    LogList list = new LogList("xenon2018", "http://ct.googleapis.com/logs/xenon2018/");
    db.append(certificate, list, 54545);
    db.flush();
    logger.info("db = {}", db.countRows());

    
  }

}