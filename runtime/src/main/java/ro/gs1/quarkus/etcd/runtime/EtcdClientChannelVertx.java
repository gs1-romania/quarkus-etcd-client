package ro.gs1.quarkus.etcd.runtime;

import io.grpc.*;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.net.JksOptions;
import io.vertx.grpc.VertxChannelBuilder;
import org.jboss.logging.Logger;
import ro.gs1.quarkus.etcd.api.*;
import ro.gs1.quarkus.etcd.api.lock.Lock;
import ro.gs1.quarkus.etcd.api.lock.LockClient;
import ro.gs1.quarkus.etcd.runtime.config.EtcdClientConfig;
import ro.gs1.quarkus.etcd.runtime.config.EtcdSslConfig;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class EtcdClientChannelVertx implements EtcdClientChannel {

   private static final Logger logger = Logger.getLogger(EtcdClientChannelVertx.class);

   private final EtcdClientConfig config;

   private final ManagedChannel channel;

   private final Vertx vertx;

   private final Object lock;

   private final String clientName;

   private String token;

   public EtcdClientChannelVertx(String clientName, EtcdClientConfig config, Vertx vertx) {
      this.config = config;
      this.vertx = vertx;
      this.clientName = clientName;
      this.lock = new Object();
      this.channel = createGrpcVertxClient();
   }

   private ManagedChannel createGrpcVertxClient() {
      VertxChannelBuilder vertxChannelBuilder = VertxChannelBuilder.forAddress(vertx, config.host(), config.port());
      vertxChannelBuilder.intercept(new AuthTokenInterceptor());
      if (config.sslConfig()
         .keyStore()
         .isEmpty() && config.sslConfig()
         .trustStore()
         .isEmpty()) {
         vertxChannelBuilder.usePlaintext();
      } else {
         vertxChannelBuilder.useSsl(event -> {
            event.setSsl(true);
            if (config.sslConfig()
               .keyStore()
               .isPresent()) {
               event.setKeyStoreOptions(buildJksOptions(config.sslConfig()
                  .keyStore()
                  .get()));
            }
            if (config.sslConfig()
               .trustStore()
               .isPresent()) {
               event.setTrustStoreOptions(buildJksOptions(config.sslConfig()
                  .trustStore()
                  .get()));
            }
         });
      }
      if (config.keepAliveTime()
         .isPresent()) {
         vertxChannelBuilder.keepAliveTime(config.keepAliveTime()
            .get()
            .toMillis(), TimeUnit.MILLISECONDS);
      }
      if (config.keepAliveTimeout()
         .isPresent()) {
         vertxChannelBuilder.keepAliveTimeout(config.keepAliveTimeout()
            .get()
            .toMillis(), TimeUnit.MILLISECONDS);
      }
      if (config.keepAliveWithoutCalls()
         .isPresent()) {
         vertxChannelBuilder.keepAliveWithoutCalls(config.keepAliveWithoutCalls()
            .get());
      }
      if (config.defaultLoadBalancingPolicy()
         .isPresent()) {
         vertxChannelBuilder.defaultLoadBalancingPolicy(config.defaultLoadBalancingPolicy()
            .get());
      }
      if (config.maxInboundMessageSize()
         .isPresent()) {
         vertxChannelBuilder.maxInboundMessageSize(config.maxInboundMessageSize()
            .get());
      }
      if (config.authority()
         .isPresent()) {
         vertxChannelBuilder.overrideAuthority(config.authority()
            .get());
      }
      logger.debugv("Created GRPC Vert.x Channel for endpoint: {0}:{1}.", config.host(), config.port()
         .toString());
      return vertxChannelBuilder.build();
   }

   private JksOptions buildJksOptions(EtcdSslConfig.Jks jks) {
      JksOptions jksOptions = new JksOptions();
      jksOptions.setPath(jks.path()
         .toString());
      if (jks.password()
         .isPresent()) {
         jksOptions.setPassword(jks.password()
            .get());
      }
      if (jks.alias()
         .isPresent()) {
         jksOptions.setAlias(jks.alias()
            .get());
      }
      if (jks.aliasPassword()
         .isPresent()) {
         jksOptions.setAliasPassword(jks.aliasPassword()
            .get());
      }
      return jksOptions;
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

   public void forceTokenRefresh() {
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
      if (config.name()
         .isEmpty() || config.password()
         .isEmpty()) {
         return null;
      }
      MutinyAuthGrpc.MutinyAuthStub mutinyAuthStub = MutinyAuthGrpc.newMutinyStub(channel);
      Uni<AuthenticateResponse> authenticate = mutinyAuthStub.authenticate(AuthenticateRequest.newBuilder()
         .setName(config.name()
            .get())
         .setPassword(config.password()
            .get())
         .build());
      AuthenticateResponse authenticateResponse = authenticate.await()
         .atMost(config.authenticationTimeout()
            .isPresent() ? config.authenticationTimeout()
            .get() : Duration.ofSeconds(5));
      return authenticateResponse.getToken();
   }

   public ManagedChannel getChannel() {
      return channel;
   }

   public void close() throws InterruptedException {
      synchronized (lock) {
         if (channel != null) {
            channel.shutdownNow().awaitTermination(10, TimeUnit.SECONDS);
         }
      }
   }

   @Override
   public KV getKVClient() {
      return new KVClient("KVClient[" + clientName + "]", channel, (a, b) -> b);
   }

   @Override
   public Lease getLeaseClient() {
      return new LeaseClient("LeaseClient[" + clientName + "]", channel, (a, b) -> b);
   }

   @Override
   public Lock getLockClient() {
      return new LockClient("LockClient[" + clientName + "]", channel, (a, b) -> b);
   }

   @Override
   public Maintenance getMaintenanceClient() {
      return new MaintenanceClient("MaintenanceClient[" + clientName + "]", channel, (a, b) -> b);
   }

   @Override
   public Watch getWatchClient() {
      return new WatchClient("WatchClient[" + clientName + "]", channel, (a, b) -> b);
   }

   @Override
   public Cluster getClusterClient() {
      return new ClusterClient("ClusterClient[" + clientName + "]", channel, (a, b) -> b);
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

   @Override
   public String getClientName() {
      return clientName;
   }

}
