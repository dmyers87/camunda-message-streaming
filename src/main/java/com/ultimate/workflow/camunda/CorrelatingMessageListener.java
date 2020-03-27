package com.ultimate.workflow.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.util.List;
import java.util.logging.Logger;

@Component
@EnableBinding(Sink.class)
public class CorrelatingMessageListener {

    private final Logger LOGGER = Logger.getLogger(CorrelatingMessageListener.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private ProcessEngine camunda;

    @Autowired
    private MessageTypeMapper messageTypeMapper;


    @StreamListener(target = Sink.INPUT)
    @Transactional
    public void handleMessage(String messageJson) throws JsonProcessingException {
        GenericMessage genericMessage = parseMessageJson(messageJson);

        Iterable<MessageTypeExtensionData> correlationDataList = retrieveCorrelationData(genericMessage.getMessageType());

        for (MessageTypeExtensionData messageTypeExtensionData : correlationDataList) {
            CorrelationData correlationData = buildCorrelationData(genericMessage, messageTypeExtensionData);

            // Determine if any instances are interested in this message
            List<MessageCorrelationResult> results =
                    executeCorrelation(genericMessage.getMessageType(), correlationData);

            logResults(genericMessage.getMessageType(), results);
        }
    }

    private void logResults(String messageType, List<MessageCorrelationResult> results) {
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

                LOGGER.info("\n\n  ... Correlated"
                        + " message type \"" + messageType + "\""
                        + " to a \"" + result.getResultType().name() + "\""
                        + " with process instance identifier \"" + identifier + "\""
                        + " for definition \"" + definitionId + "\""
                        + " with business key \"" + businessKey +"\"");
            }
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
        }
    }

    private List<MessageCorrelationResult> executeCorrelation(String messageType, CorrelationData correlationData) {
        List<MessageCorrelationResult> results = camunda.getRuntimeService()
                .createMessageCorrelation(messageType)
                .processInstanceBusinessKey(correlationData.getBusinessKey())
                .correlateAllWithResult();

        return results;
    }

    private String buildBusinessKey(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        DocumentContext parsedBody = JsonPath.parse(genericMessage.getBody());
        try {
            // Since we are using JacksonJsonNodeJsonProvider we need to convert
            // the result of the JsonPath into the value we need
            String businessKey = ((JsonNode)parsedBody.read(messageTypeExtensionData.getBusinessKeyExpression())).textValue();
            return businessKey;
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

    private CorrelationData buildCorrelationData(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setBusinessKey(buildBusinessKey(genericMessage, messageTypeExtensionData));
        return correlationData;
    }

    private Iterable<MessageTypeExtensionData> retrieveCorrelationData(String messageType) {
        return messageTypeMapper.find(messageType);
    }

    private GenericMessage parseMessageJson(String messageJson) throws JsonProcessingException {
        GenericMessage message = objectMapper
                .readValue(messageJson, new TypeReference<GenericMessage>(){});
        return message;
    }
}
