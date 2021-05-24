package com.ultimatesoftware.workflow.messaging.correlation;

import java.util.HashMap;
import java.util.Map;

class CorrelationData {
    private String messageType;
    private String tenantId;
    private String businessKey;
    private String processDefinitionKey;
    private boolean isStartEvent;
    private Map<String, Object> matchVariables;
    private Map<String, Object> inputVariables;

    public CorrelationData(String messageType, String businessKey) {
        this.messageType = messageType;
        this.businessKey = businessKey;
        this.matchVariables = new HashMap<>();
        this.inputVariables = new HashMap<>();
    }

    public CorrelationData(String messageType, String tenantId, String businessKey, String processDefinitionKey, boolean isStartEvent,
                           Map<String, Object> matchVariables, Map<String, Object> inputVariables) {
        this.messageType = messageType;
        this.tenantId = tenantId;
        this.businessKey = businessKey;
        this.processDefinitionKey = processDefinitionKey;
        this.isStartEvent = isStartEvent;
        this.matchVariables = matchVariables;
        this.inputVariables = inputVariables;
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

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public Map<String, Object> getMatchVariables() {
        return matchVariables;
    }

    public void setMatchVariables(Map<String, String> value) {
        this.matchVariables = new HashMap<>(value);
    }

    public Map<String, Object> getInputVariables() {
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
}
