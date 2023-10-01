//@formatter:off
package ro.gs1.quarkus.etcd.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.configuration.PathConverter;
import io.smallrye.config.WithConverter;

import java.nio.file.Path;
import java.util.Optional;

/**
 * SSL/TLS configuration.
 */
@ConfigGroup
public interface EtcdSslConfig {
   /**
    * An optional key store which holds the keys and certificates for the Certificate Authentication.
    * The key store can be either on classpath or in an external file.
    */
   Optional<Jks> keyStore();

   /**
    * An optional trust store which holds the certificate information of the certificates to trust.
    * The trust store can be either on classpath or in an external file.
    */
   Optional<Jks> trustStore();

   @ConfigGroup
   interface Jks {

      /**
       * Path of this JKS.
       */
      @WithConverter(PathConverter.class)
      Path path();

      /**
       * Password of this JKS.
       * @return
       */
      Optional<String> password();

      /**
       * If there are multiple aliases in this JKS select only one.
       */
      Optional<String> alias();

      /**
       * Password for the alias.
       */
      Optional<String> aliasPassword();
   }


}
