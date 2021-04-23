package com.ultimatesoftware.workflow.messaging.topicmapping.repositories;

import com.ultimatesoftware.workflow.messaging.topicmapping.entities.ExtensionData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtensionDataRepository extends CrudRepository<ExtensionData, Integer> {

    List<ExtensionData> findAllByTopicAndTenantIdAndMessageType(String topic, String tenantId, String messageType);

    List<ExtensionData> findAllByTenantIdAndProcessDefinitionKey(String tenantId, String processDefinitionKey);
}
