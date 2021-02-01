package com.ultimatesoftware.workflow.messaging.correlation;

import static com.ultimatesoftware.workflow.messaging.Constants.ZERO_UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.HashMap;
import java.util.Map;

class CorrelationData {
    private String messageType;
    private String tenantId;
    private String businessKey;
    private String processDefinitionKey;
    private boolean isStartEvent;
    private Map<String, String> matchVariables;
    private Map<String, String> inputVariables;

    public CorrelationData(String messageType, String businessKey) {
        this.messageType = messageType;
        this.businessKey = businessKey;
        this.matchVariables = new HashMap<>();
        this.inputVariables = new HashMap<>();
    }

    public CorrelationData(String messageType, String tenantId, String businessKey, String processDefinitionKey, boolean isStartEvent,
                           Map<String, String> matchVariables, Map<String, String> inputVariables) {
        this.messageType = messageType;
        this.tenantId = tenantId;
        this.businessKey = businessKey;
        this.processDefinitionKey = processDefinitionKey;
        this.isStartEvent = isStartEvent;
        this.matchVariables = matchVariables;
        this.inputVariables = inputVariables;
    }

    public static CorrelationData buildCorrelationData(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

        // Correlation data parsed from the document
        DocumentContext documentContext = JsonPath.parse(genericMessage.getBody(), configuration);

        String messageType = genericMessage.getMessageType();
        String businessKey = evaluateExpression(documentContext, messageTypeExtensionData.getBusinessKeyExpression());
        String tenantId = genericMessage.getTenantId();
        boolean isStartEvent = messageTypeExtensionData.isStartEvent();
        String processDefinitionKey = messageTypeExtensionData.getProcessDefinitionKey();

        Map<String, String> matchVariables = createMatchVariablesFromExtensionData(documentContext, messageTypeExtensionData.getMatchVariableExpressions());
        Map<String, String> inputVariables = createInputVariablesFromExtensionData(documentContext, messageTypeExtensionData.getInputVariableExpressions());

        return new CorrelationData(messageType, tenantId, businessKey, processDefinitionKey, isStartEvent, matchVariables, inputVariables);
    }

    public String getMessageType() {
        return messageType;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String value) {
        this.tenantId = value;
    }

    public boolean hasNonZeroTenantId() {
        return !ZERO_UUID.equals(tenantId);
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public Map<String, String> getMatchVariables() {
        return matchVariables;
    }

    public void setMatchVariables(Map<String, String> value) {
        this.matchVariables = new HashMap<>(value);
    }

    public Map<String, String> getInputVariables() {
        return inputVariables;
    }

    public void setInputVariables(Map<String, String> value) {
        this.inputVariables = new HashMap<>(value);
    }

    public boolean isStartEvent() {
        return isStartEvent;
    }

    public void setStartEvent(boolean startEvent) {
        isStartEvent = startEvent;
    }

    private static Map<String, String> createMatchVariablesFromExtensionData(DocumentContext documentContext,
                                                       Iterable<Map.Entry<String, String>> matchVariableExpressions) {
        Map<String, String> matchVariables = new HashMap<>();

        matchVariableExpressions.forEach((entry) ->
            matchVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return matchVariables;
    }

    private static Map<String, String> createInputVariablesFromExtensionData(DocumentContext documentContext,
                                                       Iterable<Map.Entry<String, String>> inputVariableExpressions) {
        Map<String, String> inputVariables = new HashMap<>();

        inputVariableExpressions.forEach((entry) ->
            inputVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return inputVariables;
    }

    private static String evaluateExpression(DocumentContext documentContext, String expression) {
        // Since we are using JacksonJsonNodeJsonProvider we need to convert
        // the result of the JsonPath into the value we need
        JsonNode node = documentContext.read(expression);
        return node.textValue();
    }
}
