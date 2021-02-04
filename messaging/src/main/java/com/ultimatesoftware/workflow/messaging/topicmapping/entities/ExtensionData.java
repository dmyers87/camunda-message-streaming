package com.ultimatesoftware.workflow.messaging.topicmapping.entities;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class ExtensionData {

    public ExtensionData (){
    }
    
    public ExtensionData(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        this.tenantId = tenantId;
        processDefinitionKey = messageTypeExtensionData.getProcessDefinitionKey();
        topic = messageTypeExtensionData.getTopic();
        messageType = messageTypeExtensionData.getMessageType();
        businessKeyExpression = messageTypeExtensionData.getBusinessKeyExpression();
        isStartEvent = messageTypeExtensionData.isStartEvent();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    private String tenantId;

    @NotNull
    private String processDefinitionKey;

    @NotNull
    private String topic;

    @NotNull
    private String messageType;

    @NotNull
    private String businessKeyExpression;

    @NotNull
    private Boolean isStartEvent;

    public String getMessageType() {
        return messageType;
    }

    public String getTopic() {
        return topic;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getBusinessKeyExpression() {
        return businessKeyExpression;
    }

    public Boolean getStartEvent() {
        return isStartEvent;
    }
}
