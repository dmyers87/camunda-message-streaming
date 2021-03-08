package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class MessageTypeExtensionData {
    private final String processDefinitionKey;
    private final String messageType;
    private final String topic;
    private final String businessKeyExpression;
    private final boolean isStartEvent;
    private final Map<String, String> matchVariableExpressions = new HashMap<>();
    private final Map<String, String> inputVariableExpressions = new HashMap<>();

    public static MessageTypeExtensionDataBuilder builder(String processDefinitionKey, String messageType) {
        return new MessageTypeExtensionDataBuilder(processDefinitionKey, messageType);
    }

    private MessageTypeExtensionData(
            @NotNull String processDefinitionKey,
            @NotNull String topic,
            @NotNull String messageType,
            @NotNull String businessKeyExpression,
            boolean isStartEvent,
            @NotNull Map<String, String> matchVariableExpressions,
            @NotNull Map<String, String> inputVariableExpressions) {
        Assert.notNull(processDefinitionKey, "processDefinitionKey can not be null");
        Assert.notNull(topic, "topic can not be null");
        Assert.notNull(messageType, "messageType can not be null");
        Assert.notNull(businessKeyExpression, "businessKeyExpression can not be null");
        Assert.notNull(matchVariableExpressions, "matchVariableExpressions can not be null");
        Assert.notNull(inputVariableExpressions, "inputVariableExpressions can not be null");
        this.processDefinitionKey = processDefinitionKey;
        this.topic = topic;
        this.messageType = messageType;
        this.businessKeyExpression = businessKeyExpression;
        this.isStartEvent = isStartEvent;
        this.matchVariableExpressions.putAll(matchVariableExpressions);
        this.inputVariableExpressions.putAll(inputVariableExpressions);
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTopic() {
        return topic;
    }

    public String getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public boolean isStartEvent() {
        return isStartEvent;
    }

    public Iterable<Map.Entry<String, String>> getMatchVariableExpressions() {
        return this.matchVariableExpressions.entrySet();
    }

    public Iterable<Map.Entry<String, String>> getInputVariableExpressions() {
        return this.inputVariableExpressions.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageTypeExtensionData that = (MessageTypeExtensionData) o;

        if (isStartEvent != that.isStartEvent) return false;
        if (!processDefinitionKey.equals(that.processDefinitionKey)) return false;
        if (!messageType.equals(that.messageType)) return false;
        if (!topic.equals(that.topic)) return false;
        if (!businessKeyExpression.equals(that.businessKeyExpression)) return false;
        if (!matchVariableExpressions.equals(that.matchVariableExpressions)) return false;
        return inputVariableExpressions.equals(that.inputVariableExpressions);
    }

    @Override
    public int hashCode() {
        int result = processDefinitionKey.hashCode();
        result = 31 * result + messageType.hashCode();
        result = 31 * result + topic.hashCode();
        result = 31 * result + businessKeyExpression.hashCode();
        result = 31 * result + (isStartEvent ? 1 : 0);
        result = 31 * result + matchVariableExpressions.hashCode();
        result = 31 * result + inputVariableExpressions.hashCode();
        return result;
    }

    public static class MessageTypeExtensionDataBuilder {
        private String topic;
        private String messageType;
        private String processDefinitionKey;
        private String businessKeyExpression;
        private boolean isStartEvent;
        private Map<String, String> matchVariableExpressions;
        private Map<String, String> inputVariableExpressions;

        public MessageTypeExtensionDataBuilder(String processDefinitionKey, String messageType) {
            this.processDefinitionKey = processDefinitionKey;
            this.messageType = messageType;
            this.matchVariableExpressions = new HashMap<>();
            this.inputVariableExpressions = new HashMap<>();
        }

        public MessageTypeExtensionDataBuilder addFieldFromPropertyElement(ProcessDefinitionEntity processDefinition,
                                                                           Element propertyElement,
                                                                           MetadataValueEvaluator metadataValueEvaluator) {
            CustomExtensionElementParseFactory.parseExtensionElement(
                    processDefinition,
                    propertyElement,
                    metadataValueEvaluator,
                    this);
            return this;
        }

        public MessageTypeExtensionDataBuilder withTopic(String value) {
            this.topic = value;
            return this;
        }

        public MessageTypeExtensionDataBuilder withBusinessKeyExpression(String businessKeyExpression) {
            this.businessKeyExpression = businessKeyExpression;
            return this;
        }

        public MessageTypeExtensionDataBuilder setStartEvent(boolean isStartEvent) {
            this.isStartEvent = isStartEvent;
            return this;
        }

        public MessageTypeExtensionDataBuilder withMatchVariable(String name, String expression) {
            this.matchVariableExpressions.put(name, expression);
            return this;
        }

        public MessageTypeExtensionDataBuilder withMatchVariables(Map<String, String> matchVariableExpressions) {
            this.matchVariableExpressions.putAll(matchVariableExpressions);
            return this;
        }

        public MessageTypeExtensionDataBuilder withInputVariable(String name, String expression) {
            this.inputVariableExpressions.put(name, expression);
            return this;
        }

        public MessageTypeExtensionDataBuilder withInputVariables(Map<String, String> inputVariableExpressions) {
            this.inputVariableExpressions.putAll(inputVariableExpressions);
            return this;
        }

        public MessageTypeExtensionData build() {
            if (messageType == null || businessKeyExpression == null) {
                throw new IllegalArgumentException("Message type and business key can not be null");
            }

            ensureMatchVariablesNotSetOnStartEvent();

            MessageTypeExtensionData data = new MessageTypeExtensionData(
                    processDefinitionKey,
                    topic,
                    messageType,
                    businessKeyExpression,
                    isStartEvent,
                    matchVariableExpressions,
                    inputVariableExpressions);
            return data;
        }

        private void ensureMatchVariablesNotSetOnStartEvent() {
            if (isStartEvent && !matchVariableExpressions.isEmpty()) {
                throw new IllegalArgumentException("Match variables cannot be provided on start events");
            }
        }
    }
}
