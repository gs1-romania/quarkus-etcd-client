package ro.comtec.etcd.quarkus.etcd.kv;

import io.smallrye.mutiny.Uni;

public interface KV {

   Uni<PutResponse> put(String key, String value, PutRequestOptions options);

   Uni<RangeResponse> range(String key, RangeRequestOptions options);

   Uni<DeleteResponse> delete(String key, DeleteRequestOptions options);

   // txn

   // compact
}
