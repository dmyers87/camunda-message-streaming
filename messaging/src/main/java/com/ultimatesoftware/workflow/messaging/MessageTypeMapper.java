package com.ultimatesoftware.workflow.messaging;

public interface MessageTypeMapper {
    void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData);

    Iterable<MessageTypeExtensionData> find(String tenantId, String messageType);
}
