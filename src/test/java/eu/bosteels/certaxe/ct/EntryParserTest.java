package eu.bosteels.certaxe.ct;


import eu.bosteels.certaxe.certificates.Certificate;
import org.bouncycastle.util.encoders.Base64;
import org.cryptacular.util.CertUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;

class EntryParserTest {

  private static final Logger logger = LoggerFactory.getLogger(EntryParserTest.class);
  
  @Test
  public void parse() throws IOException, CertificateParsingException {
    String leaf_input =
        "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";

    Interval interval = new Interval(null, 1, 1);
    byte[] input = Base64.decode(leaf_input);
    byte[] extra = Base64.decode(leaf_input);
    Entry entry = new Entry(interval, 1, input, extra);

    parse(entry);

  }

  public void parse(Entry entry) throws IOException, CertificateParsingException {

    DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(entry.getLeaf_input()));
    int version = dataInput.readUnsignedByte();
    System.out.println("version = " + version);
    int merkleLeafType = dataInput.readUnsignedByte();
    System.out.println("merkleLeafType = " + merkleLeafType);
    long ts = dataInput.readLong();
    System.out.println("ts = " + ts);
    Instant instant = Instant.ofEpochMilli(ts);
    System.out.println("instant = " + instant);
    int logEntryType = dataInput.readUnsignedShort();
    System.out.println("logEntryType = " + logEntryType);

    int b1 = dataInput.readUnsignedByte();
    int b2 = dataInput.readUnsignedByte();
    int b3 = dataInput.readUnsignedByte();
    int certLength = b1 * 256 * 256 + b2 * 256 + b3;
    logger.info("certLength = {}", certLength);
    byte [] certBytes = new byte[certLength];
    dataInput.readFully(certBytes);
    X509Certificate cert = CertUtil.decodeCertificate(certBytes);

    System.out.println("getCriticalExtensionOIDs = " + cert.getCriticalExtensionOIDs());
    System.out.println("getNonCriticalExtensionOIDs = " + cert.getNonCriticalExtensionOIDs());
    var certificate = Certificate.from(cert);
    logger.info("certificate = {}", certificate);
    logger.info("certificate = {}", certificate.prettyString());

  }

  //    logger.atInfo().setMessage("cert as base64: {}")
//        .addArgument(() -> Base64.toBase64String(certBytes))
//        .log();

}