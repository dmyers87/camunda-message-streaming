package com.ultimatesoftware.workflow.messaging.kafka;

import com.ultimatesoftware.workflow.messaging.CamundaMessagingAutoConfiguration;
import com.ultimatesoftware.workflow.messaging.correlation.GenericMessageCorrelator;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@AutoConfigureAfter({CamundaMessagingAutoConfiguration.class})
public class CamundaMessagingKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({CorrelatingMessageListener.class})
    public CorrelatingMessageListener correlatingMessageListener(GenericMessageCorrelator messageCorrelator, MessageTypeMapper messageTypeMapper) {
        return new CorrelatingMessageListener(messageCorrelator, messageTypeMapper);
    }

    @Bean
    @ConditionalOnMissingBean({TopicContainerManager.class})
    public TopicContainerManager topicContainerManager(ConsumerFactory<String, String> consumerFactory) {
        return new KafkaTopicContainerManager(consumerFactory);
    }

}
