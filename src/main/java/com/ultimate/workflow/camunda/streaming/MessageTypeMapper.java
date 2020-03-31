package com.ultimate.workflow.camunda.streaming;

import com.ultimate.workflow.camunda.Constants;

import java.util.*;

public class MessageTypeMapper {
    private Dictionary<String, List<MessageTypeExtensionData>> mappings = new Hashtable<>();

    public void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        String messageType = messageTypeExtensionData.getMessageType();

        List<MessageTypeExtensionData> data = mappings.get(buildKey(tenantId, messageType));
        if (data == null) {
            data = new ArrayList<>();
            data.add(messageTypeExtensionData);
            mappings.put(buildKey(tenantId, messageType), data);
        } else {
            data.add(messageTypeExtensionData);
        }
    }

    public Iterable<MessageTypeExtensionData> find(String tenantId, String messageType) {
        Iterable<MessageTypeExtensionData> result = mappings.get(buildKey(tenantId, messageType));
        if (result != null) {
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    private String buildKey(String tenantId, String messageType) {
        return messageType + ":" + (tenantId == null ? Constants.ZERO_UUID : tenantId).toLowerCase();
    }

}
