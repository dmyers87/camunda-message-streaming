package com.ultimatesoftware.workflow.messaging.correlation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class CorrelationDataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationDataUtils.class.getName());
    private static final ObjectMapper SORTED_MAPPER = new ObjectMapper();

    static {
        SORTED_MAPPER
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    private CorrelationDataUtils() {
    }

    public static CorrelationData buildCorrelationData(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

        // Correlation data parsed from the document
        DocumentContext documentContext = JsonPath.parse(genericMessage.getBody(), configuration);

        String messageType = genericMessage.getMessageType();
        String businessKey = evaluateExpression(documentContext, messageTypeExtensionData.getBusinessKeyExpression());
        String tenantId = genericMessage.getTenantId();
        boolean isStartEvent = messageTypeExtensionData.isStartEvent();
        String processDefinitionKey = messageTypeExtensionData.getProcessDefinitionKey();

        Map<String, Object> matchVariables = createMatchVariablesFromExtensionData(documentContext, messageTypeExtensionData.getMatchVariableExpressions());
        Map<String, Object> inputVariables = createInputVariablesFromExtensionData(documentContext, messageTypeExtensionData.getInputVariableExpressions());

        return new CorrelationData(messageType, tenantId, businessKey, processDefinitionKey, isStartEvent, matchVariables, inputVariables);
    }

    private static Map<String, Object> createMatchVariablesFromExtensionData(DocumentContext documentContext,
                                                                             Iterable<Map.Entry<String, String>> matchVariableExpressions) {
        Map<String, Object> matchVariables = new HashMap<>();

        matchVariableExpressions.forEach(entry ->
            matchVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return matchVariables;
    }

    private static Map<String, Object> createInputVariablesFromExtensionData(DocumentContext documentContext,
                                                                             Iterable<Map.Entry<String, String>> inputVariableExpressions) {
        Map<String, Object> inputVariables = new HashMap<>();

        inputVariableExpressions.forEach((entry) ->
            inputVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return inputVariables;
    }

    private static String evaluateExpression(DocumentContext documentContext, String expression) {
        if (isExpressionConstant(expression)) {
            LOGGER.debug("Expression '{}' is constant, returning immediately", expression);
            // By returning without parsing, this assumes constant expressions are strings only
            // Parsing the expression can get messy since it might not be valid JSON
            return expression;
        }
        try {
            // Since we are using JacksonJsonNodeJsonProvider we need to convert
            // the result of the JsonPath into the value we need
            JsonNode node = documentContext.read(expression);
            if (node == null || node.isNull()) {
                String errorMessage = String.format("Unable to resolve expression %s in context %s", expression, documentContext);
                LOGGER.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            // Returns evaluated JSON string with the keys of the map being ordered
            return SORTED_MAPPER.writeValueAsString(SORTED_MAPPER.treeToValue(node, Object.class));
        } catch (JsonProcessingException ex) {
            // This error shouldn't ever occur
            LOGGER.error("Unable to process JSON expression {} in context {}", expression, documentContext);
            throw new RuntimeException(ex);
        }
    }

    private static boolean isExpressionConstant(String expression) {
        return expression.charAt(0) != '$';
    }

}
