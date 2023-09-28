package ro.gs1.quarkus.etcd;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

public class EtcdResourceLifecycleManager
   implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

   private static final Logger logger = Logger.getLogger(EtcdResourceLifecycleManager.class);

   public static final Integer ETCD_PORT = 2379;

   private Optional<String> containerNetworkId;

   GenericContainer etcdContainer;

   @Override
   public void setIntegrationTestContext(DevServicesContext context) {
      containerNetworkId = context.containerNetworkId();
   }

   @Override
   public Map<String, String> start() {
      containerNetworkId.ifPresent(id -> logger.debugv("Network id: {0}", id));
      etcdContainer = new GenericContainer(DockerImageName.parse("bitnami/etcd:latest"))
         .withEnv("ALLOW_NONE_AUTHENTICATION", "yes")
         .withEnv("ETCD_ADVERTISE_CLIENT_URLS", "http://localhost:2379")
         .withExposedPorts(ETCD_PORT)
         .waitingFor(Wait.forLogMessage(".*Starting etcd in background*\\n", 1));
      etcdContainer.start();
      logger.infov("ETCD host: {0}", buildEtcdHost(etcdContainer));
      logger.infov("ETCD port: {0}", buildEtcdPort(etcdContainer));
      return ImmutableMap.of("quarkus.etcd.host", buildEtcdHost(etcdContainer), "quarkus.etcd.port",
         String.valueOf(buildEtcdPort(etcdContainer)));
   }

   protected String buildEtcdHost(GenericContainer container) {
      return container.getHost();
   }

   protected Integer buildEtcdPort(GenericContainer container) {
      return container.getMappedPort(ETCD_PORT);
   }

   @Override
   public void stop() {
      if (etcdContainer != null) {
         logger.info("Stopping containers");
         etcdContainer.stop();
      }
   }
}
