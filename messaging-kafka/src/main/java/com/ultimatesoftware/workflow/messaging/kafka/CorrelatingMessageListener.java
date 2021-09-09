package com.ultimatesoftware.workflow.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.correlation.GenericMessageCorrelator;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class CorrelatingMessageListener implements MessageListener<String, String> {

    private final Logger LOGGER = LoggerFactory.getLogger(CorrelatingMessageListener.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final GenericMessageCorrelator correlator;

    private final MessageTypeMapper messageTypeMapper;

    public CorrelatingMessageListener(GenericMessageCorrelator correlator, MessageTypeMapper messageTypeMapper) {
        this.correlator = correlator;
        this.messageTypeMapper = messageTypeMapper;
    }

    @Transactional
    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        if (record.value().isEmpty()) {
            LOGGER.warn("Consumer received an empty record from topic: {}", record.topic());
            return;
        }

        LOGGER.debug("Consumer message with key {} and value {} received under topic {}",
            record.key(), record.value(), record.topic());
        onMessage(record.topic(), record.value());
    }

    private void onMessage(String topic, String messageJson) {
        try {
            GenericMessage genericMessage = parseMessageJson(messageJson);

            String tenantId = genericMessage.getTenantId();
            String messageType = genericMessage.getMessageType();

            Iterable<MessageTypeExtensionData> messageTypeExtensionDataList =
                    messageTypeMapper.find(topic, tenantId, messageType);

            if (!messageTypeExtensionDataList.iterator().hasNext()) {
                LOGGER.debug("No Message Extension Element found for topic {}, tenant {} and messageType {}",
                    topic, tenantId, messageType);
            }

            List<MessageCorrelationResult> results = correlator.correlate(genericMessage, messageTypeExtensionDataList);

            if (results.size() == 0) {
                LOGGER.debug("No correlations were made for Message {} and Message Extension List {}",
                    genericMessage.toString(), messageTypeExtensionDataList.toString());
            }

            logResults(genericMessage.getTenantId(), genericMessage.getMessageType(), results);
        } catch (RuntimeException ex) {
            LOGGER.warn("A runtime exception occurred while processing the message", ex);
            throw ex;
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Error parse message body", ex);
            throw new RuntimeException("Error parse message body", ex);
        } catch (Throwable ex) {
            LOGGER.warn("Converting throwable to runtime exception", ex);
            throw new RuntimeException("Converting throwable to runtime exception", ex);
        }
    }

    private GenericMessage parseMessageJson(String messageJson) throws JsonProcessingException {
        GenericMessage message = objectMapper
                .readValue(messageJson, new TypeReference<GenericMessage>(){});
        return message;
    }

    private void logResults(String tenantId, String messageType, List<MessageCorrelationResult> results) {
        try {
            for (MessageCorrelationResult result : results) {
                String identifier;
                String definitionId;
                String businessKey;

                if (result.getResultType() == MessageCorrelationResultType.ProcessDefinition) {
                    identifier = result.getProcessInstance().getProcessInstanceId();
                    definitionId = result.getProcessInstance().getProcessDefinitionId();
                    businessKey = result.getProcessInstance().getBusinessKey();
                } else {
                    ExecutionEntity executionEntity = (ExecutionEntity) result.getExecution();
                    identifier = executionEntity.getProcessInstanceId();
                    definitionId = executionEntity.getProcessDefinitionId();
                    businessKey = executionEntity.getProcessBusinessKey();
                }

                LOGGER.debug("\n\n  ... Correlated message type \"{}\" for tenant \"{}\" to a \"{}\" with process"
                        + " instance identifier \"{}\" for definition \"{}\" with business key \"{}\"",
                        messageType,
                        tenantId,
                        result.getResultType().name(),
                        identifier,
                        definitionId,
                        businessKey);
            }
        } catch (Exception ex) {
            LOGGER.warn("An exception occurred while logging the results", ex);
        }
    }
}
