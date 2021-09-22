package com.ultimatesoftware.workflow.messaging.correlation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CorrelationDataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationDataUtils.class.getName());
    private static final JsonNodeEvaluator jsonNodeEvaluator = new JsonNodeEvaluator();

    private CorrelationDataUtils() {}

    public static CorrelationData buildCorrelationData(GenericMessage genericMessage, MessageTypeExtensionData messageTypeExtensionData) {
        Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

        // Correlation data parsed from the document
        DocumentContext documentContext = JsonPath.parse(genericMessage.getBody(), configuration);

        String messageType = genericMessage.getMessageType();
        String businessKey = evaluateExpression(documentContext, messageTypeExtensionData.getBusinessKeyExpression()).toString();
        String tenantId = genericMessage.getTenantId();
        boolean isStartEvent = messageTypeExtensionData.isStartEvent();
        String processDefinitionId = messageTypeExtensionData.getProcessDefinitionId();
        int version = messageTypeExtensionData.getVersion();
        String processDefinitionKey = messageTypeExtensionData.getProcessDefinitionKey();
        String activityId = messageTypeExtensionData.getActivityId();

        Map<String, Object> matchVariables = createMatchVariablesFromExtensionData(documentContext, messageTypeExtensionData.getMatchVariableExpressions());
        Map<String, Object> matchLocalVariables = createMatchLocalVariablesFromExtensionData(documentContext, messageTypeExtensionData.getMatchLocalVariableExpressions());
        Map<String, Object> inputVariables = createInputVariablesFromExtensionData(documentContext, messageTypeExtensionData.getInputVariableExpressions());

        return new CorrelationData(messageType, tenantId, businessKey, processDefinitionId, version, processDefinitionKey,
            activityId, isStartEvent, matchVariables, matchLocalVariables, inputVariables);
    }

    private static Map<String, Object> createMatchVariablesFromExtensionData(DocumentContext documentContext,
                                                                             Iterable<Map.Entry<String, String>> matchVariableExpressions) {
        Map<String, Object> matchVariables = new HashMap<>();

        matchVariableExpressions.forEach(entry ->
            matchVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return matchVariables;
    }

    private static Map<String, Object> createMatchLocalVariablesFromExtensionData(DocumentContext documentContext,
                                                                                  Iterable<Map.Entry<String, String>> matchLocalVariableExpressions) {
        Map<String, Object> matchLocalVariables = new HashMap<>();

        matchLocalVariableExpressions.forEach(entry ->
            matchLocalVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return matchLocalVariables;
    }

    private static Map<String, Object> createInputVariablesFromExtensionData(DocumentContext documentContext,
                                                                             Iterable<Map.Entry<String, String>> inputVariableExpressions) {
        Map<String, Object> inputVariables = new HashMap<>();

        inputVariableExpressions.forEach((entry) ->
            inputVariables.put(entry.getKey(), evaluateExpression(documentContext, entry.getValue())));

        return inputVariables;
    }

    private static Object evaluateExpression(DocumentContext documentContext, String expression) {
        if (isExpressionConstant(expression)) {
            LOGGER.debug("Expression '{}' is a constant expression, the value will be evaluated as a string and " +
                "returned immediately", expression);
            return expression;
        }

        // Since we are using JacksonJsonNodeJsonProvider we need to convert
        // the result of the JsonPath into the value we need
        JsonNode node = documentContext.read(expression);
        return jsonNodeEvaluator.evaluateNode(node);
    }

    private static boolean isExpressionConstant(String expression) {
        return expression.charAt(0) != '$';
    }

}
