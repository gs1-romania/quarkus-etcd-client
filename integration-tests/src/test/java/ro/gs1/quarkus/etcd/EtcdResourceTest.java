package ro.gs1.quarkus.etcd;

import java.util.Map;
import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import ro.gs1.quarkus.etcd.runtime.EtcdClient;

@QuarkusTest
@QuarkusTestResource(EtcdResourceLifecycleManager.class)
public class EtcdResourceTest {

   @Inject
   EtcdClient etcdClient;

   @Test
   public void testMultiplePutRead() {
      String parentKey = "/test/testMultiplePutRead/parent";
      String key1 = parentKey + "/key1";
      String value1 = "test_value1";
      String key2 = parentKey + "/key2";
      String value2 = "test_value2";
      String key3 = parentKey + "/key3";
      String value3 = "test_value3";
      etcdClient.put(key1, value1);
      etcdClient.put(key2, value2);
      etcdClient.put(key3, value3);
      String read1 = etcdClient.read(key1);
      String read2 = etcdClient.read(key2);
      String read3 = etcdClient.read(key3);
      Assertions.assertEquals(value1, read1);
      Assertions.assertEquals(value2, read2);
      Assertions.assertEquals(value3, read3);
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
      etcdClient.put(key1, value1);
      etcdClient.put(key2, value2);
      etcdClient.put(key3, value3);
      Map<String, String> read = etcdClient.readPrefix(parentKey);
      Assertions.assertEquals(3, read.size());
      Assertions.assertEquals(value1, read.get(key1));
      Assertions.assertEquals(value2, read.get(key2));
      Assertions.assertEquals(value3, read.get(key3));
   }
}
