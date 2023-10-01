package ro.gs1.quarkus.etcd;

import com.google.protobuf.ByteString;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.gs1.quarkus.etcd.api.*;
import ro.gs1.quarkus.etcd.api.lock.LockRequest;

import java.time.Duration;

@QuarkusTest
@QuarkusTestResource(EtcdLifecycleManager.class)
public class LeaseTest {

   @EtcdClient("clientNoTlsNoAuth")
   EtcdClientChannel etcdClientChannel;

   @Test
   public void testLease() {
      String parentKey = "/test/testLease/parent";
      String key = parentKey + "/key1";
      String value = "test_value1";
      LeaseGrantResponse leaseGrantResponse = etcdClientChannel.getLeaseClient()
         .leaseGrant(LeaseGrantRequest.newBuilder()
            .setTTL(200)
            .build())
         .await()
         .atMost(Duration.ofSeconds(5));
      Assertions.assertNotEquals(0, leaseGrantResponse.getID());
      etcdClientChannel.getKVClient()
         .put(PutRequest.newBuilder()
            .setKey(ByteString.copyFromUtf8(key))
            .setValue(ByteString.copyFromUtf8(value))
            .setLease(leaseGrantResponse.getID())
            .build())
         .await()
         .atMost(Duration.ofSeconds(5));
      LeaseTimeToLiveResponse leaseTimeToLiveResponse = etcdClientChannel.getLeaseClient()
         .leaseTimeToLive(LeaseTimeToLiveRequest.newBuilder()
            .setID(leaseGrantResponse.getID())
            .setKeys(true)
            .build())
         .await()
         .atMost(Duration.ofSeconds(5));
      Assertions.assertEquals(1, leaseTimeToLiveResponse.getKeysCount());
   }
}
