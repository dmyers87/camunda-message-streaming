package com.ultimate.workflow.camunda.streaming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.ultimate.workflow.camunda.Constants.ZERO_UUID;

@Component
@EnableBinding(Sink.class)
public class CorrelatingMessageListener {

    private final Logger LOGGER = Logger.getLogger(CorrelatingMessageListener.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private GenericMessageCorrelator correlator;

    @StreamListener(target = Sink.INPUT)
    @Transactional
    public void handleMessage(String messageJson) throws JsonProcessingException {
        GenericMessage genericMessage = parseMessageJson(messageJson);

        List<MessageCorrelationResult> results = correlator.correlate(genericMessage);

        //logResults(genericMessage.getTenantId(), genericMessage.getMessageType(), results);
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
