package com.ultimate.workflow.camunda.streaming;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeExtensionData {
    private String messageType;
    private String businessKeyExpression;
    private Map<String, Object> variables;

    public MessageTypeExtensionData(String messageType) {
        this.messageType = messageType;
        this.variables = new HashMap<>();
    }

    public String getMessageType() {
        return messageType;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public void setBusinessKeyExpression(String businessKeyExpression) {
        this.businessKeyExpression = businessKeyExpression;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }
}
