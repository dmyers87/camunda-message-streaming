package com.ultimatesoftware.workflow.webapp;

import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

public class CustomSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    public CustomSpringProcessEngineConfiguration(BpmnParseFactory bpmnParseFactory) {
        this.bpmnParseFactory = bpmnParseFactory;
    }

}
