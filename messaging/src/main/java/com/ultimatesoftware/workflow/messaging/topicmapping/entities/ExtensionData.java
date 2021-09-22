package com.ultimatesoftware.workflow.messaging.topicmapping.entities;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Entity
public class ExtensionData {

    public ExtensionData() {
    }

    public ExtensionData(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        this.tenantId = tenantId;

        deploymentId = messageTypeExtensionData.getDeploymentId();
        processDefinitionId = messageTypeExtensionData.getProcessDefinitionId();
        version = messageTypeExtensionData.getVersion();
        processDefinitionKey = messageTypeExtensionData.getProcessDefinitionKey();
        activityId = messageTypeExtensionData.getActivityId();
        topic = messageTypeExtensionData.getTopic();
        messageType = messageTypeExtensionData.getMessageType();
        businessKeyExpression = messageTypeExtensionData.getBusinessKeyExpression();
        isStartEvent = messageTypeExtensionData.isStartEvent();

        matchVariableExpressions = new HashMap<>();
        messageTypeExtensionData.getMatchVariableExpressions().forEach(mv ->
                this.matchVariableExpressions.put(mv.getKey(),mv.getValue()));

        matchLocalVariableExpressions = new HashMap<>();
        messageTypeExtensionData.getMatchLocalVariableExpressions().forEach(mv ->
            this.matchLocalVariableExpressions.put(mv.getKey(),mv.getValue()));

        inputVariableExpressions = new HashMap<>();
        messageTypeExtensionData.getInputVariableExpressions().forEach(iv ->
                this.inputVariableExpressions.put(iv.getKey(), iv.getValue()));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String tenantId;

    @NotNull
    private String deploymentId;

    private String processDefinitionId;
    private int version;

    @NotNull
    private String processDefinitionKey;

    @NotNull
    private String activityId;

    @NotNull
    private String topic;

    @NotNull
    private String messageType;

    @NotNull
    private String businessKeyExpression;

    @NotNull
    private Boolean isStartEvent;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "expression")
    @CollectionTable(name = "matchVariableExpressions")
    Map<String, String> matchVariableExpressions;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "expression")
    @CollectionTable(name = "matchLocalVariableExpressions")
    Map<String, String> matchLocalVariableExpressions;


    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "expression")
    @CollectionTable(name = "inputVariableExpressions")
    Map<String, String> inputVariableExpressions;

    public String getMessageType() {
        return messageType;
    }

    public String getTopic() {
        return topic;
    }

    public String getTenantId() {
        return tenantId;
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
        return processDefinitionKey;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public Boolean getStartEvent() {
        return isStartEvent;
    }

    public Map<String, String> getMatchVariableExpressions() {
        return matchVariableExpressions;
    }

    public Map<String, String> getMatchLocalVariableExpressions() {
        return matchLocalVariableExpressions;
    }

    public Map<String, String> getInputVariableExpressions() {
        return inputVariableExpressions;
    }
}
