package eu.bosteels.certaxe.certificates;

import com.google.common.net.InternetDomainName;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cryptacular.util.CertUtil;
import org.cryptacular.x509.ExtensionReader;
import org.cryptacular.x509.GeneralNameType;
import org.cryptacular.x509.dn.NameReader;
import org.slf4j.Logger;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.interfaces.DHPublicKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.ECParameterSpec;
import java.time.Instant;
import java.util.*;

import static javax.security.auth.x500.X500Principal.RFC2253;
import static org.cryptacular.x509.dn.StandardAttributeType.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Certificate objects hold all the data we want to save in our output files.
 */
@Data
@Builder
public class Certificate {


  // Have only seen version 3 (99%) and version 1
  private final int version;

  // The serial number is an integer assigned by the certification authority to each certificate.
  // It must be unique for each certificate issued by a given CA
  private String serialNumberHex;

  // We have seen these values:
  // RSAPublicKey,  EllipticCurvePublicKey, DSAPublicKey, Ed25519PublicKey
  // I suggest to strip off the "PublicKey" suffix
  private final String publicKeySchema;

  // number of bits used for public key
  private final int publicKeyLength;

  private final Instant notBefore;
  private final Instant notAfter;

  private final String issuer;
  private final String issuer_CN;
  private final String issuer_C;
  private final String issuer_O;
  private final String subject;
  private final String subject_C;
  private final String subject_CN;
  private final String subject_O;

  // common values are sha256, sha384, sha1, md5, sha512
  private final String signatureHashAlgorithm;

  // The sha256Fingerprint of signer
  private final String signedBy;

  private final String sha256Fingerprint;

  private final List<String> subjectAlternativeNames;

  private List<String> domainNames;
  private List<String> publicSuffixes;
  private List<String> registrableNames;
  private List<String> topPrivateDomains;
  private List<String> tlds;

  private int extensionCount;

  private String authorityKeyIdentifier;
  private String subjectKeyIdentifier; 

  private List<String> keyUsage;
  private List<String> extendedKeyUsage;
  private List<String> basicConstraints;
  private List<String> certificatePolicies;
  private List<String> crlDistributionPoints;
  private List<String> authorityInfoAccess; // or should this be a map ?
  private String ocsp;
  private Map<String, String> otherExtensions;
  private Boolean isCa;
  private BigInteger pathLenConstraint;

  /*
  TODO:

  int   extensionCount
  private List<String> basicConstraints;
  private List<String> certificatePolicies;
  private List<String> crlDistributionPoints;
  private String ocsp;
  private Map<String, String> otherExtensions;
   */

  private static final Logger logger = getLogger(Certificate.class);

  private final static Map<String, String> OID_MAP = new HashMap<>();
  static {
    // there are over 2.000 OIDs but for now we only care about these:
    OID_MAP.put("2.5.4.5", "SerialNumber");
    OID_MAP.put("1.3.6.1.4.1.311.60.2.1.1", "JurisdictionOfIncorporationL");
    OID_MAP.put("1.3.6.1.4.1.311.60.2.1.2", "JurisdictionOfIncorporationSP");
    OID_MAP.put("1.3.6.1.4.1.311.60.2.1.3", "JurisdictionOfIncorporationC");
    OID_MAP.put("2.5.4.15", "BusinessCategory");
    OID_MAP.put("2.5.4.16", "PostalAddress");
  }

