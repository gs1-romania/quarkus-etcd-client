package ro.gs1.quarkus.etcd.runtime.config;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

import java.util.Map;

/**
 * ETCD Configuration root.
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.etcd")
public interface EtcdConfig {

   /**
    * Configures the etcd clients.
    */
   @WithParentName
   Map<String, EtcdClientConfig> clients();
}
