package com.ultimate.workflow.camunda.streaming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.ultimate.workflow.camunda.Constants.ZERO_UUID;

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

        Iterable<MessageTypeExtensionData> correlationDataList =
                retrieveMessageTypeExtensionData(genericMessage.getTenantId(), genericMessage.getMessageType());

        for (MessageTypeExtensionData messageTypeExtensionData : correlationDataList) {
            CorrelationData correlationData =
                    buildCorrelationData(genericMessage, messageTypeExtensionData);

            // Determine if any instances are interested in this message
            List<MessageCorrelationResult> results =
                    executeCorrelation(correlationData);

            //logResults(genericMessage.getTenantId(), genericMessage.getMessageType(), results);
        }
    }

    private GenericMessage parseMessageJson(String messageJson) throws JsonProcessingException {
        GenericMessage message = objectMapper
                .readValue(messageJson, new TypeReference<GenericMessage>(){});
        return message;
    }

    private Iterable<MessageTypeExtensionData> retrieveMessageTypeExtensionData(String tenantId, String messageType) {
        return messageTypeMapper.find(tenantId, messageType);
    }

    private CorrelationData buildCorrelationData(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        String tenantId = genericMessage.getTenantId();

        // Correlation data parsed from the document
        DocumentContext documentContext = JsonPath.parse(genericMessage.getBody());

        CorrelationData correlationData =
                new CorrelationData(
                        genericMessage.getMessageType(),
                        evaluateBusinessKeyExpression(tenantId, documentContext, messageTypeExtensionData));
        correlationData.setTenantId(tenantId);

        messageTypeExtensionData.getMatchVariableExpressions().forEach((k, v) -> {
                correlationData.getMatchVariables().put(k, evaluateExpression(documentContext, v));
        });

        messageTypeExtensionData.getInputVariableExpressions().forEach((k, v) -> {
            correlationData.getInputVariables().put(k, evaluateExpression(documentContext, v));
        });

        return correlationData;
    }

    private List<MessageCorrelationResult> executeCorrelation(CorrelationData correlationData) {
        List<MessageCorrelationResult> results = null;

        MessageCorrelationBuilder messageCorrelationBuilder =
                camunda.getRuntimeService()
                .createMessageCorrelation(correlationData.getMessageType())
                .processInstanceBusinessKey(correlationData.getBusinessKey());

        // assign tenant id
        if (!ZERO_UUID.equals(correlationData.getTenantId())) {
            messageCorrelationBuilder
                    .tenantId(correlationData.getTenantId());
        }

        // assign variable matchers
        correlationData.getMatchVariables().forEach((k, v) -> {
            messageCorrelationBuilder.processInstanceVariableEquals(k, v);
        });

        // assign variables inputs
        correlationData.getInputVariables().forEach((k, v) -> {
            messageCorrelationBuilder.setVariable(k, v);
        });

        // execute the correlation
        results = messageCorrelationBuilder.correlateAllWithResult();

        return results;
    }

    private String evaluateBusinessKeyExpression(String tenantId, DocumentContext documentContext, MessageTypeExtensionData messageTypeExtensionData) {
        try {
            String businessKey = evaluateExpression(documentContext, messageTypeExtensionData.getBusinessKeyExpression());
            if (businessKey == null) {
                LOGGER.warning("Could not find business key for"
                        + " tenant id=\"" + tenantId + "\""
                        + " message type=" + messageTypeExtensionData.getMessageType() + "\""
                        + " and business key expresssion \"" + messageTypeExtensionData.getBusinessKeyExpression() + "\"");
                return null;
            }

            return businessKey;
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

    private static String evaluateExpression(DocumentContext documentContext, String expression) {
        // Since we are using JacksonJsonNodeJsonProvider we need to convert
        // the result of the JsonPath into the value we need
        JsonNode node = documentContext.read(expression);
        return node.textValue();
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

                LOGGER.info("\n\n  ... Correlated"
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
