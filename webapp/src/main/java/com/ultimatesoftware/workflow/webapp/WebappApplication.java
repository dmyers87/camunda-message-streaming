package com.ultimatesoftware.workflow.webapp;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.ultimatesoftware.workflow.messaging.CorrelatingMessageListener;
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
@SpringBootApplication(scanBasePackages = { "com.ultimatesoftware.workflow"})
public class WebappApplication implements ApplicationListener<ApplicationReadyEvent> {

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
     * using the messaging specification, queue a message into Kafka
     * read that message from topic in Camunda and change the state of a BPMN
     *
     * TODO:
     *   [X] Correlation of messages based on message type
     *   [X] Correlation of messages based on business key (defined as expression)
     *   [X] Multi-Tenant BPMNs (https://docs.camunda.org/manual/7.5/user-guide/process-engine/multi-tenancy/)
     *   [X] Parsing of extension data
     *   [X] Allow correlation on match variables (defined as expression)
     *   [X] Allow correlation to set BP variables (defined as expression)
     *   [X] Support both 3 and 4 part for business-process-key
     *   [X] Create topic subscription at runtime
     *   [ ] Acquire consumer configuration from binding configuration object (where possible)
     *   [ ] Multiple topics per BPMN
     *   [ ] Multiple message types per topic
     *   [ ] Kakfa cluster (aggregate or local) per Message Type
     *   [ ] Versioning of the message types
     *   [ ] Redeployment of BPMN and updating mappings
     *   [ ] Refactor all strings into constants
     *   [ ] Ensure BP matching includes cluster + topic + tenant + message_type + message_version
     *
     * BUGS:
     *   [X] Extensions of type "match-var" should not be allowed on start events
     *   [X] Messaging mapping can cause additional correlations if not specific enough
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
        topicContainerManager.createOrStartConsumer("poc1", listener, consumerConfig());
        topicContainerManager.createOrStartConsumer("poc2", listener, consumerConfig());
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
