package ro.gs1.quarkus.etcd.api;

import io.grpc.ManagedChannel;
import ro.gs1.quarkus.etcd.api.lock.Lock;

public interface EtcdClientChannel {

   /**
    * @return KV stub associated with this client.
    */
   KV getKVClient();

   /**
    * @return Lease stub associated with this client.
    */
   Lease getLeaseClient();

   /**
    * @return Lock stub associated with this client.
    */
   Lock getLockClient();

   /**
    * @return Maintenance stub associated with this client.
    */
   Maintenance getMaintenanceClient();

   /**
    * @return Watch stub associated with this client.
    */
   Watch getWatchClient();

   /**
    * @return Cluster stub associated with this client.
    */
   Cluster getClusterClient();

   /**
    * Force refresh this token.
    */
   void forceTokenRefresh();

   /**
    * @return The underlying gRPC managed channel of this client.
    */
   ManagedChannel getChannel();

   /**
    * @return The client name.
    */
   String getClientName();
}

