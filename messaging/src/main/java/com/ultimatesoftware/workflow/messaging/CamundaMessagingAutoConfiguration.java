package com.ultimatesoftware.workflow.messaging;

import com.ultimatesoftware.workflow.messaging.*;
import com.ultimatesoftware.workflow.messaging.KafkaTopicContainerManager;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MessagingProperties.class})
@AutoConfigureAfter({CamundaBpmAutoConfiguration.class})
public class CamundaMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({MessageTypeMapper.class})
    public MessageTypeMapper getMessageTypeMapper() {
        return new MemoryMessageTypeMapper();
    }

    @Bean
    public GenericMessageCorrelator getGenericMessageCorrelator(RuntimeService runtimeService, MessageTypeMapper messageTypeMapper) {
        return new GenericMessageCorrelator(runtimeService, messageTypeMapper);
    }

    @Bean
    @ConditionalOnMissingBean({CorrelatingMessageListener.class})
    public CorrelatingMessageListener getCorrelatingMessageListener(GenericMessageCorrelator messageCorrelator) {
        return new CorrelatingMessageListener(messageCorrelator);
    }

    @Bean
    @ConditionalOnMissingBean({TopicContainerManager.class})
    public TopicContainerManager getTopicContainerManager() {
        return new KafkaTopicContainerManager();
    }

    @Bean
    @ConditionalOnMissingBean({BpmnParseFactory.class})
    public BpmnParseFactory getBeanParseFactory(MessageTypeMapper mapper, MessagingProperties messagingProperties) {
        return new MessageExtensionBpmnParseFactory(mapper, messagingProperties);
    }
}
