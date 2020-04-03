package com.ultimate.workflow.camunda.streaming;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeExtensionData {
    private String messageType;
    private String businessKeyExpression;
    private Map<String, String> matchVariableExpressions;
    private Map<String, String> inputVariableExpressions;

    public static MessageTypeExtensionDataBuilder builder(String messageType) {
        return new MessageTypeExtensionDataBuilder(messageType);
    }

    public MessageTypeExtensionData(String messageType, String businessKeyExpression) {
        this.messageType = messageType;
        this.businessKeyExpression = businessKeyExpression;
        this.matchVariableExpressions = new HashMap<>();
        this.inputVariableExpressions = new HashMap<>();
    }

    public String getMessageType() {
        return messageType;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public Map<String, String> getMatchVariableExpressions() {
        return this.matchVariableExpressions;
    }

    public Map<String, String> getInputVariableExpressions() {
        return this.inputVariableExpressions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageTypeExtensionData that = (MessageTypeExtensionData) o;

        if (!messageType.equals(that.messageType)) return false;
        if (!businessKeyExpression.equals(that.businessKeyExpression)) return false;
        if (!matchVariableExpressions.equals(that.matchVariableExpressions)) return false;
        return inputVariableExpressions.equals(that.inputVariableExpressions);
    }

    @Override
    public int hashCode() {
        int result = messageType.hashCode();
        result = 31 * result + businessKeyExpression.hashCode();
        result = 31 * result + matchVariableExpressions.hashCode();
        result = 31 * result + inputVariableExpressions.hashCode();
        return result;
    }

    public static class MessageTypeExtensionDataBuilder {
        private String messageType;
        private String businessKeyExpression;
        private Map<String, String> matchVariableExpressions;
        private Map<String, String> inputVariableExpressions;

        public MessageTypeExtensionDataBuilder(String messageType) {
            this.messageType = messageType;
            this.matchVariableExpressions = new HashMap<>();
            this.inputVariableExpressions = new HashMap<>();
        }

        public MessageTypeExtensionDataBuilder withBusinessKeyExpression(String businessKeyExpression) {
            this.businessKeyExpression = businessKeyExpression;
            return this;
        }

        public MessageTypeExtensionDataBuilder withMatchVariable(String name, String expression) {
            this.matchVariableExpressions.put(name, expression);
            return this;
        }

        public MessageTypeExtensionDataBuilder withInputVariable(String name, String expression) {
            this.inputVariableExpressions.put(name, expression);
            return this;
        }

        public MessageTypeExtensionData build() {
            if (messageType == null || businessKeyExpression == null) {
                throw new IllegalArgumentException("Message type and business key can not be null");
            }

            MessageTypeExtensionData data = new MessageTypeExtensionData(messageType, businessKeyExpression);
            data.getMatchVariableExpressions().putAll(matchVariableExpressions);
            data.getInputVariableExpressions().putAll(inputVariableExpressions);
            return data;
        }
    }
}
