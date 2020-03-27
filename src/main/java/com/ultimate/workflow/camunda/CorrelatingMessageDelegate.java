package com.ultimate.workflow.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.logging.Logger;

@Component
public class CorrelatingMessageDelegate implements JavaDelegate {

    private final Logger LOGGER = Logger.getLogger(CorrelatingMessageDelegate.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private ProcessEngine camunda;

    @Autowired
    private MessageTypeMapper messageTypeMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String messageJson = (String)execution.getVariable("msg");

        executionInternal(messageJson);
    }

    private void executionInternal(String messageJson) throws JsonProcessingException {
        GenericMessage genericMessage = ParseMessageJson(messageJson);

        Iterable<CorrelationData> correlationDataList = RetrieveCorrelationData(genericMessage.getMessageType());

        for (CorrelationData correlationData : correlationDataList) {
            String businessKey = buildBusinessKey(correlationData, genericMessage);

            // Determine if any instances are interested in this message
            List<MessageCorrelationResult> results =
                    executeCorrelation(genericMessage.getMessageType(), businessKey);

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

    private List<MessageCorrelationResult> executeCorrelation(String messageType, String businessKey) {
        List<MessageCorrelationResult> results = camunda.getRuntimeService()
                .createMessageCorrelation(messageType)
                .processInstanceBusinessKey(businessKey)
                .correlateAllWithResult();

        return results;
    }

    private String buildBusinessKey(CorrelationData correlationData, GenericMessage<JsonNode> genericMessage) {
        //Configuration.defaultConfiguration().jsonProvider().parse(genericMessage.getBody().toString())
//        Configuration conf = Configuration
//                .builder()
//                .jsonProvider(new JacksonJsonNodeJsonProvider(objectMapper))
//                .build();
//
//        DocumentContext parsedBody = JsonPath.using(conf).parse(genericMessage.getBody());
        DocumentContext parsedBody = JsonPath.parse(genericMessage.getBody());
        try {
            // Since we are using JacksonJsonNodeJsonProvider we need to convert
            // the result of the JsonPath into the value we need
            String businessKey = ((JsonNode)parsedBody.read(correlationData.getBusinessKeyExpression())).textValue();
            return businessKey;
        } catch (Exception ex) {
            LOGGER.warning(ex.toString());
            throw ex;
        }
    }

    private Iterable<CorrelationData> RetrieveCorrelationData(String messageType) {
        return messageTypeMapper.find(messageType);
    }

    private GenericMessage ParseMessageJson(String messageJson) throws JsonProcessingException {
        GenericMessage<JsonNode> message = objectMapper
                .readValue(messageJson, new TypeReference<GenericMessage<JsonNode>>(){});
        return message;
    }

}

