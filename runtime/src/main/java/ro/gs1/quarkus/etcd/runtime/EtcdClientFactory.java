package ro.gs1.quarkus.etcd.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.vertx.core.Vertx;
import ro.gs1.quarkus.etcd.api.EtcdClientChannel;
import ro.gs1.quarkus.etcd.runtime.config.EtcdClientConfig;
import ro.gs1.quarkus.etcd.runtime.config.EtcdConfigProvider;

public class EtcdClientFactory {

   private EtcdClientFactory() {
   }

   @SuppressWarnings("unused")
   public static EtcdClientChannel createClient(String name) {
      ArcContainer container = Arc.container();
      InstanceHandle<EtcdConfigProvider> instance = container.instance(EtcdConfigProvider.class);
      if (!instance.isAvailable()) {
         throw new IllegalStateException("Unable to find the EtcdConfigProvider");
      }
      EtcdConfigProvider configProvider = instance.get();
      EtcdClientConfig config = configProvider.getConfiguration(name);
      if (config == null) {
         throw new IllegalStateException("etcd client " + name + " is missing configuration.");
      }
      Vertx vertx = container.instance(Vertx.class)
         .get();
      return new EtcdClientChannelVertx(name, config, vertx);
   }
}
