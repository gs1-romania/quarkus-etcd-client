// @formatter:off

package ro.gs1.quarkus.etcd.runtime;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import com.google.protobuf.ByteString;

import io.quarkus.grpc.GrpcClient;
import ro.gs1.quarkus.etcd.api.*;
import ro.gs1.quarkus.etcd.api.kv.KeyValue;

@ApplicationScoped
public class EtcdClient {

   private static final Logger logger = Logger.getLogger(EtcdClient.class);

   @GrpcClient("etcd")
   KVGrpc.KVBlockingStub kvBlockingStub;

   public void put(String key, String value) {
      PutRequest putRequest = PutRequest.newBuilder().setKey(ByteString.copyFrom(key.getBytes(Charset.defaultCharset()))).setValue(ByteString.copyFrom(value.getBytes(Charset.defaultCharset()))).build();
      PutResponse putResponse = kvBlockingStub.put(putRequest);
      logger.infov("Put key: {0} with value: {1}", putResponse.getPrevKv().getKey().toString(Charset.defaultCharset()), putResponse.getPrevKv().getValue().toString(Charset.defaultCharset()));
   }

   public Map<String, String> readPrefix(String key) {
      RangeRequest rangeRequest = RangeRequest.newBuilder().setKey(ByteString.copyFrom(key.getBytes())).setRangeEnd(ByteString.copyFrom("\0".getBytes(Charset.defaultCharset()))).build();
      RangeResponse rangeResponse = kvBlockingStub.range(rangeRequest);
      logger.infov("For key: {0} we have {1} of records", key, rangeResponse.getCount());
      Map<String, String> map = new HashMap<>();
      for (KeyValue keyValue : rangeResponse.getKvsList()) {
         logger.infov("Range key: {0} with value: {1}", keyValue.getKey().toString(Charset.defaultCharset()), keyValue.getValue().toString(Charset.defaultCharset()));
         map.put(keyValue.getKey().toString(Charset.defaultCharset()), keyValue.getValue().toString(Charset.defaultCharset()));
      }
      return map;
   }

   public String read(String key) {
      RangeRequest rangeRequest = RangeRequest.newBuilder().setKey(ByteString.copyFrom(key.getBytes())).build();
      RangeResponse rangeResponse = kvBlockingStub.range(rangeRequest);
      logger.infov("For key: {0} we have {1} of records", key, rangeResponse.getCount());
      for (KeyValue keyValue : rangeResponse.getKvsList()) {
         logger.infov("Range key: {0} with value: {1}", keyValue.getKey().toString(Charset.defaultCharset()), keyValue.getValue().toString(Charset.defaultCharset()));
         return keyValue.getValue().toString(Charset.defaultCharset());
      }
      return null;
   }

}
