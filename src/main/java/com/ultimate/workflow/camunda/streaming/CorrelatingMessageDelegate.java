package com.ultimate.workflow.camunda.streaming;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CorrelatingMessageDelegate implements JavaDelegate {

    @Autowired
    private CorrelatingMessageListener listener;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String messageJson = (String)execution.getVariable("msg");

        listener.handleMessage(messageJson);
    }

}

