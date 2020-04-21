package com.ultimatesoftware.workflow.webapp;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class LoggerDelegate implements JavaDelegate {

    private final Logger LOGGER = Logger.getLogger(LoggerDelegate.class.getName());

    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("\n\nLoggerDelegate invoked by {"
                + "\n processDefinitionId=" + execution.getProcessDefinitionId() + ","
                + "\n tenantId=" + execution.getTenantId() + ","
                //+ "\n activityId=" + execution.getCurrentActivityId()
                //+ "\n activityName='" + execution.getCurrentActivityName().replaceAll("\n", " ") + "'"
                + "\n processInstanceId=" + execution.getProcessInstanceId() + ","
                + "\n businessKey=" + execution.getProcessBusinessKey() + ","
                + "\n executionId=" + execution.getId() + ","
                //+ "\n modelName=" + execution.getBpmnModelInstance().getModel().getModelName() + ","
                //+ "\n elementId" + execution.getBpmnModelElementInstance().getId() + ","
                + "\n variables=" + execution.getVariables().toString().replaceAll("\n", "\n ")
                + "\n} \n\n");

    }

}