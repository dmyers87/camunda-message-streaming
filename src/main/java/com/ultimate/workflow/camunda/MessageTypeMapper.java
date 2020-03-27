package com.ultimate.workflow.camunda;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class MessageTypeMapper {
    private Dictionary<String, List<CorrelationData>> mappings = new Hashtable<>();

    public Iterable<CorrelationData> find(String messageType) {
        return mappings.get(messageType);
    }

    public void add(String messageType, CorrelationData correlationData) {
        List<CorrelationData> data = mappings.get(messageType);
        if (data == null) {
            data = new ArrayList<>();
            data.add(correlationData);
            mappings.put(messageType, data);
        } else {
            data.add(correlationData);
        }
    }
}
