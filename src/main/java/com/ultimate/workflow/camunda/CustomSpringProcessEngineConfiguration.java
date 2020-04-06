package com.ultimate.workflow.camunda;

import com.ultimate.workflow.camunda.deploying.CustomBpmnParseFactory;
import com.ultimate.workflow.camunda.streaming.MessageTypeMapper;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

public class CustomSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    private MessageTypeMapper mapper;

    public CustomSpringProcessEngineConfiguration(MessageTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BpmnDeployer getBpmnDeployer() {
        // Copied from org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
        BpmnDeployer bpmnDeployer = new BpmnDeployer();
        bpmnDeployer.setExpressionManager(expressionManager);
        bpmnDeployer.setIdGenerator(idGenerator);

        if (bpmnParseFactory == null) {
            // Replace parse factory with custom parse factory
            bpmnParseFactory = new CustomBpmnParseFactory(mapper);
        }

        BpmnParser bpmnParser = new BpmnParser(expressionManager, bpmnParseFactory);

        if (preParseListeners != null) {
            bpmnParser.getParseListeners().addAll(preParseListeners);
        }
        bpmnParser.getParseListeners().addAll(getDefaultBPMNParseListeners());
        if (postParseListeners != null) {
            bpmnParser.getParseListeners().addAll(postParseListeners);
        }

        bpmnDeployer.setBpmnParser(bpmnParser);

        return bpmnDeployer;
    }
}
