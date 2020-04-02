package com.ultimate.workflow.camunda.streaming;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeExtensionData {
    private String messageType;
    private String businessKeyExpression;
    private Map<String, Object> variables;

    public static MessageTypeExtensionDataBuilder builder(String messageType) {
        return new MessageTypeExtensionDataBuilder(messageType);
    }

    public MessageTypeExtensionData(String messageType, String businessKeyExpression) {
        this.messageType = messageType;
        this.businessKeyExpression = businessKeyExpression;
        this.variables = new HashMap<>();
    }

    public String getMessageType() {
        return messageType;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageTypeExtensionData that = (MessageTypeExtensionData) o;

        if (!messageType.equals(that.messageType)) return false;
        if (!businessKeyExpression.equals(that.businessKeyExpression)) return false;
        return variables.equals(that.variables);
    }

    @Override
    public int hashCode() {
        int result = messageType.hashCode();
        result = 31 * result + businessKeyExpression.hashCode();
        result = 31 * result + variables.hashCode();
        return result;
    }

    public static class MessageTypeExtensionDataBuilder {
        private String messageType;
        private String businessKeyExpression;
        private Map<String, Object> variables;

        public MessageTypeExtensionDataBuilder(String messageType) {
            this.messageType = messageType;
            this.variables = new HashMap<>();
        }

        public MessageTypeExtensionDataBuilder withBusinessKeyExpression(String businessKeyExpression) {
            this.businessKeyExpression = businessKeyExpression;
            return this;
        }

        public MessageTypeExtensionDataBuilder withVariable(String name, Object value) {
            this.variables.put(name, value);
            return this;
        }

        public MessageTypeExtensionData build() {
            if (messageType == null || businessKeyExpression == null) {
                throw new IllegalArgumentException("Message type and business key can not be null");
            }

            MessageTypeExtensionData data = new MessageTypeExtensionData(messageType, businessKeyExpression);
            data.getVariables().putAll(variables);
            return data;
        }
    }
}
