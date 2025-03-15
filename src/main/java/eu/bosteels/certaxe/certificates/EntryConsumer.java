package eu.bosteels.certaxe.certificates;

import eu.bosteels.certaxe.ct.Entry;
import eu.bosteels.certaxe.observability.Event;
import eu.bosteels.certaxe.observability.ProgressDatabase;
import org.cryptacular.util.CertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EntryConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EntryConsumer.class);
  private final BlockingQueue<Entry> queue;
  private final CertificateAppender appender;
  private final ProgressDatabase progressDatabase;
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  public final AtomicInteger parsed = new AtomicInteger(0);

  public EntryConsumer(BlockingQueue<Entry> queue, CertificateAppender appender, ProgressDatabase progressDatabase) {
    this.queue = queue;
    this.appender = appender;
    this.progressDatabase = progressDatabase;
  }

  public void start() throws InterruptedException {
    while (!stopped.get()) {
      logger.atInfo().setMessage("Polling while queue has size {}").addArgument(queue::size).log();
      Entry entry = queue.poll(500, TimeUnit.MILLISECONDS);
      if (entry == null) {
        logger.debug("No entry found in the queue, polling again ...");
      } else {
        logger.info("Found an entry. Now queue.size: {} ", queue.size());
        Instant start = Instant.now();
        Certificate certificate = extractCertificate(entry);
        if (certificate != null) {
          save(entry, certificate);
          progressDatabase.append(Event.appended(start, entry));
        }
      }
    }
  }

  public Certificate extractCertificate(Entry entry) {
    if (entry == null) {
      return null;
    }
    try {
      X509Certificate x509Certificate = parse(entry);
      if (x509Certificate != null) {
        Certificate certificate = Certificate.from(x509Certificate);
        logger.debug("certificate = {}", certificate);
        return certificate;
      }
    } catch (IOException e) {
      // we log it and carry on with the other entries in the queue
      logger.error("Failed to read certificate: " + e.getMessage(), e);
    } catch (CertificateParsingException e) {
      logger.error("Failed to parse certificate", e);
    } catch (Exception e) {
      logger.error("Failed to extract certificate", e);
    }
    return null;
  }
  
  public void save(Entry entry, Certificate certificate) {
    try {
      appender.append(certificate, entry.getInterval().getList(), entry.getIndex());
      logger.debug("appended cert with index {}", entry.getIndex());
    } catch (SQLException e) {
      logger.atError()
          .setMessage("SQLException while saving cert {} from {}")
          .addArgument(entry::getIndex)
          .addArgument(entry::getLogList)
          .setCause(e)
          .log();
    }

  }


  public void stop() {
    stopped.set(true);
  }

  public X509Certificate parse(Entry entry) throws IOException {
    // TODO : process  extra_data
    //byte[] extra = entry.getExtra_data();
    DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(entry.getLeaf_input()));
    int version = dataInput.readUnsignedByte();
    int merkleLeafType = dataInput.readUnsignedByte();
    long ts = dataInput.readLong();
    Instant instant = Instant.ofEpochMilli(ts);
    int logEntryType = dataInput.readUnsignedShort();
    logger.debug("logEntryType = {}", logEntryType);

    // X509LogEntryType=0, PrecertLogEntryType=1
    if (logEntryType == 1) {
      return null;
    }

    logger.debug("version={}, merkleLeafType={}, instant={}, logEntryType={}", version, merkleLeafType, instant, logEntryType);

    int b1 = dataInput.readUnsignedByte();
    int b2 = dataInput.readUnsignedByte();
    int b3 = dataInput.readUnsignedByte();
    int certLength = b1 * 256 * 256 + b2 * 256 + b3;

    //logger.debug("certLength = {}", certLength);

    byte[] certBytes = new byte[certLength];
    dataInput.readFully(certBytes);

    logger.debug("We have read {} bytes", certBytes.length);
    //dump(certBytes);

    try {
      X509Certificate cert = CertUtil.decodeCertificate(certBytes);
      logger.debug("Parsed {} bytes to obtain X509Certificate", certLength);
      int total = parsed.incrementAndGet();
      logger.debug("total = {}", total);



      return cert;
    } catch (Exception e) {
      logger.info("Could not decode", e);
      return null;
    }
  }

  private void dump(byte[] certBytes) {
    int certNr = parsed.get();
    try {
      FileCopyUtils.copy(certBytes, Files.newOutputStream(Path.of("cert." + certNr + ".cert")));
    } catch (IOException e) {
      logger.info("Could not save cert: ", e);
    }
  }

}