  public static Certificate from(X509Certificate cert) throws CertificateParsingException {
    PublicKey pubKey = cert.getPublicKey();
    ExtensionReader er = new ExtensionReader(cert);
    NameReader nr = new NameReader(cert);
    
//    List<PolicyInformation> pol = er.readCertificatePolicies();
//    for (PolicyInformation policyInformation : pol) {
//      logger.info("policyInformation = {}", policyInformation);
//    }
//    for (KeyPurposeId keyPurposeId : er.readExtendedKeyUsage()) {
//      logger.info("keyPurposeId = {}", keyPurposeId);
//    }

    List<String> subjectNames = CertUtil.subjectNames(cert, GeneralNameType.DNSName, GeneralNameType.IPAddress);
    List<String> domainNames = CertUtil.subjectNames(cert, GeneralNameType.DNSName);
    Set<String> publicSuffixes = new HashSet<>();
    Set<String> registerableNames = new HashSet<>();
    Set<String> topPrivateDomains = new HashSet<>();
    Set<String> tlds = new HashSet<>();

    for (String subjectName : subjectNames) {
      var dn = InternetDomainName.from(subjectName);
      if (dn.hasPublicSuffix() && dn.publicSuffix() != null) {
        publicSuffixes.add(dn.publicSuffix().toString());
        registerableNames.add(dn.topDomainUnderRegistrySuffix().toString());
        topPrivateDomains.add(dn.topPrivateDomain().toString());
        var parent = dn;
        while (parent.hasParent()) {
            parent = parent.parent();
        }
        tlds.add(parent.toString());
      }
    }

    // check out
    // https://www.cryptacular.org/javadocs/org/cryptacular/x509/dn/StandardAttributeType.html
    // maybe we also read postalcode, TelephoneNumber, ...

    //KeyUsage keyUsage = er.readKeyUsage();

//    List<String> keyUsages = Arrays.stream(KeyUsageBits.values())
//        .filter(s -> s.isSet(keyUsage))
//        .map(Enum::name)
//        .toList();

   // List<String> extendedKeyUsage = er.readExtendedKeyUsage().stream().map(KeyPurposeId::getId).toList();

//    BigInteger pathLenConstraint = er.readBasicConstraints().getPathLenConstraint();

    //    Authority Information Access:
    //    OCSP - URI:http://ocsp.digicert.com
    //    CA Issuers - URI:http://cacerts.digicert.com/DigiCertSHA2ExtendedValidationServerCA.crt

//    List<String> authorityInfoAccess = er.readAuthorityInformationAccess().stream()
//        .map(AccessDescription::getAccessLocation)
//        .map(org.bouncycastle.asn1.x509.GeneralName::toString)
//        .toList();

    return Certificate.builder()
        .version                (   cert.getVersion())
        .serialNumberHex        (   convertBigIntegerToHexString(cert.getSerialNumber()))
        .publicKeySchema        (   pubKey.getAlgorithm())
        .publicKeyLength        (   getKeyLength(pubKey))
        .notBefore              (   cert.getNotBefore().toInstant())
        .notAfter               (   cert.getNotAfter().toInstant())
        .issuer                 (   cert.getIssuerX500Principal().getName())
        .issuer_CN              (   nr.readIssuer().getValue(CommonName))
        .issuer_C               (   nr.readIssuer().getValue(CountryName))
        .issuer_O               (   nr.readIssuer().getValue(OrganizationName))
        .subject                (   cert.getSubjectX500Principal().getName(RFC2253, OID_MAP))
        .subject_CN             (    nr.readSubject().getValue(CommonName))
        .subject_C              (    nr.readSubject().getValue(CountryName))
        .subject_O              (   nr.readSubject().getValue(OrganizationName))
        .signatureHashAlgorithm (   cert.getSigAlgName())
        .sha256Fingerprint      (   sha256_fingerprint(cert))
        .subjectAlternativeNames(   subjectNames )
        .authorityKeyIdentifier (   CertUtil.authorityKeyId(cert)     )
        .subjectKeyIdentifier   (   subjectKeyId(cert)       )
        .isCa                   (   er.readBasicConstraints().isCA()  )
        .publicSuffixes         (   new ArrayList<>(publicSuffixes))
        .domainNames            (   domainNames )
        .registrableNames       (   new ArrayList<>(registerableNames)  )
        .topPrivateDomains      (   new ArrayList<>(topPrivateDomains)  )
        .tlds                   (   new ArrayList<>(tlds)  )
       // .keyUsage               (   keyUsages )
    //    .extendedKeyUsage       (   extendedKeyUsage )
     //   .authorityInfoAccess    (   authorityInfoAccess )
     //   .pathLenConstraint      (   pathLenConstraint )
        .build();
  }

  private static String subjectKeyId(X509Certificate cert) {
    try {
      return CertUtil.subjectKeyId(cert);
    } catch (NullPointerException e) {
      return "";
    }
  }

  public String prettyString() {
    return new StringJoiner(",\n ", Certificate.class.getSimpleName() + "[\n", "]")
        .add("sha256Fingerprint=" + sha256Fingerprint)
        .add("version=" + version)
//        .add("serialNumberHex=" + serialNumberHex)
//        .add("publicKeySchema='" + publicKeySchema + "'")
//        .add("publicKeyLength=" + publicKeyLength)
        .add("notBefore=" + notBefore)
        .add("notAfter=" + notAfter)
//        .add("issuer='" + issuer + "'")
//        .add("subject='" + subject + "'")
//        .add("signatureHashAlgorithm='" + signatureHashAlgorithm + "'")
//        .add("signedBy=" + signedBy)
//        .add("subjectAlternativeNames=" + subjectAlternativeNames)
        .add("registrableNames=" + registrableNames)
        .add("publicSuffixes=" + publicSuffixes)
        .add("tlds=" + tlds)
//        .add("keyUsage=" + keyUsage)
//        .add("authorityInfoAccess=" + authorityInfoAccess)
        .toString();
  }

