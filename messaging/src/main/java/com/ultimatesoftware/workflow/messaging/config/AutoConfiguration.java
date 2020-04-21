package com.ultimatesoftware.workflow.messaging.config;

import com.ultimatesoftware.workflow.messaging.*;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({MessageTypeMapper.class})
    public MessageTypeMapper messageTypeMapper() {
        return new MemoryMessageTypeMapper();
    }

    @Bean
    public GenericMessageCorrelator genericMessageCorrelator(ProcessEngine processEngine, MessageTypeMapper messageTypeMapper) {
        return new GenericMessageCorrelator(processEngine, messageTypeMapper);
    }

    @Bean
    @ConditionalOnMissingBean({CorrelatingMessageListener.class})
    public CorrelatingMessageListener correlatingMessageListener(GenericMessageCorrelator messageCorrelator) {
        return new CorrelatingMessageListener(messageCorrelator);
    }

    @Bean
    public TopicContainerManager topicContainerManager() {
        return new KafkaTopicContainerManager();
    }
}
