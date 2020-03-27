package com.ultimate.workflow.camunda;

public class MessageTypeExtensionData {
    private String messageType;
    private String businessKeyExpression;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public void setBusinessKeyExpression(String businessKeyExpression) {
        this.businessKeyExpression = businessKeyExpression;
    }
}
