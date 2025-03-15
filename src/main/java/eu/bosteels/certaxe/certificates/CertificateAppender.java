package eu.bosteels.certaxe.certificates;

import eu.bosteels.certaxe.ct.LogList;

import java.sql.SQLException;

public interface CertificateAppender {

  void append(Certificate certificate, LogList list, long index) throws SQLException;

  void flush() throws SQLException;
  

}
