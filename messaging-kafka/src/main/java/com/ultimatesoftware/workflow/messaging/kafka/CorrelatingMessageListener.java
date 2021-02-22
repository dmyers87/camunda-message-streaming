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
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

public class CorrelatingMessageListener implements MessageListener<String, String> {

    private final Logger LOGGER = Logger.getLogger(CorrelatingMessageListener.class.getName());

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
            LOGGER.warning("Consumer received an empty record from topic: " + record.topic());
            return;
        }
        onMessage(record.topic(), record.value());
    }

    private void onMessage(String topic, String messageJson) {
        try {
            GenericMessage genericMessage = parseMessageJson(messageJson);

            String tenantId = genericMessage.getTenantId();
            String messageType = genericMessage.getMessageType();

            Iterable<MessageTypeExtensionData> messageTypeExtensionDataList =
                    messageTypeMapper.find(topic, tenantId, messageType);

            List<MessageCorrelationResult> results = correlator.correlate(genericMessage, messageTypeExtensionDataList);

            logResults(genericMessage.getTenantId(), genericMessage.getMessageType(), results);
        } catch (RuntimeException ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        } catch (JsonProcessingException ex) {
            LOGGER.warning(ex.toString());
            throw new RuntimeException("Error parse message body", ex);
        } catch (Throwable ex) {
            LOGGER.warning(ex.toString());
            throw new RuntimeException("Converting to runtime exception", ex);
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
                    identifier = result.getExecution().getProcessInstanceId();
                    definitionId = "unknown";
                    businessKey = "unknown";
                }

                LOGGER.fine("\n\n  ... Correlated"
                        + " message type \"" + messageType + "\""
                        + " for tenant \"" + tenantId + "\""
                        + " to a \"" + result.getResultType().name() + "\""
                        + " with process instance identifier \"" + identifier + "\""
                        + " for definition \"" + definitionId + "\""
                        + " with business key \"" + businessKey +"\"");
            }
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
        }
    }
}
