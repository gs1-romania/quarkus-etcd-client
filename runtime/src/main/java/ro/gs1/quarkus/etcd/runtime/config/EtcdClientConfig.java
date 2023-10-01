//@formatter:off
package ro.gs1.quarkus.etcd.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.configuration.DurationConverter;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

@ConfigGroup
public interface EtcdClientConfig {

   /**
    * ETCD host to connect to.
    */
   @WithDefault("localhost")
   String host();

   /**
    * ETCD port to connect to.
    */
   @WithDefault("2379")
   Integer port();

   /**
    * SSL/TLS configuration for this client.
    */
   EtcdSslConfig sslConfig();

   /**
    * Authentication name.
    */
   Optional<String> name();

   /**
    * Authentication password.
    */
   Optional<String> password();

   /**
    * Authentication timeout.
    */
   @WithConverter(DurationConverter.class)
   @WithDefault("5s")
   Optional<Duration> authenticationTimeout();

   /**
    * Sets the time without read activity before sending a keepalive ping.
    *
    * An unreasonably small value might be increased, and {@code Long.MAX_VALUE} nanoseconds or an unreasonably large
    * value will disable keepalive. Defaults to infinite.
    */
   @WithConverter(DurationConverter.class) Optional<Duration> keepAliveTime();

   /**
    * Sets the time waiting for read activity after sending a keepalive ping.
    * If the time expires without any read activity on the connection, the connection is considered dead. An
    * unreasonably small value might be increased. Defaults to 20 seconds.
    * This value should be at least multiple times the RTT to allow for lost packets.
    */
   @WithConverter(DurationConverter.class) Optional<Duration> keepAliveTimeout();

   /**
    * Sets whether keepalive will be performed when there are no outstanding RPC on a connection. Defaults to
    * {@code false}.
    */
   Optional<Boolean> keepAliveWithoutCalls();

   /**
    * Sets the maximum message size allowed to be received on the channel.
    * If not called, defaults to 4 MiB. The default provides protection to clients who haven't considered the
    * possibility of receiving large messages while trying to be large enough to not be hit in normal usage.
    */
   Optional<Integer> maxInboundMessageSize();

   /**
    * Overrides the authority used with TLS and HTTP virtual hosting.
    * It does not change what host is actually connected to. Is commonly in the form host:port.
    */
   Optional<String> authority();

   /**
    * Sets the default load-balancing policy that will be used if the service config doesn't specify one.
    * If not set, the default will be the "pick_first" policy.
    */
   Optional<String> defaultLoadBalancingPolicy();
}
