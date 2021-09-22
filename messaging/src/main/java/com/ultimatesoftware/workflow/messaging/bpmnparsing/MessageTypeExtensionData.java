package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageTypeExtensionData {
    private final String deploymentId;
    private String processDefinitionId;
    private final int version;
    private final String processDefinitionKey;
    private final String activityId;
    private final String messageType;
    private final String topic;
    private final String businessKeyExpression;
    private final boolean isStartEvent;
    private final Map<String, String> matchVariableExpressions = new HashMap<>();
    private final Map<String, String> matchLocalVariableExpressions = new HashMap<>();
    private final Map<String, String> inputVariableExpressions = new HashMap<>();

    public static MessageTypeExtensionDataBuilder builder(String deploymentId, String processDefinitionKey, String messageType) {
        return new MessageTypeExtensionDataBuilder(deploymentId, processDefinitionKey, messageType);
    }

    private MessageTypeExtensionData(
            @NotNull String deploymentId,
            @NotNull String processDefinitionKey,
            String processDefinitionId,
            int version,
            @NotNull String activityId,
            @NotNull String topic,
            @NotNull String messageType,
            @NotNull String businessKeyExpression,
            boolean isStartEvent,
            @NotNull Map<String, String> matchVariableExpressions,
            @NotNull Map<String, String> matchLocalVariableExpressions,
            @NotNull Map<String, String> inputVariableExpressions) {
        Assert.notNull(deploymentId, "deploymentId can not be null");
        Assert.notNull(processDefinitionKey, "processDefinitionKey can not be null");
        Assert.notNull(activityId, "activityId can not be null");
        Assert.notNull(topic, "topic can not be null");
        Assert.notNull(messageType, "messageType can not be null");
        Assert.notNull(businessKeyExpression, "businessKeyExpression can not be null");
        Assert.notNull(matchVariableExpressions, "matchVariableExpressions can not be null");
        Assert.notNull(matchLocalVariableExpressions, "matchLocalVariableExpressions can not be null");
        Assert.notNull(inputVariableExpressions, "inputVariableExpressions can not be null");
        this.deploymentId = deploymentId;
        this.processDefinitionId = processDefinitionId;
        this.version = version;
        this.processDefinitionKey = processDefinitionKey;
        this.activityId = activityId;
        this.topic = topic;
        this.messageType = messageType;
        this.businessKeyExpression = businessKeyExpression;
        this.isStartEvent = isStartEvent;
        this.matchVariableExpressions.putAll(matchVariableExpressions);
        this.matchLocalVariableExpressions.putAll(matchLocalVariableExpressions);
        this.inputVariableExpressions.putAll(inputVariableExpressions);
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTopic() {
        return topic;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public int getVersion() {
        return version;
    }

    public String getProcessDefinitionKey() {
        return this.processDefinitionKey;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public boolean isStartEvent() {
        return isStartEvent;
    }

    public boolean isCatchEvent() {
        return !isStartEvent;
    }

    public Iterable<Map.Entry<String, String>> getMatchVariableExpressions() {
        return this.matchVariableExpressions.entrySet();
    }

    public Iterable<Map.Entry<String, String>> getMatchLocalVariableExpressions() {
        return this.matchLocalVariableExpressions.entrySet();
    }

    public Iterable<Map.Entry<String, String>> getInputVariableExpressions() {
        return this.inputVariableExpressions.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageTypeExtensionData)) {
            return false;
        }
        MessageTypeExtensionData that = (MessageTypeExtensionData) o;
        return version == that.version &&
            isStartEvent == that.isStartEvent &&
            Objects.equals(deploymentId, that.deploymentId) &&
            Objects.equals(processDefinitionId, that.processDefinitionId) &&
            Objects.equals(processDefinitionKey, that.processDefinitionKey) &&
            Objects.equals(activityId, that.activityId) &&
            Objects.equals(messageType, that.messageType) &&
            Objects.equals(topic, that.topic) &&
            Objects.equals(businessKeyExpression, that.businessKeyExpression) &&
            Objects.equals(matchVariableExpressions, that.matchVariableExpressions) &&
            Objects.equals(matchLocalVariableExpressions, that.matchLocalVariableExpressions) &&
            Objects.equals(inputVariableExpressions, that.inputVariableExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, processDefinitionId, version, processDefinitionKey, activityId, messageType, topic, businessKeyExpression,
            isStartEvent, matchVariableExpressions, matchLocalVariableExpressions, inputVariableExpressions);
    }

    @Override
    public String toString() {
        return "MessageTypeExtensionData{" +
            "deploymentId='" + deploymentId + '\'' +
            ", processDefinitionId='" + processDefinitionId + '\'' +
            ", version=" + version +
            ", processDefinitionKey='" + processDefinitionKey + '\'' +
            ", activityId='" + activityId + '\'' +
            ", messageType='" + messageType + '\'' +
            ", topic='" + topic + '\'' +
            ", businessKeyExpression='" + businessKeyExpression + '\'' +
            ", isStartEvent=" + isStartEvent +
            ", matchVariableExpressions=" + matchVariableExpressions +
            ", matchLocalVariableExpressions=" + matchLocalVariableExpressions +
            ", inputVariableExpressions=" + inputVariableExpressions +
            '}';
    }

    public static class MessageTypeExtensionDataBuilder {
        private String topic;
        private String messageType;
        private String deploymentId;
        private String processDefinitionId;
        private int version;
        private String processDefinitionKey;
        private String activityId;
        private String businessKeyExpression;
        private boolean isStartEvent;
        private Map<String, String> matchVariableExpressions;
        private Map<String, String> matchLocalVariableExpressions;
        private Map<String, String> inputVariableExpressions;

        public MessageTypeExtensionDataBuilder(String deploymentId, String processDefinitionKey, String messageType) {
            this.deploymentId = deploymentId;
            this.processDefinitionKey = processDefinitionKey;
            this.messageType = messageType;
            this.matchVariableExpressions = new HashMap<>();
            this.matchLocalVariableExpressions = new HashMap<>();
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

        public MessageTypeExtensionDataBuilder withProcessDefinitionId(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public MessageTypeExtensionDataBuilder withVersion(int version) {
            this.version = version;
            return this;
        }

        public MessageTypeExtensionDataBuilder withActivityId(String activityId) {
            this.activityId = activityId;
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

        public MessageTypeExtensionDataBuilder withMatchLocalVariable(String name, String expression) {
            this.matchLocalVariableExpressions.put(name, expression);
            return this;
        }

        public MessageTypeExtensionDataBuilder withMatchLocalVariables(Map<String, String> matchLocalVariableExpressions) {
            this.matchLocalVariableExpressions.putAll(matchLocalVariableExpressions);
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
                deploymentId,
                processDefinitionKey,
                processDefinitionId,
                version,
                activityId,
                topic,
                messageType,
                businessKeyExpression,
                isStartEvent,
                matchVariableExpressions,
                matchLocalVariableExpressions,
                inputVariableExpressions);
            return data;
        }

        private void ensureMatchVariablesNotSetOnStartEvent() {
            if (isStartEvent && !matchVariableExpressions.isEmpty() && !matchLocalVariableExpressions.isEmpty()) {
                throw new IllegalArgumentException("Match variables cannot be provided on start events");
            }
        }
    }
}
