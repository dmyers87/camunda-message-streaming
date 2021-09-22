package com.ultimatesoftware.workflow.messaging;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.DefaultMetadataValueEvaluator;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageExtensionBpmnParseFactory;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessagingProperties;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MetadataValueEvaluator;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.PostBpmnDeployExtensionDataInitializerPlugin;
import com.ultimatesoftware.workflow.messaging.correlation.GenericMessageCorrelator;
import com.ultimatesoftware.workflow.messaging.topicmapping.MemoryMessageTypeMapper;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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
    @ConditionalOnMissingBean({GenericMessageCorrelator.class})
    public GenericMessageCorrelator genericMessageCorrelator(RuntimeService runtimeService) {
        return new GenericMessageCorrelator(runtimeService);
    }

    @Bean
    @ConditionalOnMissingBean({BpmnParseFactory.class})
    public BpmnParseFactory bpmnParseFactory(MessageTypeMapper mapper,
                                             MessagingProperties messagingProperties,
                                             MetadataValueEvaluator metadataValueEvaluator) {
        return new MessageExtensionBpmnParseFactory(mapper, messagingProperties, metadataValueEvaluator);
    }

    @Bean
    @ConditionalOnMissingBean({MetadataValueEvaluator.class})
    public MetadataValueEvaluator topicValueEvaluator(Environment environment) {
        return new DefaultMetadataValueEvaluator(environment);
    }

    @Bean
    public PostBpmnDeployExtensionDataInitializerPlugin extensionDataInitializerPlugin(MessageTypeMapper messageTypeMapper) {
        return new PostBpmnDeployExtensionDataInitializerPlugin(messageTypeMapper);

    }
}
