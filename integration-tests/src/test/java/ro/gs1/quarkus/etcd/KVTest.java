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
public class KVTest {

   @EtcdClient("clientNoTlsNoAuth")
   EtcdClientChannel etcdClientNoTlsNoAuth;

   @Test
   public void testMultiplePutRead() {
      String parentKey = "/test/testMultiplePutRead/parent";
      String key1 = parentKey + "/key1";
      String value1 = "test_value1";
      String key2 = parentKey + "/key2";
      String value2 = "test_value2";
      String key3 = parentKey + "/key3";
      String value3 = "test_value3";
      testMultiplePutReadHelper(etcdClientNoTlsNoAuth, key1, value1);
      testMultiplePutReadHelper(etcdClientNoTlsNoAuth, key2, value2);
      testMultiplePutReadHelper(etcdClientNoTlsNoAuth, key3, value3);
   }

   private void testMultiplePutReadHelper(EtcdClientChannel client, final String key, final String value) {
      UniAssertSubscriber<RangeResponse> tester = client.getKVClient()
         .put(PutRequest.newBuilder()
            .setKey(ByteString.copyFrom(key, StandardCharsets.UTF_8))
            .setValue(ByteString.copyFrom(value, StandardCharsets.UTF_8))
            .build())
         .chain(() -> client.getKVClient()
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
   public void testReadPrefix() {
      String parentKey = "/test/testReadPrefix/parent";
      String key1 = parentKey + "/key1";
      String value1 = "test_value1";
      String key2 = parentKey + "/key2";
      String value2 = "test_value2";
      String key3 = parentKey + "/key3";
      String value3 = "test_value3";
      UniAssertSubscriber<RangeResponse> tester = etcdClientNoTlsNoAuth.getKVClient()
         .put(PutRequest.newBuilder()
            .setKey(ByteString.copyFrom(key1, StandardCharsets.UTF_8))
            .setValue(ByteString.copyFrom(value1, StandardCharsets.UTF_8))
            .build())
         .chain(() -> etcdClientNoTlsNoAuth.getKVClient()
            .put(PutRequest.newBuilder()
               .setKey(ByteString.copyFrom(key2, StandardCharsets.UTF_8))
               .setValue(ByteString.copyFrom(value2, StandardCharsets.UTF_8))
               .build()))
         .chain(() -> etcdClientNoTlsNoAuth.getKVClient()
            .put(PutRequest.newBuilder()
               .setKey(ByteString.copyFrom(key3, StandardCharsets.UTF_8))
               .setValue(ByteString.copyFrom(value3, StandardCharsets.UTF_8))
               .build()))
         .chain(() -> etcdClientNoTlsNoAuth.getKVClient()
            .range(RangeRequest.newBuilder()
               .setKey(ByteString.copyFrom(parentKey, StandardCharsets.UTF_8))
               .setRangeEnd(ByteString.copyFrom("\0", StandardCharsets.UTF_8))
               .build()))
         .invoke(i -> Assertions.assertEquals(3, i.getKvsList()
            .size()))
         .subscribe()
         .withSubscriber(UniAssertSubscriber.create());
      tester.awaitItem();
      tester.assertCompleted();
   }

}
