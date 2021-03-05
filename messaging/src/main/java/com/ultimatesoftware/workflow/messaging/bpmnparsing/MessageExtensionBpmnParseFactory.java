package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;

public class MessageExtensionBpmnParseFactory implements org.camunda.bpm.engine.impl.cfg.BpmnParseFactory {
    private final MessageTypeMapper mapper;
    private final MessagingProperties messagingProperties;
    private final TopicValueEvaluator topicValueEvaluator;

    public MessageExtensionBpmnParseFactory(MessageTypeMapper mapper,
                                            MessagingProperties messagingProperties,
                                            TopicValueEvaluator topicValueEvaluator) {
        this.mapper = mapper;
        this.messagingProperties = messagingProperties;
        this.topicValueEvaluator = topicValueEvaluator;
    }

    @Override
    public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
        return new MessageExtensionBpmnParse(bpmnParser,
                mapper,
                messagingProperties.getParser().getExtensionPrefix(),
                topicValueEvaluator);
    }
}
