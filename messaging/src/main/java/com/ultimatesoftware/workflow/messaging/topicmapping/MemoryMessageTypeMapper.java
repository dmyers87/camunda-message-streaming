package com.ultimatesoftware.workflow.messaging.topicmapping;

import com.ultimatesoftware.workflow.messaging.Constants;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
