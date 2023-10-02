# quarkus-etcd-client - etcd Client for Quarkus
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/gs1-romania/quarkus-etcd-client/.github%2Fworkflows%2Fbuild.yml)
![GitHub](https://img.shields.io/github/license/gs1-romania/quarkus-etcd-client)
![Maven Central](https://img.shields.io/maven-central/v/ro.gs1/quarkus-etcd-client)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/m/gs1-romania/quarkus-etcd-client)
![Experimental Badge](https://img.shields.io/badge/experimental-red)

This project exposes the etcd gRPC API (KV, Lease, Lock, Maintenance, Watch, Cluster).

Inspired from: https://github.com/etcd-io/jetcd

This project is experimental, use at your own risk.

For Quarkus version 3.4.1+

## Download

### Maven

```xml
<dependency>
  <groupId>ro.gs1</groupId>
  <artifactId>quarkus-etcd-client</artifactId>
  <version>${current.version}</version>
</dependency>
```
## Configuration

```properties
# --- Configuration per client, replace "client-name" (quotes included) ---

# etcd server host.
# Defaults to 'localhost'
quarkus.etcd."client-name".host=localhost

# etcd server port.
# Defaults to '2379'.
quarkus.etcd."client-name".port=2379

# Client username for authentication with server.
# No default.
quarkus.etcd."client-name".name=my_username

# Client password for authentication with server.
# No default.
quarkus.etcd."client-name".password=my_password

# Timeout for authentication with the server. 
# Defaults to 5s default.
quarkus.etcd."client-name".authentication-timeout=5s

# Vert.x Channel default '9223372036854775807s'.
quarkus.etcd."client-name".keep-alive-time=5s

# Vert.x Channel default '20s'.
quarkus.etcd."client-name".keep-alive-timeout=20s

# Vert.x Channel default 'false'.
quarkus.etcd."client-name".keep-alive-without-calls=false

# Vert.x Channel default '4194304' (4MiB).
quarkus.etcd."client-name".max-inbound-message-size=4194304

# Vert.x Channel default ''.
quarkus.etcd."client-name".authority=

# Vert.x Channel default 'pick_first'.
quarkus.etcd."client-name".default-load-balancing-policy=pick_first
```

```properties
# --- Certificate authentication configuration per client name, optional ---

# Path to the JKS file, classpath or file.
# No default.
quarkus.etcd."client-name".ssl-config.key-store.path=

# Password of the JKS.
# No default.
quarkus.etcd."client-name".ssl-config.key-store.password=

# If there are multiple aliases in the JKS, choose one.
# No default.
quarkus.etcd."client-name".ssl-config.key-store.alias=

# Password of the alias.
# No default.
quarkus.etcd."client-name".ssl-config.key-store.alias-password=

# --- SSL/TLS configuration per client name, optional ---

# Path to the JKS file, classpath or file.
# No default.
quarkus.etcd."client-name".ssl-config.trust-store.path=

# Password of the JKS.
# No default.
quarkus.etcd."client-name".ssl-config.trust-store.password=
```

## Usage

You can use @EtcdClient in your code to inject etcd stub:

```java
public class Foo {

   @EtcdClient("clientName")
   EtcdClientChannel client;
}
```


```java
public class Foo {
   @EtcdClient("clientName")
   EtcdClientChannel client;
    
    public Uni<PutResponse> bar() {
       return client.getKVClient().put(PutRequest.newBuilder()
          .setKey(ByteString.copyFrom("key", StandardCharsets.UTF_8))
          .setValue(ByteString.copyFrom("value", StandardCharsets.UTF_8))
          .build());
    }
}
```
All the above are Mutiny backed stubs and are using a single gRPC channel per client.

Authentication is done by token with username and password or by certificate.
Also, TLS/SLL is supported.

You can also inject the stubs with @GrpcClient. 
If choose this way you need to use the io.quarkus.grpc configuration ([Quarkus Documentation](https://quarkus.io/guides/grpc-getting-started)):

```java
public class Foo {

   @GrpcClient("etcd")
   KV kvClient;
}
```

## License
quarkus-etcd-client is under the Apache 2.0 license. See the [LICENSE](https://github.com/gs1-romania/quarkus-etcd-client/blob/master/LICENSE) file for details.