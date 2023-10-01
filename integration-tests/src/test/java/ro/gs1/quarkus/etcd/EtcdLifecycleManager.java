package ro.gs1.quarkus.etcd;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class EtcdLifecycleManager implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

   private static final Logger logger = Logger.getLogger(EtcdLifecycleManager.class);

   public static final Integer ETCD_PORT = 2379;

   private Optional<String> containerNetworkId;

   private GenericContainer<?> etcdContainerNoTlsNoAuth;

   private GenericContainer<?> etcdContainerWithTlsCertAuth;

   @Override
   public void setIntegrationTestContext(DevServicesContext context) {
      containerNetworkId = context.containerNetworkId();
   }

   @Override
   public Map<String, String> start() {
      try {
         generateCertificates();
      } catch (Exception e) {
         logger.error(e);
         throw new RuntimeException(e);
      }
      containerNetworkId.ifPresent(id -> logger.debugv("Network id: {0}", id));
      etcdContainerNoTlsNoAuth = new GenericContainer<>("gcr.io/etcd-development/etcd:v3.4.27").withExposedPorts(
            ETCD_PORT)
         .withCommand("/usr/local/bin/etcd", "--name", "etcd0", "--advertise-client-urls", "http://0.0.0.0:2379",
            "--listen-client-urls", "http://0.0.0.0:2379")
         .waitingFor(Wait.forLogMessage(".*ready to serve client requests.*", 1));
      etcdContainerWithTlsCertAuth = new GenericContainer<>(
         new ImageFromDockerfile().withFileFromPath("server.crt", Path.of("target/server.crt"))
            .withFileFromPath("server.key", Path.of("target/server.key"))
            .withFileFromClasspath("Dockerfile", "/ro/gs1/quarkus/etcd/Dockerfile")).withExposedPorts(ETCD_PORT)
         .withCommand("/usr/local/bin/etcd", "--name", "infra0", "--data-dir", "infra0", "--client-cert-auth",
            "--trusted-ca-file=/etc/ssl/etcd/ca-certificate.crt", "--cert-file=/etc/ssl/etcd/certificate.crt",
            "--key-file=/etc/ssl/etcd/private-key.key", "--advertise-client-urls", "https://0.0.0.0:2379",
            "--listen-client-urls", "https://0.0.0.0:2379")
         .waitingFor(Wait.forLogMessage(".*ready to serve client requests.*", 1));
      containerNetworkId.ifPresent(etcdContainerNoTlsNoAuth::withNetworkMode);
      containerNetworkId.ifPresent(etcdContainerWithTlsCertAuth::withNetworkMode);
      etcdContainerNoTlsNoAuth.start();
      etcdContainerWithTlsCertAuth.start();
      logger.info(etcdContainerNoTlsNoAuth.getLogs());
      logger.infov("etcdContainerNoTlsNoAuth endpoint: {0}:{1}", buildEtcdHost(etcdContainerNoTlsNoAuth),
         buildEtcdPort(etcdContainerNoTlsNoAuth));
      logger.infov("etcdContainerWithTlsCertAuth endpoint: {0}:{1}", buildEtcdHost(etcdContainerWithTlsCertAuth),
         buildEtcdPort(etcdContainerWithTlsCertAuth));
      Map<String, String> conf = new HashMap<>();
      conf.put("quarkus.etcd.clientNoTlsNoAuth.host", buildEtcdHost(etcdContainerNoTlsNoAuth));
      conf.put("quarkus.etcd.clientNoTlsNoAuth.port", String.valueOf(buildEtcdPort(etcdContainerNoTlsNoAuth)));
      conf.put("quarkus.etcd.clientWithTlsCertAuth.host", buildEtcdHost(etcdContainerWithTlsCertAuth));
      conf.put("quarkus.etcd.clientWithTlsCertAuth.port", String.valueOf(buildEtcdPort(etcdContainerWithTlsCertAuth)));
      conf.put("quarkus.etcd.clientWithTlsCertAuth.ssl-config.key-store.path", Path.of("target/client.keystore")
         .toAbsolutePath()
         .toString());
      conf.put("quarkus.etcd.clientWithTlsCertAuth.ssl-config.key-store.password", "123456");
      conf.put("quarkus.etcd.clientWithTlsCertAuth.ssl-config.key-store.alias", "client");
      conf.put("quarkus.etcd.clientWithTlsCertAuth.ssl-config.key-store.alias-password", "222");
      conf.put("quarkus.etcd.clientWithTlsCertAuth.ssl-config.trust-store.path", Path.of("target/server.keystore")
         .toAbsolutePath()
         .toString());
      conf.put("quarkus.etcd.clientWithTlsCertAuth.sslConfig.trust-store.password", "123456");
      return conf;
   }

   protected String buildEtcdHost(GenericContainer<?> container) {
      return container.getHost();
   }

   protected Integer buildEtcdPort(GenericContainer<?> container) {
      return container.getMappedPort(ETCD_PORT);
   }

   @Override
   public void stop() {
      logger.info("Stopping containers");
      if (etcdContainerNoTlsNoAuth != null) {
         etcdContainerNoTlsNoAuth.stop();
      }
      if (etcdContainerWithTlsCertAuth != null) {
         etcdContainerWithTlsCertAuth.stop();
      }
   }

   protected void generateCertificates() throws Exception {
      Security.addProvider(new BouncyCastleProvider());
      KeyPair keyPairServer = generateKeyPair();
      KeyPair keyPairClient = generateKeyPair();
      X509Certificate serverCert = selfSign(keyPairServer.getPrivate(), keyPairServer.getPublic(),
         "CN=localhost,O=Server,C=RO", "CN=localhost,O=Server,C=RO", "localhost", "127.0.0.1", true);
      X509Certificate clientCert = selfSign(keyPairServer.getPrivate(), keyPairClient.getPublic(),
         "CN=localhost,O=Server,C=RO", "CN=localhost,O=Client,C=RO", "localhost", "127.0.0.1", false);
      Files.write(Path.of("target/server.crt"), convertToBase64PEMString(serverCert).getBytes());
      Files.write(Path.of("target/server.pub"), convertToBase64PEMString(keyPairServer.getPublic()).getBytes());
      Files.write(Path.of("target/server.key"), convertToBase64PEMString(keyPairServer.getPrivate()).getBytes());
      Files.write(Path.of("target/client.crt"), convertToBase64PEMString(clientCert).getBytes());
      Files.write(Path.of("target/client.pub"), convertToBase64PEMString(keyPairClient.getPublic()).getBytes());
      Files.write(Path.of("target/client.key"), convertToBase64PEMString(keyPairClient.getPrivate()).getBytes());
      KeyStore keyStoreClient = KeyStore.getInstance("JKS");
      keyStoreClient.load(null, null);
      keyStoreClient.setKeyEntry("client", keyPairClient.getPrivate(), "222".toCharArray(),
         new Certificate[] { clientCert });
      keyStoreClient.store(new FileOutputStream(Path.of("target/client.keystore")
         .toFile()), "123456".toCharArray());
      KeyStore keyStoreServer = KeyStore.getInstance("JKS");
      keyStoreServer.load(null, null);
      keyStoreServer.setCertificateEntry("server", serverCert);
      keyStoreServer.store(new FileOutputStream(Path.of("target/server.keystore")
         .toFile()), "123456".toCharArray());
   }

   public String convertToBase64PEMString(X509Certificate x509Cert) throws IOException {
      StringWriter sw = new StringWriter();
      try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
         pw.writeObject(x509Cert);
      }
      return sw.toString();
   }

   public String convertToBase64PEMString(PublicKey publicKey) throws IOException {
      StringWriter sw = new StringWriter();
      try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
         pw.writeObject(publicKey);
      }
      return sw.toString();
   }

   public String convertToBase64PEMString(PrivateKey privateKey) throws IOException {
      StringWriter sw = new StringWriter();
      try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
         pw.writeObject(privateKey);
      }
      return sw.toString();
   }

   private X509Certificate selfSign(PrivateKey signer, PublicKey publicKey, String issuer, String subject,
      String dnsName, String ip, boolean ca) throws CertificateException, CertIOException, OperatorCreationException {
      Provider bcProvider = new BouncyCastleProvider();
      Security.addProvider(bcProvider);
      long now = System.currentTimeMillis();
      Date startDate = new Date(now);
      X500Name issuerName = new X500Name(issuer);
      X500Name subjectName = new X500Name(subject);
      BigInteger certSerialNumber = new BigInteger(Long.toString(now));
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(startDate);
      calendar.add(Calendar.YEAR, 30);
      Date endDate = calendar.getTime();
      String signatureAlgorithm = "SHA256WithRSA";
      ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(signer);
      JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuerName, certSerialNumber, startDate,
         endDate, subjectName, publicKey);
      List<GeneralName> altNames = new ArrayList<>();
      altNames.add(new GeneralName(GeneralName.iPAddress, ip));
      altNames.add(new GeneralName(GeneralName.dNSName, dnsName));
      GeneralNames subjectAltNames = GeneralNames.getInstance(new DERSequence(altNames.toArray(new GeneralName[] {})));
      certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
      BasicConstraints basicConstraints = new BasicConstraints(ca);
      certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);
      return new JcaX509CertificateConverter().setProvider(bcProvider)
         .getCertificate(certBuilder.build(contentSigner));
   }

   private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048, new SecureRandom());
      return keyPairGenerator.generateKeyPair();
   }
}
