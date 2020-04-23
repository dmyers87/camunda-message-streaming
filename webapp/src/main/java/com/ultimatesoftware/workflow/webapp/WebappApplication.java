package com.ultimatesoftware.workflow.webapp;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.ultimatesoftware.workflow.messaging.CorrelatingMessageListener;
import com.ultimatesoftware.workflow.messaging.KafkaUtils;
import com.ultimatesoftware.workflow.messaging.MessageTypeMapper;
import com.ultimatesoftware.workflow.messaging.TopicContainerManager;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Import(KafkaBinderConfiguration.class)
@SpringBootApplication
public class WebappApplication implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    MessageTypeMapper messageTypeMapper;

    @Autowired
    CorrelatingMessageListener listener;

    @Autowired
    TopicContainerManager topicContainerManager;

    @Autowired
    KafkaBinderConfigurationProperties binderConfigurationProperties;

    /**
     * Goals:
     * read extension data from the BMPN in order to creating mappings between Kafka and BMPN message objects
     *
     * @param args
     */
    public static void main(String[] args) {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });

        SpringApplication.run(WebappApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        for (String topic : KafkaUtils.getTopics(messageTypeMapper)) {
            topicContainerManager.createOrStartConsumer(topic, listener, consumerConfig());
        }
    }

    private Map<String, Object> consumerConfig() {
        Map<String, Object> properties = new HashMap<>();
        //properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, binderConfigurationProperties.getKafkaProperties().getBootstrapServers());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }
}
