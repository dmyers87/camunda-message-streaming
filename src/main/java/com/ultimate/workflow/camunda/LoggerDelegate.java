package com.ultimate.workflow.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class LoggerDelegate implements JavaDelegate {

    private final Logger LOGGER = Logger.getLogger(LoggerDelegate.class.getName());

    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("\n\n  ... LoggerDelegate invoked by "
                + "processDefinitionId=" + execution.getProcessDefinitionId()
                + ", activityId=" + execution.getCurrentActivityId()
                + ", activityName='" + execution.getCurrentActivityName().replaceAll("\n", " ") + "'"
                + ", processInstanceId=" + execution.getProcessInstanceId()
                + ", businessKey=" + execution.getProcessBusinessKey()
                + ", executionId=" + execution.getId()
                + ", modelName=" + execution.getBpmnModelInstance().getModel().getModelName()
                + ", elementId" + execution.getBpmnModelElementInstance().getId()
                + " \n\n");

    }

}