package com.ultimatesoftware.workflow.webapp;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaExtendedBindingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@Import(KafkaBinderConfiguration.class)
public class KafkaConsumerFactoryConfiguration {

  @Autowired
  public KafkaBinderConfigurationProperties binderConfigurationProperties;

  @Autowired
  public KafkaExtendedBindingProperties bindingProperties;

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfig());
  }

  public Map<String, Object> consumerConfig() {
    Map<String, Object> propsMap = new HashMap<>();
    propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, binderConfigurationProperties.getKafkaProperties().getBootstrapServers());
    propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
    propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
    propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return propsMap;
  }

//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//        KafkaConsumerProperties consumerProperties = bindingProperties.getExtendedConsumerProperties(Sink.INPUT);
//        ExtendedConsumerProperties<KafkaConsumerProperties> extendedConsumerProperties =
//                new ExtendedConsumerProperties<>(consumerProperties);
//        Map<String, Object> configs = convertConsumerConfig(extendedConsumerProperties);
//        return new DefaultKafkaConsumerFactory<>(configs);
//    }

//    public Map<String, Object> convertConsumerConfig(ExtendedConsumerProperties<KafkaConsumerProperties> consumerProperties) {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
//        //props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, anonymous ? "latest" : "earliest");
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProperties.getExtension());
//
//        Map<String, Object> mergedConfig = binderConfigurationProperties.mergedConsumerConfiguration();
//        if (!ObjectUtils.isEmpty(mergedConfig)) {
//            props.putAll(mergedConfig);
//        }
//        if (ObjectUtils.isEmpty(props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG))) {
//            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, binderConfigurationProperties.getKafkaConnectionString());
//        }
//        if (!ObjectUtils.isEmpty(consumerProperties.getExtension().getConfiguration())) {
//            props.putAll(consumerProperties.getExtension().getConfiguration());
//        }
//        if (!ObjectUtils.isEmpty(consumerProperties.getExtension().getStartOffset())) {
//            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProperties.getExtension().getStartOffset().name());
//        }
//
//        return props;
//    }
}
