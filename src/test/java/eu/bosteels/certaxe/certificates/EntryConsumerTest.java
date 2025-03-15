package eu.bosteels.certaxe.certificates;

import org.cryptacular.util.CertUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

class EntryConsumerTest {


  private static final Logger logger = LoggerFactory.getLogger(EntryConsumerTest.class);

  @Test
  public void parse() throws IOException, CertificateParsingException {
    byte[] certBytes = FileCopyUtils.copyToByteArray(new File("data/cert.467.cert"));
    logger.info("bytes = {}", certBytes.length);
    X509Certificate x509Certificate = CertUtil.decodeCertificate(certBytes);
    logger.info("Parsed {} bytes to obtain X509Certificate", certBytes.length);
    //logger.info("x509Certificate = {}", x509Certificate);
    Certificate certificate = Certificate.from(x509Certificate);
    logger.info("certificate = {}", certificate.toString());
  }
}