package ro.gs1.quarkus.etcd;

import com.google.protobuf.ByteString;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.gs1.quarkus.etcd.api.EtcdClient;
import ro.gs1.quarkus.etcd.api.EtcdClientChannel;
import ro.gs1.quarkus.etcd.api.LeaseGrantRequest;
import ro.gs1.quarkus.etcd.api.TxnRequest;
import ro.gs1.quarkus.etcd.api.lock.LockRequest;
import ro.gs1.quarkus.etcd.api.lock.UnlockRequest;
import ro.gs1.quarkus.etcd.api.lock.UnlockResponse;

@QuarkusTest
@QuarkusTestResource(EtcdLifecycleManager.class)
public class LockTest {

   @EtcdClient("clientNoTlsNoAuth")
   EtcdClientChannel etcdClientChannel;

   @Test
   public void testLockAndUnlock() {
      UniAssertSubscriber<UnlockResponse> tester = etcdClientChannel.getLeaseClient()
         .leaseGrant(LeaseGrantRequest.newBuilder()
            .setTTL(200)
            .build())
         .invoke((leaseGrantResponse) -> Assertions.assertNotEquals(0, leaseGrantResponse.getID()))
         .chain((leaseGrantResponse) -> etcdClientChannel.getLockClient()
            .lock(LockRequest.newBuilder()
               .setLease(leaseGrantResponse.getID())
               .setName(ByteString.copyFromUtf8("my-lock"))
               .build()))
         .invoke((lockResponse) -> Assertions.assertTrue(lockResponse.getKey()
            .toStringUtf8()
            .startsWith("my-lock")))
         .chain((lockResponse) -> etcdClientChannel.getLockClient()
            .unlock(UnlockRequest.newBuilder()
               .setKey(lockResponse.getKey())
               .build()))
         .subscribe()
         .withSubscriber(UniAssertSubscriber.create());
      tester.awaitItem();
      tester.assertCompleted();
   }
}
