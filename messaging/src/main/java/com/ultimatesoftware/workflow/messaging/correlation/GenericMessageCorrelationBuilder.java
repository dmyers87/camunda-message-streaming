package com.ultimatesoftware.workflow.messaging.correlation;

import java.util.Map;
import java.util.logging.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

public class GenericMessageCorrelationBuilder {

    private final Logger LOGGER = Logger.getLogger(GenericMessageCorrelationBuilder.class.getName());

    private MessageCorrelationBuilder messageCorrelationBuilder;

    public GenericMessageCorrelationBuilder(MessageCorrelationBuilder messageCorrelationBuilder) {
        this.messageCorrelationBuilder = messageCorrelationBuilder;
    }

    public static GenericMessageCorrelationBuilder builder(RuntimeService runtimeService, String messageType) {
        MessageCorrelationBuilder messageCorrelationBuilder = runtimeService
            .createMessageCorrelation(messageType);

        return new GenericMessageCorrelationBuilder(messageCorrelationBuilder);
    }

    public GenericMessageCorrelationBuilder withBusinessKey(String businessKey) {
        this.messageCorrelationBuilder = messageCorrelationBuilder.processInstanceBusinessKey(businessKey);
        return this;
    }

    public GenericMessageCorrelationBuilder withProcessInstanceId(String processInstanceId) {
        this.messageCorrelationBuilder = messageCorrelationBuilder.processInstanceId(processInstanceId);
        return this;
    }

    public GenericMessageCorrelationBuilder withTenantId(String tenantId) {
        this.messageCorrelationBuilder = messageCorrelationBuilder.tenantId(tenantId);
        return this;
    }

    public GenericMessageCorrelationBuilder withVariables(Map<String, String> variables) {
        variables.forEach((key, value) -> messageCorrelationBuilder.setVariable(key, value));
        return this;
    }

    public MessageCorrelationResult correlateStartMessageOnly() {
        this.messageCorrelationBuilder = messageCorrelationBuilder.startMessageOnly();
        return correlate();
    }

    public MessageCorrelationResult correlate() {
        try{
            return messageCorrelationBuilder.correlateWithResult();
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

}

