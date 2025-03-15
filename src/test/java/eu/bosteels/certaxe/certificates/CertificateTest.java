package eu.bosteels.certaxe.certificates;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import static eu.bosteels.certaxe.certificates.CertificateReader.readTestCertificate;
import static org.assertj.core.api.Assertions.assertThat;

class CertificateTest {

  private static final Logger logger = LoggerFactory.getLogger(CertificateTest.class);
  @SuppressWarnings("SpellCheckingInspection")
  final String cert_base64 = "MIIGHjCCBQagAwIBAgISBBurwQX3TJScvk7u9jTnW8nJMA0GCSqGSIb3DQEBCwUAMEoxCzAJBgNVBA" + "YTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQDExpMZXQncyBFbmNyeXB0IEF1dGhv" + "cml0eSBYMzAeFw0xODA0MjkxODAyNDJaFw0xODA3MjgxODAyNDJaMB0xGzAZBgNVBAMTEnd3dy5ib25kZWRhdX" + "RvLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALVkY8gJnq0skfc5pTTKzMBL3dowN7WCGTITgREZ2OOzEOd0" + "etVB9trib91RTCHTvltgkQVBy9/2Zg4W470o48XY6WC+/QV7qN053bpE4OvM2bhKVFZxhq587/sqqIS/fFB9zz2dkGv6GQ06HL" + "eW5ggRL7IAcH+vRjS/a/TZ2hplcA2141BW1sRN9F/r5TTrSA4tq2uxqxXdxTHeHQ2gtjcs34y7D4Z5WayrLYfOvsY6Yi6CVXKR" + "EEL7oTAD5A1aZ+0gOjwU7OnLXW0Dn0pEOWQfZr31M2HE6ehQUwbLn25KmbKzfwYhyNckgEUMFow57XCKCll0UuxZq4eZmoG8H1E" + "CAwEAAaOCAykwggMlMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDAYDVR0TAQH/BAIwAD" + "AdBgNVHQ4EFgQUqy7Q+ScivG++WlRwbxLOoNUENZIwHwYDVR0jBBgwFoAUqEpqYwR93brm0Tm3pkVl7/Oo7KEwbwYIKwYBBQUHAQ" + "EEYzBhMC4GCCsGAQUFBzABhiJodHRwOi8vb2NzcC5pbnQteDMubGV0c2VuY3J5cHQub3JnMC8GCCsGAQUFBzAChiNodHRwOi8vY2V" + "ydC5pbnQteDMubGV0c2VuY3J5cHQub3JnLzAtBgNVHREEJjAkgg5ib25kZWRhdXRvLmNvbYISd3d3LmJvbmRlZGF1dG8uY29tMIH+B" + "gNVHSAEgfYwgfMwCAYGZ4EMAQIBMIHmBgsrBgEEAYLfEwEBATCB1jAmBggrBgEFBQcCARYaaHR0cDovL2Nwcy5sZXRzZW5jcnlwdC5vcm" + "cwgasGCCsGAQUFBwICMIGeDIGbVGhpcyBDZXJ0aWZpY2F0ZSBtYXkgb25seSBiZSByZWxpZWQgdXBvbiBieSBSZWx5aW5nIFBhcnRpZXM" + "gYW5kIG9ubHkgaW4gYWNjb3JkYW5jZSB3aXRoIHRoZSBDZXJ0aWZpY2F0ZSBQb2xpY3kgZm91bmQgYXQgaHR0cHM6Ly9sZXRzZW5jcnlwdC5vcmcvcmVwb3NpdG9yeS8wggEDBgorBgEEAdZ5AgQCBIH0BIHxAO8AdQApPFGWVMg5ZbqqUPxYB9S3b79Yeily3KTDDPTlRUf0eAAAAWMSySTKAAAEAwBGMEQCIHk7Hlz9S5VKn6GcCx7IqjnqTFY3/CdC4Eow9gAoNgnNAiBBZHAW7RH2GYrH2ajLs0k+9yVV+fhlnEDp5xgY660+1wB2AFWB1MIWkDYBSuoLm1c8U/DA5Dh4cCUIFy+jqh0HE9MMAAABYxLJJOEAAAQDAEcwRQIhAN5aMulW8WP4EQSpDgsgvLOL9sa8DP4y1WE1M05iT70jAiBkzibWAe8ayfIp4Cel6lkp2rTnU/MrcxZ3lC23pEdkvjANBgkqhkiG9w0BAQsFAAOCAQEAHRyEuRb9uyZ2/2IQ1qhraRAsI2epSB3XTXSLSUdPAyq3CkXYmnoMiHSYoKfGNPC9sA7lpknv5JzbZUWv1gcSTMgtMYYhcOW7inpUTNLQQLXy1GG8zvjh7QCFsjryfXqpu0KTIAI254JBp9US+sOi7TTW+O9FrRxlV31RWnSecnPG3UgOhS0ax5oH1eP+vNAQhywUbyClkq3fXpDRoSNXvfxgx8+h/cKbySNufGUcSWlR9Xj1M4N33fmF6v/cpGS+uxZm7mfsw40zHOK6M8b/RUQFMpFuGbyCef8bbPOEwtuISLn9UJ1iObcjvI2YuaZueC2eVtKg/soSZzJE7g8bXQ==";

  @Test
  public void parse() throws CertificateException {
    byte[] bytes = java.util.Base64.getDecoder().decode(cert_base64);
    logger.info("bytes,length = " + bytes.length);
    InputStream in = new ByteArrayInputStream(bytes);
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    logger.info("certFactory = " + certFactory.getClass().getName());
    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
  }

  @Test
  public void subjectAlternativeNames_DnsBelgium_be() throws CertificateException, IOException {
    X509Certificate certificate = readTestCertificate("dnsbelgium.be.pem");
    List<String> subjectAlternativeNames = Certificate.getSubjectAlternativeNames(certificate);
    logger.info("subjectAlternativeNames = {}", subjectAlternativeNames);
    assertThat(subjectAlternativeNames).containsOnly("dnsbelgium.be", "production.dnsbelgium.be", "www.dnsbelgium.be");
  }

  @Test void blackanddecker() throws CertificateException, IOException {
    X509Certificate certificate = readTestCertificate("blackanddecker.be.pem");
    Certificate info = Certificate.from(certificate);
    logger.info("info = {}", info);
    logger.info("info = {}", info.prettyString());
    assertThat(info.getSha256Fingerprint()).isEqualTo("fb051996220fa119022b25ce0d476725c6711f9142001801c9437af0f6017739");
    assertThat(info.getIssuer()).isEqualTo("CN=DigiCert TLS RSA SHA256 2020 CA1,O=DigiCert Inc,C=US");
    assertThat(info.getSubject()).isEqualTo("CN=www.blackanddecker.com,O=Stanley Black & Decker\\, Inc.,L=New Britain,ST=Connecticut,C=US");
    assertThat(info.getPublicKeySchema()).isEqualTo("EC");
    assertThat(info.getPublicKeyLength()).isEqualTo(256);
    assertThat(info.getNotBefore()).isEqualTo("2022-11-21T00:00:00Z");
    assertThat(info.getNotAfter()).isEqualTo("2023-11-21T23:59:59Z");
    assertThat(info.getSerialNumberHex()).isEqualTo("0c:03:5e:1e:91:26:8b:8a:9d:cd:c8:46:03:6e:54:fe");
    assertThat(info.getVersion()).isEqualTo(3);
    assertThat(info.getSignatureHashAlgorithm()).isEqualTo("SHA256withRSA");


  }

}