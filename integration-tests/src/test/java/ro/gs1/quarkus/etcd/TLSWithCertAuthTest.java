package ro.gs1.quarkus.etcd;

import com.google.protobuf.ByteString;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.gs1.quarkus.etcd.api.*;

import java.nio.charset.StandardCharsets;

@QuarkusTest
@QuarkusTestResource(EtcdLifecycleManager.class)
public class TLSWithCertAuthTest {

   @EtcdClient("clientWithTlsCertAuth")
   EtcdClientChannel etcdClientWithTlsCertAuth;

   @Test
   public void testPutReadWithTlsCertAuth() {
      String parentKey = "/test/testPutReadWithTlsCertAuth/parent";
      String key = parentKey + "/key1";
      String value = "test_value1";
      UniAssertSubscriber<RangeResponse> tester = etcdClientWithTlsCertAuth.getKVClient()
         .put(PutRequest.newBuilder()
            .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
            .setValue(ByteString.copyFrom(value, StandardCharsets.UTF_8))
            .build())
         .chain(() -> etcdClientWithTlsCertAuth.getKVClient()
            .range(RangeRequest.newBuilder()
               .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
               .build()))
         .invoke(i -> Assertions.assertEquals(value, i.getKvsList()
            .get(0)
            .getValue()
            .toString(StandardCharsets.UTF_8)))
         .subscribe()
         .withSubscriber(UniAssertSubscriber.create());
      tester.awaitItem();
      tester.assertCompleted();
   }
}
