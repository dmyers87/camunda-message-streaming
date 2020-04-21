package com.ultimatesoftware.workflow.messaging;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;

public class MessageExtensionBpmnParseFactory implements org.camunda.bpm.engine.impl.cfg.BpmnParseFactory {
    private final MessageTypeMapper mapper;

    public MessageExtensionBpmnParseFactory(MessageTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
        return new MessageExtensionBpmnParse(bpmnParser, mapper);
    }
}
