package com.ultimate.workflow.camunda;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class MessageTypeMapper {
    private Dictionary<String, List<MessageTypeExtensionData>> mappings = new Hashtable<>();

    public Iterable<MessageTypeExtensionData> find(String messageType) {
        return mappings.get(messageType);
    }

    public void add(MessageTypeExtensionData messageTypeExtensionData) {
        String messageType = messageTypeExtensionData.getMessageType();

        List<MessageTypeExtensionData> data = mappings.get(messageType);
        if (data == null) {
            data = new ArrayList<>();
            data.add(messageTypeExtensionData);
            mappings.put(messageType, data);
        } else {
            data.add(messageTypeExtensionData);
        }
    }
}
