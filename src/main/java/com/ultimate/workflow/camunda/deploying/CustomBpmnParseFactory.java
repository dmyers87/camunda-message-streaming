package com.ultimate.workflow.camunda.deploying;

import com.ultimate.workflow.camunda.streaming.MessageTypeMapper;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;

public class CustomBpmnParseFactory implements org.camunda.bpm.engine.impl.cfg.BpmnParseFactory {
    private final MessageTypeMapper mapper;

    public CustomBpmnParseFactory(MessageTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
        return new CustomBpmnParse(bpmnParser, mapper);
    }
}
