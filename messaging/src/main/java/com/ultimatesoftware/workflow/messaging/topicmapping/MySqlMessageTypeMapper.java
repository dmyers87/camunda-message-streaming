package com.ultimatesoftware.workflow.messaging.topicmapping;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.entities.ExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.repositories.ExtensionDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class MySqlMessageTypeMapper implements MessageTypeMapper {

    @Autowired
    public ExtensionDataRepository extensionDataRepository;

    @Override
    public void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        ExtensionData extensionData = new ExtensionData(tenantId, messageTypeExtensionData);
        extensionDataRepository.save(extensionData);
    }

    @Override
    public Iterable<MessageTypeExtensionData> find(String topic, String tenantId, String messageType) {
        return getTypeExtensionDataSet(extensionDataRepository.findAllByTopicAndTenantIdAndMessageType(topic,
                tenantId, messageType));
    }

    public Iterable<MessageTypeExtensionData> getAll() {
        return getTypeExtensionDataSet(extensionDataRepository.findAll());
    }

    private Set<MessageTypeExtensionData> getTypeExtensionDataSet(Iterable<ExtensionData> extensionDataList)  {
        Set<MessageTypeExtensionData> messageTypeExtensionDataSet = new HashSet<>();
        extensionDataList.forEach(e -> {
            MessageTypeExtensionData messageTypeExtensionData =
                    MessageTypeExtensionData.builder(e.getProcessDefinitionKey(), e.getMessageType())
                            .withBusinessKeyExpression(e.getBusinessKeyExpression())
                            .withTopic(e.getTopic())
                            .setStartEvent(e.getStartEvent())
                            .build();
            messageTypeExtensionDataSet.add(messageTypeExtensionData);
        });
        return messageTypeExtensionDataSet;
    }
}
