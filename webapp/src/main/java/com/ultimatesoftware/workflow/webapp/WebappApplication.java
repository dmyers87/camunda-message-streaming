package com.ultimatesoftware.workflow.webapp;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.ultimatesoftware.workflow.messaging.consumer.TopicContainerManager;
import com.ultimatesoftware.workflow.messaging.kafka.CorrelatingMessageListener;
import com.ultimatesoftware.workflow.messaging.kafka.KafkaUtils;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.EnumSet;
import java.util.Set;

@SpringBootApplication
public class WebappApplication implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    MessageTypeMapper messageTypeMapper;

    @Autowired
    CorrelatingMessageListener listener;

    @Autowired
    TopicContainerManager topicContainerManager;

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
        topicContainerManager.createOrStartConsumers(
                KafkaUtils.getTopics(messageTypeMapper),
                listener);
    }

}
