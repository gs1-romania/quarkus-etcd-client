package ro.gs1.quarkus.etcd.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.InjectionPointInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import ro.gs1.quarkus.etcd.api.EtcdClient;
import ro.gs1.quarkus.etcd.api.EtcdClientChannel;
import ro.gs1.quarkus.etcd.runtime.EtcdClientDestroyer;
import ro.gs1.quarkus.etcd.runtime.EtcdClientRecorder;
import ro.gs1.quarkus.etcd.runtime.config.EtcdConfigProvider;

import java.util.HashSet;
import java.util.Set;

class EtcdClientProcessor {

   private static final String FEATURE = "etcd-client";

   @BuildStep
   void createContext(BuildProducer<AdditionalBeanBuildItem> beans) {
      beans.produce(new AdditionalBeanBuildItem(EtcdClient.class));
      beans.produce(AdditionalBeanBuildItem.builder()
         .setUnremovable()
         .addBeanClasses(EtcdConfigProvider.class)
         .build());
   }

   @BuildStep
   @Record(ExecutionTime.RUNTIME_INIT)
   void generateEtcdClientProducers(BeanDiscoveryFinishedBuildItem beanDiscovery,
      BuildProducer<SyntheticBeanBuildItem> syntheticBeans, BuildProducer<FeatureBuildItem> features,
      EtcdClientRecorder recorder) {
      Set<String> clients = new HashSet<>();
      for (InjectionPointInfo injectionPoint : beanDiscovery.getInjectionPoints()) {
         AnnotationInstance clientAnnotation = injectionPoint.getRequiredQualifier(
            DotName.createSimple(EtcdClient.class.getName()));
         if (clientAnnotation == null) {
            continue;
         }
         AnnotationValue clientNameValue = clientAnnotation.value();
         if (clientNameValue == null) {
            throw new DeploymentException("@EtcdClient client name cannot be null.");
         }
         String clientName = clientNameValue.asString();
         clients.add(clientName);
      }
      if (!clients.isEmpty()) {
         for (String client : clients) {
            SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem.configure(
                  EtcdClientChannel.class)
               .addQualifier()
               .annotation(DotName.createSimple(EtcdClient.class.getName()))
               .addValue("value", client)
               .done()
               .scope(ApplicationScoped.class)
               .unremovable()
               .forceApplicationClass()
               .setRuntimeInit()
               .supplier(recorder.createClientSupplier(client))
               .destroyer(EtcdClientDestroyer.class);
            syntheticBeans.produce(configurator.done());
         }
         features.produce(new FeatureBuildItem(FEATURE));
      }
   }
}