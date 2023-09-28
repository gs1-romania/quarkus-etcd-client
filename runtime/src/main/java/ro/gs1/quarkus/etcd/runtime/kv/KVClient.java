package ro.comtec.etcd.quarkus.etcd.kv;

import io.grpc.Channel;
import io.smallrye.mutiny.Uni;

public class KVClient implements KV {

   private final Channel channel;

   public KVClient(Channel channel) {
      this.channel = channel;
   }

   @Override
   public Uni<PutResponse> put(String key, String value, PutRequestOptions options) {
      return null;
   }

   @Override
   public Uni<RangeResponse> range(String key, RangeRequestOptions options) {
      return null;
   }

   @Override
   public Uni<DeleteResponse> delete(String key, DeleteRequestOptions options) {
      return null;
   }
}
