package ro.gs1.quarkus.etcd.runtime;

import io.quarkus.runtime.annotations.Recorder;
import ro.gs1.quarkus.etcd.api.EtcdClientChannel;

import java.util.function.Supplier;

@Recorder
public class EtcdClientRecorder {

   public Supplier<EtcdClientChannel> createClientSupplier(String clientName) {
      return () -> EtcdClientFactory.createClient(clientName);
   }
}