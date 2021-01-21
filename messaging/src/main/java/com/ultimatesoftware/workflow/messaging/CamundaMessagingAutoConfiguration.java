package com.ultimatesoftware.workflow.messaging;

import com.ultimatesoftware.workflow.messaging.kafka.KafkaTopicContainerManager;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@EnableConfigurationProperties({MessagingProperties.class})
@AutoConfigureAfter({CamundaBpmAutoConfiguration.class})
public class CamundaMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({MessageTypeMapper.class})
    public MessageTypeMapper messageTypeMapper() {
        return new MemoryMessageTypeMapper();
    }

    @Bean
    public GenericMessageCorrelator genericMessageCorrelator(RuntimeService runtimeService) {
        return new GenericMessageCorrelator(runtimeService);
    }

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

    @Bean
    @ConditionalOnMissingBean({BpmnParseFactory.class})
    public BpmnParseFactory beanParseFactory(MessageTypeMapper mapper, MessagingProperties messagingProperties) {
        return new MessageExtensionBpmnParseFactory(mapper, messagingProperties);
    }

}
