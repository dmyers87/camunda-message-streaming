package com.ultimatesoftware.workflow.messaging.correlation;

import java.util.HashMap;
import java.util.Map;

class CorrelationData {
    private String messageType;
    private String tenantId;
    private String businessKey;
    private String processDefinitionKey;
    private String activityId;
    private boolean isStartEvent;
    private Map<String, Object> matchVariables;
    private Map<String, Object> matchLocalVariables;
    private Map<String, Object> inputVariables;

    public CorrelationData(String messageType, String businessKey) {
        this.messageType = messageType;
        this.businessKey = businessKey;
        this.matchVariables = new HashMap<>();
        this.inputVariables = new HashMap<>();
    }

    public CorrelationData(String messageType, String tenantId, String businessKey, String processDefinitionKey, String activityId, boolean isStartEvent,
                           Map<String, Object> matchVariables, Map<String, Object> matchLocalVariables, Map<String, Object> inputVariables) {
        this.messageType = messageType;
        this.tenantId = tenantId;
        this.businessKey = businessKey;
        this.processDefinitionKey = processDefinitionKey;
        this.activityId = activityId;
        this.isStartEvent = isStartEvent;
        this.matchVariables = matchVariables;
        this.matchLocalVariables = matchLocalVariables;
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

    public String getActivityId() {
        return activityId;
    }

    public Map<String, Object> getMatchVariables() {
        return matchVariables;
    }

    public void setMatchVariables(Map<String, String> value) {
        this.matchVariables = new HashMap<>(value);
    }

    public Map<String, Object> getMatchLocalVariables() {
        return matchLocalVariables;
    }

    public void setMatchLocalVariables(Map<String, String> value) {
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

    @Override
    public String toString() {
        return "CorrelationData{" +
            "messageType='" + messageType + '\'' +
            ", tenantId='" + tenantId + '\'' +
            ", businessKey='" + businessKey + '\'' +
            ", processDefinitionKey='" + processDefinitionKey + '\'' +
            ", isStartEvent=" + isStartEvent +
            ", matchVariables=" + matchVariables +
            ", matchLocalVariables=" + matchLocalVariables +
            ", inputVariables=" + inputVariables +
            '}';
    }
}
