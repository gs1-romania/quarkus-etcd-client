package ro.gs1.quarkus.etcd;

import com.google.protobuf.ByteString;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.gs1.quarkus.etcd.api.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@QuarkusTest
@QuarkusTestResource(EtcdLifecycleManager.class)
public class TLSWithUserAuthTest {

   @EtcdClient("clientWithTlsUserAuth")
   EtcdClientChannel etcdClientWithTlsUserAuth;

   @Test
   public void testPutReadWithTlsUserAuth() {
      String parentKey = "/test/testPutReadWithTlsUserAuth/parent";
      String key = parentKey + "/key1";
      String value = "test_value1";
      UniAssertSubscriber<RangeResponse> tester = etcdClientWithTlsUserAuth.getKVClient()
         .put(PutRequest.newBuilder()
            .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
            .setValue(ByteString.copyFrom(value, StandardCharsets.UTF_8))
            .build())
         .chain(() -> etcdClientWithTlsUserAuth.getKVClient()
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

   @Test
   public void testRefreshToken() {
      String parentKey = "/test/testRefreshToken/parent";
      String key = parentKey + "/key1";
      String value = "test_value1";
      etcdClientWithTlsUserAuth.getKVClient()
         .put(PutRequest.newBuilder()
            .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
            .setValue(ByteString.copyFrom(value, StandardCharsets.UTF_8))
            .build())
         .await()
         .atMost(Duration.ofSeconds(5));
      RangeResponse rangeResponse = etcdClientWithTlsUserAuth.getKVClient()
         .range(RangeRequest.newBuilder()
            .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
            .build())
         .await()
         .atMost(Duration.ofSeconds(5));
      Assertions.assertEquals(value, rangeResponse.getKvsList()
         .get(0)
         .getValue()
         .toString(StandardCharsets.UTF_8));
      etcdClientWithTlsUserAuth.forceTokenRefresh();
      RangeResponse rangeResponseAfterRefresh = etcdClientWithTlsUserAuth.getKVClient()
         .range(RangeRequest.newBuilder()
            .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
            .build())
         .await()
         .atMost(Duration.ofSeconds(5));
      Assertions.assertEquals(value, rangeResponseAfterRefresh.getKvsList()
         .get(0)
         .getValue()
         .toString(StandardCharsets.UTF_8));
   }
}
