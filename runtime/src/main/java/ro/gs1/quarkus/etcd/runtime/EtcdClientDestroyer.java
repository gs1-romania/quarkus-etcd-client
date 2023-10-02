package ro.gs1.quarkus.etcd.runtime;

import io.quarkus.arc.BeanDestroyer;
import jakarta.enterprise.context.spi.CreationalContext;
import org.jboss.logging.Logger;
import ro.gs1.quarkus.etcd.api.EtcdClientChannel;

import java.util.Map;

public class EtcdClientDestroyer implements BeanDestroyer<EtcdClientChannel> {

   private static final Logger logger = Logger.getLogger(EtcdClientDestroyer.class);

   @Override
   public void destroy(EtcdClientChannel instance, CreationalContext<EtcdClientChannel> creationalContext,
      Map<String, Object> params) {
      if (instance instanceof EtcdClientChannelVertx) {
         try {
            EtcdClientChannelVertx etcdChannel = (EtcdClientChannelVertx) instance;
            logger.infov("Shutting down etcd client {0}", etcdChannel.getClientName());
            etcdChannel.close();
         } catch (InterruptedException e) {
            logger.warn("Unable to shutdown etcd client underlying gRPC channel after 10 seconds.", e);
         }
      }
   }
}
