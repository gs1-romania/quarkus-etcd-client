# quarkus-etcd-client - etcd Client for Quarkus

This project exposes the etcd gRPC API (KV, Lease, Lock, Maintenance, Watch, Cluster).

Inspired from: https://github.com/etcd-io/jetcd

This project is experimental, use at your own risk.
There is no support for TLS.

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

# Defaults to 'localhost'
quarkus.etcd.host=localhost

# Defaults to '2379'
quarkus.etcd.port=2379

# No default
quarkus.etcd.name=my_username

# No default
quarkus.etcd.password=my_password

# Defaults to 5s default
quarkus.etcd.authenticationTimeout=5s

# Vert.x Channel default '9223372036854775807s'
quarkus.etcd.keepAliveTime=5s

# Vert.x Channel default '20s'
quarkus.etcd.keepAliveTimeout=20s

# Vert.x Channel default 'false'
quarkus.etcd.keepAliveWithoutCalls=false

# Vert.x Channel default '4194304' (4MiB)
quarkus.etcd.maxInboundMessageSize=4194304

# Vert.x Channel default ''
quarkus.etcd.authority=

# Vert.x Channel default 'pick_first'
quarkus.etcd.defaultLoadBalancingPolicy=pick_first
```

## Usage

You can use @Etcd in your code to inject etcd stub:

```java
@Etcd
Lock lock;

@Etcd
Lease lease;

@Etcd
Maintenance maintenance;

@Etcd
Watch watch;

@Etcd
Cluster cluster;
```


```java
public class Foo {
    @Etcd
    KV kvClient;
    
    public Uni<PutResponse> bar() {
       return kvClient.put(PutRequest.newBuilder()
          .setKey(ByteString.copyFrom("key", StandardCharsets.UTF_8))
          .setValue(ByteString.copyFrom("value", StandardCharsets.UTF_8))
          .build());
    }
}
```
All the above are Mutiny backed stubs and are using a single gRPC channel application-wise.

You can also inject the stubs with @GrpcClient. 
If choose this way you need to use the io.quarkus.grpc configuration ([Quarkus Documentation](https://quarkus.io/guides/grpc-getting-started)):

```java
@GrpcClient("etcd")
KV kvClient;
```

## License
quarkus-etcd-client is under the Apache 2.0 license. See the [LICENSE](https://github.com/gs1-romania/quarkus-etcd-client/blob/master/LICENSE) file for details.