  public static int getKeyLength(PublicKey pubKey) {
    if (pubKey instanceof RSAPublicKey rsaPublicKey) {
      return rsaPublicKey.getModulus().bitLength();
    }
    if (pubKey instanceof final DSAPublicKey dsaPublicKey) {
      return dsaPublicKey.getY().bitLength();
    }
    if (pubKey instanceof ECPublicKey ecPublicKey) {
      ECParameterSpec params = ecPublicKey.getParams();
      if (params != null) {
        return params.getOrder().bitLength();
      }
    }
    if (pubKey instanceof DHPublicKey dhPublicKey) {
      return dhPublicKey.getY().bitLength();
    }
    if (pubKey instanceof XECPublicKey xecPublicKey) {
      return xecPublicKey.getU().bitLength();
    }
    logger.info("Unknown public key type: alg={} class={}", pubKey.getAlgorithm(), pubKey.getClass());
    return 0;
  }

  @SuppressWarnings("unused")
  public static boolean isEV() {
    /*
    https://en.wikipedia.org/wiki/Extended_Validation_Certificate
    EV certificates are standard X.509 digital certificates.
    
    The primary way to identify an EV certificate is by referencing the Certificate Policies extension field.
    Each issuer uses a different object identifier (OID) in this field to identify their EV certificates,
    and each OID is documented in the issuer's Certification Practice Statement.
    As with root certificate authorities in general, browsers may not recognize all issuers.

    EV HTTPS certificates contain a subject with X.509 OIDs for
      jurisdictionOfIncorporationCountryName (OID: 1.3.6.1.4.1.311.60.2.1.3),[12]
      jurisdictionOfIncorporationStateOrProvinceName (OID: 1.3.6.1.4.1.311.60.2.1.2) (optional),
      jurisdictionLocalityName (OID: 1.3.6.1.4.1.311.60.2.1.1) (optional),[14]
      businessCategory (OID: 2.5.4.15)[15] and
      serialNumber (OID: 2.5.4.5),[16]
      with the serialNumber pointing to the ID at the relevant secretary of state (US) or government business registrar (outside US)
      as well as a CA-specific policy identifier so that EV-aware software, such as a web browser,
      can recognize them.
      This identifier is what defines EV certificate and is the difference with OV certificate.
     */
    throw new RuntimeException("Not Implemented yet");
  }

  public static String sha256_fingerprint(X509Certificate x509Certificate) {
    try {
      byte[] encodedCertificate = x509Certificate.getEncoded();
      return DigestUtils.sha256Hex(encodedCertificate);
    } catch (CertificateEncodingException e) {
      logger.error("Encoding exception in certificate {}", x509Certificate);
      return "Encoding exception: " + e.getMessage();
    }
  }

  public static List<String> getSubjectAlternativeNames(X509Certificate x509Certificate) throws CertificateParsingException {
    List<String> subjectAlternativeNames = new ArrayList<>();
    try {
      Collection<List<?>> altNames = x509Certificate.getSubjectAlternativeNames();
      if (altNames != null) {
        for (List<?> altName : altNames) {
          if (altName.size() < 2)
            continue;
          int type = (int) altName.get(0);
          Object data = altName.get(1);
          switch(type) {
            case GeneralName.dNSName:
            case GeneralName.iPAddress:
              if (data instanceof String) {
                subjectAlternativeNames.add(data.toString());
              }
              break;
            default:
              logger.debug("Subject Alt Name of type {} with value {}", type, data);
          }
        }
      }
    } catch (CertificateParsingException e) {
      logger.error("Could not parse SAN's from certificate {} because of {}", x509Certificate, e.getMessage());
      throw e;
    }
    return subjectAlternativeNames;
  }

  public static String convertBigIntegerToHexString(BigInteger bigInteger){
    if (bigInteger == null || bigInteger.compareTo(BigInteger.valueOf(0)) < 0){
      return null;
    }
    String hexString = bigInteger.toString(16);
    if (hexString.length() % 2 == 1){
      hexString = "0" + hexString;
    }
    hexString = hexString.replaceAll("(.{2})", "$1:");
    if (hexString.endsWith(":")){
      hexString = hexString.substring(0, hexString.length() - 1);
    }
    return hexString;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        //.append("sha256Fingerprint", sha256Fingerprint)
        .append("registrableNames", registrableNames)
        .toString();
  }
}
