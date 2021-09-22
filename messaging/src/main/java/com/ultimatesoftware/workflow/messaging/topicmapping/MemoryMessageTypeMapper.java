package com.ultimatesoftware.workflow.messaging.topicmapping;

import com.ultimatesoftware.workflow.messaging.Constants;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.repository.ProcessDefinition;

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
    public void initializeProcessDefinitionIds(String deploymentId, List<ProcessDefinition> processDefinitionEntities) {
        String tenantId = processDefinitionEntities.get(0).getTenantId();
        Map<String, Set<MessageTypeExtensionData>> tenantFilteredMap = getTenantFilteredMap(tenantId);

        processDefinitionEntities.forEach(processDefinition ->
            tenantFilteredMap.forEach((key, messageTypeExtensionDataSet) ->
                messageTypeExtensionDataSet.stream()
                    .filter(messageTypeExtensionData -> messageTypeExtensionData.getProcessDefinitionKey().equals(processDefinition.getKey()))
                    .forEach(messageTypeExtensionData -> messageTypeExtensionData.setProcessDefinitionId(processDefinition.getId()))));
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


    private Map<String, Set<MessageTypeExtensionData>> getTenantFilteredMap(String tenantId) {
        List<String> tenantFilteredKeys = mappings.keySet().stream()
            .filter(key -> key.contains(tenantId))
            .collect(Collectors.toList());

        return mappings.entrySet().stream()
            .filter(entry -> tenantFilteredKeys.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
