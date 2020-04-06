package com.ultimate.workflow.camunda.streaming;

public interface MessageTypeMapper {
    void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData);

    Iterable<MessageTypeExtensionData> find(String tenantId, String messageType);
}
