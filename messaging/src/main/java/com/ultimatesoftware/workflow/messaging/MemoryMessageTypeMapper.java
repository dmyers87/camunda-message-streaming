package com.ultimatesoftware.workflow.messaging;

import java.util.*;

public class MemoryMessageTypeMapper implements MessageTypeMapper {
    private Map<String, Set<MessageTypeExtensionData>> mappings = new HashMap<>();

    @Override
    public void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        String topic = messageTypeExtensionData.getTopic();
        String messageType = messageTypeExtensionData.getMessageType();

        Set<MessageTypeExtensionData> data = mappings.get(buildKey(topic, tenantId, messageType));
        if (data == null) {
            data = new HashSet<>();
            data.add(messageTypeExtensionData);
            mappings.put(buildKey(topic, tenantId, messageType), data);
        } else {
            data.add(messageTypeExtensionData);
        }
    }

    @Override
    public Iterable<MessageTypeExtensionData> find(String topic, String tenantId, String messageType) {
        Iterable<MessageTypeExtensionData> result = mappings.get(buildKey(topic, tenantId, messageType));
        if (result != null) {
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Iterable<MessageTypeExtensionData> getAll() {
        Set<MessageTypeExtensionData> extensionData = new HashSet<>();
        mappings.forEach((k, v) -> {
            extensionData.addAll(v);
        });
        return extensionData;
    }

    private String buildKey(String topic, String tenantId, String messageType) {
        return topic.toLowerCase() + ":" + messageType + ":" + (tenantId == null ? Constants.ZERO_UUID : tenantId).toLowerCase();
    }

}
