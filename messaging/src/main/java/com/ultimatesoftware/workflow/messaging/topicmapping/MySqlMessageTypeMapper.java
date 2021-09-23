package com.ultimatesoftware.workflow.messaging.topicmapping;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.entities.ExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.repositories.ExtensionDataRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;

public class MySqlMessageTypeMapper implements MessageTypeMapper {

    @Autowired
    public ExtensionDataRepository extensionDataRepository;

    @Override
    public void add(String tenantId, MessageTypeExtensionData messageTypeExtensionData) {
        ExtensionData extensionData = new ExtensionData(tenantId, messageTypeExtensionData);
        extensionDataRepository.save(extensionData);
    }

    @Override
    public void initializeProcessDefinitionIds(String deploymentId, List<ProcessDefinition> processDefinitionEntities) {
        List<ExtensionData> extensionDataList = extensionDataRepository.findAllByDeploymentId(deploymentId);

        for (ExtensionData extensionData : extensionDataList) {
            String processDefinitionId = processDefinitionEntities.stream()
                .filter(processDefinition -> processDefinition.getKey()
                    .equals(extensionData.getProcessDefinitionKey()))
                .findFirst()
                .get()
                .getId();

            extensionData.setProcessDefinitionId(processDefinitionId);
        }

        extensionDataRepository.saveAll(extensionDataList);
    }

    @Override
    public Iterable<MessageTypeExtensionData> find(String topic, String tenantId, String messageType) {
        return getTypeExtensionDataSet(extensionDataRepository.findAllByTopicAndTenantIdAndMessageType(topic,
                tenantId, messageType));
    }

    public Iterable<MessageTypeExtensionData> find(String tenantId, String processDefinitionKey) {
        return getTypeExtensionDataSet(extensionDataRepository.findAllByTenantIdAndProcessDefinitionKey(tenantId,
            processDefinitionKey));
    }

    public Iterable<MessageTypeExtensionData> getAll() {
        return getTypeExtensionDataSet(extensionDataRepository.findAll());
    }

    private Set<MessageTypeExtensionData> getTypeExtensionDataSet(Iterable<ExtensionData> extensionDataList)  {
        Set<MessageTypeExtensionData> messageTypeExtensionDataSet = new HashSet<>();
        extensionDataList.forEach(e -> {
            MessageTypeExtensionData messageTypeExtensionData =
                    MessageTypeExtensionData.builder(e.getDeploymentId(), e.getProcessDefinitionKey(), e.getMessageType())
                        .withProcessDefinitionId(e.getProcessDefinitionId())
                        .withVersion(e.getVersion())
                        .withActivityId(e.getActivityId())
                        .withBusinessKeyExpression(e.getBusinessKeyExpression())
                        .withTopic(e.getTopic())
                        .setStartEvent(e.getStartEvent())
                        .withInputVariables(e.getInputVariableExpressions())
                        .withMatchVariables(e.getMatchVariableExpressions())
                        .withMatchLocalVariables(e.getMatchLocalVariableExpressions())
                        .build();
            messageTypeExtensionDataSet.add(messageTypeExtensionData);
        });
        return messageTypeExtensionDataSet;
    }
}
