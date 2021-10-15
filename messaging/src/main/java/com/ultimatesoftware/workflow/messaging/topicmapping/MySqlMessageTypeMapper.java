package com.ultimatesoftware.workflow.messaging.topicmapping;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.correlation.GenericMessageCorrelator;
import com.ultimatesoftware.workflow.messaging.topicmapping.entities.ExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.repositories.ExtensionDataRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MySqlMessageTypeMapper implements MessageTypeMapper {

    private final Logger LOGGER = LoggerFactory.getLogger(MySqlMessageTypeMapper.class.getName());

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
        LOGGER.debug("Initializing process definition ids under deployment {} for the following extension data: {}",
            deploymentId, extensionDataList);

        for (ExtensionData extensionData : extensionDataList) {
            if (extensionData.getProcessDefinitionId() != null) {
                LOGGER.trace("Extension data {} already has process definition id set. Skipping over data element",
                    extensionData);
            } else {
                String processDefinitionId =
                    getMatchProcessDefinitionIdForExtensionElement(deploymentId, processDefinitionEntities, extensionData);

                extensionData.setProcessDefinitionId(processDefinitionId);
            }
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

    private String getMatchProcessDefinitionIdForExtensionElement(String deploymentId, List<ProcessDefinition> processDefinitionEntities, ExtensionData extensionData) {
        return processDefinitionEntities.stream()
            .filter(processDefinition -> processDefinition.getKey()
                .equals(extensionData.getProcessDefinitionKey()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                String.format("Unable to initialize Process Definition Ids for Extension Data. "
                        + "No Process Definition with key %s found on deployment %s for tenant %s.",
                    extensionData.getProcessDefinitionKey(), deploymentId, extensionData.getTenantId())))
            .getId();
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
