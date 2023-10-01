package ro.gs1.quarkus.etcd.api;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifies an injected etcd client.
 */
@Qualifier
@Retention(RUNTIME)
@Target(value = { FIELD, PARAMETER, METHOD })
@Documented
public @interface EtcdClient {

   String DEFAULT_CLIENT_NAME = "<<default>>";

   /**
    * The name is used to configure the etcd client.
    *
    * @return the client name
    */
   String value() default DEFAULT_CLIENT_NAME;
}
