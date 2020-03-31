package com.ultimate.workflow.camunda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class MessageTypeMapper {
    private Dictionary<String, List<MessageTypeExtensionData>> mappings = new Hashtable<>();

    public void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        String messageType = messageTypeExtensionData.getMessageType();

        List<MessageTypeExtensionData> data = mappings.get(messageType);
        if (data == null) {
            data = new ArrayList<>();
            data.add(messageTypeExtensionData);
            mappings.put(buildKey(messageType, tenantId), data);
        } else {
            data.add(messageTypeExtensionData);
        }
    }

    public Iterable<MessageTypeExtensionData> find(String messageType, String tenantId) {
        Iterable<MessageTypeExtensionData> result = mappings.get(buildKey(messageType, tenantId));
        if (result != null) {
            return result;
        }

        return Collections.EMPTY_LIST;
    }

    private String buildKey(String messageType, String tenantId) {
        return (messageType + ":" + tenantId).toLowerCase();
    }

}
