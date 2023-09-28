package ro.comtec.etcd.quarkus.etcd;

import io.grpc.Channel;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import ro.comtec.etcd.quarkus.etcd.config.EtcdConfig;
import ro.comtec.etcd.quarkus.etcd.kv.KV;
import ro.comtec.etcd.quarkus.etcd.kv.KVClient;

@ApplicationScoped
public class EtcdClient {

   private static final Logger logger = Logger.getLogger(EtcdClient.class);

   private final Vertx vertx;

   private final Channel channel;

   private final EtcdConfig etcdConfig;

   @Inject
   public EtcdClient(Vertx vertx, EtcdConfig etcdConfig) {
      this.vertx = vertx;
      this.etcdConfig = etcdConfig;
      this.channel = createGrpcVertxClient();
   }

   @Produces
   @ApplicationScoped
   public KV getKVClient() {
      return new KVClient(channel);
   }

   private Channel createGrpcVertxClient() {
      VertxChannelBuilder vertxChannelBuilder = VertxChannelBuilder.forAddress(vertx, etcdConfig.host(),
         etcdConfig.port());
      //vertxChannelBuilder.intercept(new AuthTokenInterceptor());
      // TODO: add TLS support.
      vertxChannelBuilder.usePlaintext();


      logger.debugv("Created GRPC Vert.x Channel for endpoint: {0}:{1}.", etcdConfig.host(), etcdConfig.port()
         .toString());
      return vertxChannelBuilder.build();
   }
}
