package ro.comtec.etcd.quarkus.etcd.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;


@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.etcd")
public interface EtcdConfig {

   @WithDefault("localhost")
   String host();

   /**
    * ETCD Port
    */
   @WithDefault("2379")
   Integer port();



}
