package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;

public class MessageExtensionBpmnParseFactory implements org.camunda.bpm.engine.impl.cfg.BpmnParseFactory {
    private final MessageTypeMapper mapper;
    private final MessagingProperties messagingProperties;
    private final MetadataValueEvaluator metadataValueEvaluator;

    public MessageExtensionBpmnParseFactory(MessageTypeMapper mapper,
                                            MessagingProperties messagingProperties,
                                            MetadataValueEvaluator metadataValueEvaluator) {
        this.mapper = mapper;
        this.messagingProperties = messagingProperties;
        this.metadataValueEvaluator = metadataValueEvaluator;
    }

    @Override
    public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
        return new MessageExtensionBpmnParse(bpmnParser,
                mapper,
                messagingProperties.getParser().getExtensionPrefix(),
                metadataValueEvaluator);
    }
}
