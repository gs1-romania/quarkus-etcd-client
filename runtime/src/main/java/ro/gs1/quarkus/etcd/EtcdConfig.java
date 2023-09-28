package ro.gs1.quarkus.etcd;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.DurationConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

/**
 * ETCD Configuration.
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.etcd")
public interface EtcdConfig {
   // @formatter:off
   /**
    * ETCD host to connect to.
    *
    * @asciidoclet
    */
   // @formatter:on
   @WithDefault("localhost")
   String host();
   // @formatter:off
   /**
    * ETCD port to connect to.
    *
    * @asciidoclet
    */
   // @formatter:on
   @WithDefault("2379")
   Integer port();
   // @formatter:off
   /**
    * Authentication name.
    *
    * @asciidoclet
    */
   // @formatter:on
   Optional<String> name();
   // @formatter:off
   /**
    * Authentication password.
    *
    * @asciidoclet
    */
   // @formatter:on
   Optional<String> password();
   // @formatter:off
   /**
    * Authentication timeout.
    *
    * @asciidoclet
    */
   // @formatter:on
   @WithConverter(DurationConverter.class) Optional<Duration> authenticationTimeout();
   // @formatter:off
   /**
    * Sets the time without read activity before sending a keepalive ping.
    *
    * An unreasonably small value might be increased, and {@code Long.MAX_VALUE} nanoseconds or an unreasonably large
    * value will disable keepalive. Defaults to infinite.
    * @asciidoclet
    */
   // @formatter:on
   @WithConverter(DurationConverter.class) Optional<Duration> keepAliveTime();
   // @formatter:off
   /**
    * Sets the time waiting for read activity after sending a keepalive ping.
    *
    * If the time expires without any read activity on the connection, the connection is considered dead.
    * An unreasonably small value might be increased. Defaults to 20 seconds.
    *
    * This value should be at least multiple times the RTT to allow for lost packets.
    *
    * @asciidoclet
    */
   // @formatter:on
   @WithConverter(DurationConverter.class) Optional<Duration> keepAliveTimeout();
   // @formatter:off
   /**
    * Sets whether keepalive will be performed when there are no outstanding RPC on a connection.
    * Defaults to {@code false}.
    *
    * @asciidoclet
    */
   // @formatter:on
   Optional<Boolean> keepAliveWithoutCalls();
   // @formatter:off
   /**
    * Sets the maximum message size allowed to be received on the channel.
    *
    * If not called, defaults to 4 MiB. The default provides protection to clients who haven't considered the
    * possibility of receiving large messages while trying to be large enough to not be hit in normal usage.
    * @asciidoclet
    */
   // @formatter:on
   Optional<Integer> maxInboundMessageSize();
   //formatter:off
   // @formatter:off
   /**
    * Overrides the authority used with TLS and HTTP virtual hosting.
    *
    * It does not change what host is * actually connected to. Is commonly in the form host:port.
    *
    * @asciidoclet
    */
   // @formatter:on
   Optional<String> authority();
   //formatter:off
   // @formatter:off
   /**
    * Sets the default load-balancing policy that will be used if the service config doesn't specify
    * one.
    * If not set, the default will be the "pick_first" policy.
    * @asciidoclet
    */
   // @formatter:on
   Optional<String> defaultLoadBalancingPolicy();
}
