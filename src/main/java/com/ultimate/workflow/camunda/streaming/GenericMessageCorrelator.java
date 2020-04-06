package com.ultimate.workflow.camunda.streaming;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.ultimate.workflow.camunda.Constants.ZERO_UUID;

@Component
public class GenericMessageCorrelator {

    private final Logger LOGGER = Logger.getLogger(GenericMessageCorrelator.class.getName());

    @Autowired
    private ProcessEngine camunda;

    @Autowired
    private MessageTypeMapper messageTypeMapper;

    public List<MessageCorrelationResult> correlate(GenericMessage genericMessage) {
        Iterable<MessageTypeExtensionData> correlationDataList =
                retrieveMessageTypeExtensionData(genericMessage.getTenantId(), genericMessage.getMessageType());

        for (MessageTypeExtensionData messageTypeExtensionData : correlationDataList) {
            CorrelationData correlationData =
                    buildCorrelationData(genericMessage, messageTypeExtensionData);

            // Determine if any instances are interested in this message
            List<MessageCorrelationResult> results =
                    executeCorrelation(correlationData);

            return results;
        }

        return null;
    }

    private Iterable<MessageTypeExtensionData> retrieveMessageTypeExtensionData(String tenantId, String messageType) {
        return messageTypeMapper.find(tenantId, messageType);
    }

    private CorrelationData buildCorrelationData(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        String tenantId = genericMessage.getTenantId();

        // Correlation data parsed from the document
        DocumentContext documentContext = JsonPath.parse(genericMessage.getBody());

        CorrelationData correlationData =
                new CorrelationData(
                        genericMessage.getMessageType(),
                        evaluateBusinessKeyExpression(tenantId, documentContext, messageTypeExtensionData));
        correlationData.setTenantId(tenantId);
        correlationData.setStartEvent(messageTypeExtensionData.isStartEvent());
        correlationData.setProcessDefinitionKey(messageTypeExtensionData.getProcessDefinitionKey());

        messageTypeExtensionData.getMatchVariableExpressions().forEach((e) -> {
            correlationData.getMatchVariables().put(e.getKey(), evaluateExpression(documentContext, e.getValue()));
        });

        messageTypeExtensionData.getInputVariableExpressions().forEach((e) -> {
            correlationData.getInputVariables().put(e.getKey(), evaluateExpression(documentContext, e.getValue()));
        });

        return correlationData;
    }

    private List<MessageCorrelationResult> executeCorrelation(CorrelationData correlationData) {
        if (correlationData.isStartEvent()) {
            return executeStartMessageEventCorrelation(correlationData);
        } else {
            return executeCatchMessageEventCorrelation(correlationData);
        }
    }

    private List<MessageCorrelationResult> executeStartMessageEventCorrelation(CorrelationData correlationData) {
        MessageCorrelationBuilder messageCorrelationBuilder =
                camunda.getRuntimeService()
                        .createMessageCorrelation(correlationData.getMessageType())
                        .processInstanceBusinessKey(correlationData.getBusinessKey());

        // assign tenant id
        if (!ZERO_UUID.equals(correlationData.getTenantId())) {
            messageCorrelationBuilder
                    .tenantId(correlationData.getTenantId());
        }

        // assign variable inputs
        correlationData.getInputVariables().forEach((k, v) -> {
            messageCorrelationBuilder.setVariable(k, v);
        });

        // execute the correlation
        try{
            // Limit to start events if the correlation data is for start event
            messageCorrelationBuilder.startMessageOnly();
            return Arrays.asList(messageCorrelationBuilder.correlateWithResult());
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

    private List<MessageCorrelationResult> executeCatchMessageEventCorrelation(CorrelationData correlationData) {
        List<Execution> executions = determineCorrelatableProcessesInstances(correlationData);

        List<MessageCorrelationResult> results = new ArrayList<>();
        executions.forEach((e) -> {
            List<MessageCorrelationResult> r = executeCatchMessageEventCorrelationByExecution(correlationData, e);
            results.addAll(r);
        });

        return results;
    }

    private List<Execution> determineCorrelatableProcessesInstances(CorrelationData correlationData) {
        ExecutionQuery executionQuery = camunda.getRuntimeService()
                .createExecutionQuery()
                .messageEventSubscriptionName(correlationData.getMessageType())
                .processInstanceBusinessKey(correlationData.getBusinessKey())
                .tenantIdIn(correlationData.getTenantId());

        correlationData.getMatchVariables().forEach((k, v) -> {
            executionQuery.processVariableValueEquals(k, v);
        });

        return executionQuery.list();
    }

    private List<MessageCorrelationResult> executeCatchMessageEventCorrelationByExecution(CorrelationData correlationData, Execution execution) {
        MessageCorrelationBuilder messageCorrelationBuilder =
                camunda.getRuntimeService()
                        .createMessageCorrelation(correlationData.getMessageType())
                        .processInstanceId(execution.getProcessInstanceId());

        // assign variable inputs
        correlationData.getInputVariables().forEach((k, v) -> {
            messageCorrelationBuilder.setVariable(k, v);
        });

        // execute the correlation
        try{
            return Arrays.asList(messageCorrelationBuilder.correlateWithResult());
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

    private String evaluateBusinessKeyExpression(String tenantId, DocumentContext documentContext, MessageTypeExtensionData messageTypeExtensionData) {
        try {
            String businessKey = evaluateExpression(documentContext, messageTypeExtensionData.getBusinessKeyExpression());
            if (businessKey == null) {
                LOGGER.warning("Could not find business key for"
                        + " tenant id=\"" + tenantId + "\""
                        + " message type=" + messageTypeExtensionData.getMessageType() + "\""
                        + " and business key expresssion \"" + messageTypeExtensionData.getBusinessKeyExpression() + "\"");
                return null;
            }

            return businessKey;
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

    private static String evaluateExpression(DocumentContext documentContext, String expression) {
        // Since we are using JacksonJsonNodeJsonProvider we need to convert
        // the result of the JsonPath into the value we need
        JsonNode node = documentContext.read(expression);
        return node.textValue();
    }

}
