package ro.gs1.quarkus.etcd.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import ro.gs1.quarkus.etcd.EtcdClient;

class EtcdProcessor {

   private static final String FEATURE = "etcd";

   @BuildStep
   FeatureBuildItem feature() {
      return new FeatureBuildItem(FEATURE);
   }

   @BuildStep
   AdditionalBeanBuildItem createContext() {
      return new AdditionalBeanBuildItem(EtcdClient.class);
   }
}
