package com.ultimate.workflow.camunda;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CamundaProcessEnginePlugin extends AbstractProcessEnginePlugin {

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        if (processEngineConfiguration.getCustomPostBPMNParseListeners() == null) {
            processEngineConfiguration.setCustomPostBPMNParseListeners(new ArrayList<>());
        }

        processEngineConfiguration
                .getCustomPostBPMNParseListeners()
                .add(new BpmnExtensionParseListener());
    }
}
