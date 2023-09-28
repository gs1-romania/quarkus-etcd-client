package ro.gs1.quarkus.etcd;

import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import io.etcd.jetcd.launcher.Etcd;
import io.etcd.jetcd.launcher.EtcdCluster;
import io.etcd.jetcd.launcher.EtcdContainer;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class EtcdResourceLifecycleManager
   implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

   private static final Logger logger = Logger.getLogger(EtcdResourceLifecycleManager.class);

   public static final Integer ETCD_PORT = 2379;

   private Optional<String> containerNetworkId;

   EtcdCluster etcdContainer;

   @Override
   public void setIntegrationTestContext(DevServicesContext context) {
      containerNetworkId = context.containerNetworkId();
   }

   @Override
   public Map<String, String> start() {
      containerNetworkId.ifPresent(id -> logger.debugv("Network id: {0}", id));
      etcdContainer = Etcd.builder()
         .build();
      etcdContainer.start();
      logger.infov("ETCD host: {0}", buildEtcdHost(etcdContainer));
      logger.infov("ETCD port: {0}", buildEtcdPort(etcdContainer));
      return ImmutableMap.of("quarkus.etcd.host", buildEtcdHost(etcdContainer),
         "quarkus.etcd.port", String.valueOf(buildEtcdPort(etcdContainer)));
   }

   protected String buildEtcdHost(EtcdCluster etcdCluster) {
      for (EtcdContainer container : etcdCluster.containers()) {
         return container.getHost();
      }
      return "localhost";
   }

   protected Integer buildEtcdPort(EtcdCluster etcdCluster) {
      for (EtcdContainer container : etcdCluster.containers()) {
         return container.getMappedPort(ETCD_PORT);
      }
      return ETCD_PORT;
   }

   @Override
   public void stop() {
      if (etcdContainer != null) {
         logger.info("Stopping containers");
         etcdContainer.stop();
      }
   }
}
