package ro.gs1.quarkus.etcd.runtime.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class EtcdConfigProvider {

   @Inject
   EtcdConfig config;

   public EtcdClientConfig getConfiguration(String clientName) {
      Map<String, EtcdClientConfig> clients = config.clients();
      if (clients == null) {
         return null;
      } else {
         return clients.get(clientName);
      }
   }
}
