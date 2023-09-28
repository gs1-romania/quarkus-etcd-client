package ro.gs1.quarkus.etcd;

import io.grpc.*;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import ro.gs1.quarkus.etcd.api.*;
import ro.gs1.quarkus.etcd.api.lock.Lock;
import ro.gs1.quarkus.etcd.api.lock.LockClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class EtcdClient {

   private static final Logger logger = Logger.getLogger(EtcdClient.class);

   private final Vertx vertx;

   private final Channel channel;

   private final EtcdConfig etcdConfig;

   private String token;

   private final Object lock;

   @Inject
   public EtcdClient(Vertx vertx, EtcdConfig etcdConfig) {
      this.vertx = vertx;
      this.etcdConfig = etcdConfig;
      this.channel = createGrpcVertxClient();
      this.lock = new Object();
   }

   @Produces
   @Etcd
   @ApplicationScoped
   public KV getKVClient() {
      return new KVClient("KVClient", channel, (a, b) -> b);
   }

   @Produces
   @Etcd
   @ApplicationScoped
   public Lease getLeaseClient() {
      return new LeaseClient("LeaseClient", channel, (a, b) -> b);
   }

   @Produces
   @Etcd
   @ApplicationScoped
   public Lock getLockClient() {
      return new LockClient("LockClient", channel, (a, b) -> b);
   }

   @Produces
   @Etcd
   @ApplicationScoped
   public Maintenance getMaintenanceClient() {
      return new MaintenanceClient("MaintenanceClient", channel, (a, b) -> b);
   }

   @Produces
   @Etcd
   @ApplicationScoped
   public Watch getWatchClient() {
      return new WatchClient("WatchClient", channel, (a, b) -> b);
   }

   @Produces
   @Etcd
   @ApplicationScoped
   public Cluster getClusterClient() {
      return new ClusterClient("ClusterClient", channel, (a, b) -> b);
   }


   private Channel createGrpcVertxClient() {
      VertxChannelBuilder vertxChannelBuilder = VertxChannelBuilder.forAddress(vertx, etcdConfig.host(),
         etcdConfig.port());
      vertxChannelBuilder.intercept(new AuthTokenInterceptor());
      // TODO: add TLS support.
      vertxChannelBuilder.usePlaintext();
      if (etcdConfig.keepAliveTime()
         .isPresent()) {
         vertxChannelBuilder.keepAliveTime(etcdConfig.keepAliveTime()
            .get()
            .toMillis(), TimeUnit.MILLISECONDS);
      }
      if (etcdConfig.keepAliveTimeout()
         .isPresent()) {
         vertxChannelBuilder.keepAliveTimeout(etcdConfig.keepAliveTimeout()
            .get()
            .toMillis(), TimeUnit.MILLISECONDS);
      }
      if (etcdConfig.keepAliveWithoutCalls()
         .isPresent()) {
         vertxChannelBuilder.keepAliveWithoutCalls(etcdConfig.keepAliveWithoutCalls()
            .get());
      }
      if (etcdConfig.defaultLoadBalancingPolicy()
         .isPresent()) {
         vertxChannelBuilder.defaultLoadBalancingPolicy(etcdConfig.defaultLoadBalancingPolicy()
            .get());
      }
      if (etcdConfig.maxInboundMessageSize()
         .isPresent()) {
         vertxChannelBuilder.maxInboundMessageSize(etcdConfig.maxInboundMessageSize()
            .get());
      }
      logger.debugv("Created GRPC Vert.x Channel for endpoint: {0}:{1}.", etcdConfig.host(), etcdConfig.port()
         .toString());
      return vertxChannelBuilder.build();
   }

   private String getToken(Channel channel) {
      if (token == null) {
         synchronized (lock) {
            if (token == null) {
               token = generateToken(channel);
            }
         }
      }
      return token;
   }

   void forceTokenRefresh() {
      synchronized (lock) {
         token = null;
      }
   }

   private void refreshToken(Channel channel) {
      synchronized (lock) {
         token = generateToken(channel);
      }
   }

   private String generateToken(Channel channel) {
      if (etcdConfig.name()
         .isEmpty() || etcdConfig.password()
         .isEmpty()) {
         return null;
      }
      MutinyAuthGrpc.MutinyAuthStub mutinyAuthStub = MutinyAuthGrpc.newMutinyStub(channel);
      Uni<AuthenticateResponse> authenticate = mutinyAuthStub.authenticate(AuthenticateRequest.newBuilder()
         .setName(etcdConfig.name()
            .get())
         .setPassword(etcdConfig.password()
            .get())
         .build());
      AuthenticateResponse authenticateResponse = authenticate.await()
         .atMost(etcdConfig.authenticationTimeout()
            .isPresent() ? etcdConfig.authenticationTimeout()
            .get() : Duration.ofSeconds(5));
      return authenticateResponse.getToken();
   }

   private class AuthTokenInterceptor implements ClientInterceptor {

      @Override
      public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
         CallOptions callOptions, Channel next) {
         return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
               String generatedToken = getToken(next);
               if (generatedToken != null) {
                  Metadata.Key<String> tokenMetadata = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER);
                  headers.put(tokenMetadata, generatedToken);
               }
               super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {

                  @Override
                  public void onClose(Status status, Metadata trailers) {
                     if (status.getCode() == Status.Code.UNAUTHENTICATED && status.getDescription() != null
                        && status.getDescription()
                        .contains("invalid auth token")) {
                        refreshToken(next);
                     }
                     super.onClose(status, trailers);
                  }
               }, headers);
            }
         };
      }
   }
}
