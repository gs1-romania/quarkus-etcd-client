# quarkus-etcd-client - etcd Client for Quarkus

This project exposes the etcd gRPC API (KV, Lease, Lock, Maintenance, Watch, Cluster).

Inspired from: https://github.com/etcd-io/jetcd

## Download

### Maven

```xml
   <dependency>
      <groupId>ro.gs1</groupId>
      <artifactId>quarkus-etcd-client</artifactId>
      <version>${current.version}</version>
   </dependency>
```

## Usage

You can use @Etcd in your code to inject etcd stub:

```java
public void Foo {
    @Etcd
    KV kvClient;
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