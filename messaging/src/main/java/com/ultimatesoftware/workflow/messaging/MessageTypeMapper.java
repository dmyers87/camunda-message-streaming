package com.ultimatesoftware.workflow.messaging;

public interface MessageTypeMapper {
    void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData);

    Iterable<MessageTypeExtensionData> find(String topic, String tenantId, String messageType);

    Iterable<MessageTypeExtensionData> getAll();
}
