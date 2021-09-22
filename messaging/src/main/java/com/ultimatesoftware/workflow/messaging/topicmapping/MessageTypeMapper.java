package com.ultimatesoftware.workflow.messaging.topicmapping;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.List;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public interface MessageTypeMapper {
    void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData);

    void initializeProcessDefinitionIds(String deploymentId, List<ProcessDefinition> processDefinitionEntities);

    Iterable<MessageTypeExtensionData> find(String topic, String tenantId, String messageType);

    Iterable<MessageTypeExtensionData> getAll();
}
