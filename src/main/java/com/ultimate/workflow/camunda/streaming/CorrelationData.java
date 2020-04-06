package com.ultimate.workflow.camunda.streaming;

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
}
