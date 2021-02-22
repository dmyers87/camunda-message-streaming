package com.ultimatesoftware.workflow.webapp;

import com.ultimatesoftware.workflow.messaging.kafka.KafkaTopicContainerManager;
import com.ultimatesoftware.workflow.messaging.kafka.TopicContainerManager;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(KafkaBinderConfiguration.class)
public class KafkaConsumerFactoryConfiguration {

  @Bean
  public TopicContainerManager topicContainerManager(ConsumerFactory<String, String> consumerFactory) {
    return new KafkaTopicContainerManager(consumerFactory);
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory(KafkaBinderConfigurationProperties binderConfigurationProperties) {
    return new DefaultKafkaConsumerFactory<>(
            consumerConfig(binderConfigurationProperties)
    );
  }

  public Map<String, Object> consumerConfig(KafkaBinderConfigurationProperties binderConfigurationProperties) {
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
}
