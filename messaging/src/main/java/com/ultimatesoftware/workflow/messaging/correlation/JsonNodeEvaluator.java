package com.ultimatesoftware.workflow.messaging.correlation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.POJONode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class JsonNodeEvaluator {

    private ObjectMapper objectMapper = new ObjectMapper()
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    public JsonNodeEvaluator() {
    }

    public Object evaluateNode(JsonNode node) {
        if (node.isTextual()) {
            return node.textValue();
        } else if (node.isArray()) {
            return evaluateArrayNode((ArrayNode) node);
        } else if (node.isPojo()) {
            return evaluatePOJONode((POJONode) node);
        } else if (node.isInt()) {
            return node.intValue();
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else if (node.isDouble()) {
            return node.doubleValue();
        } else {
            return node.toString();
        }
    }

    private Object evaluateArrayNode(ArrayNode arrayNode) {
        Iterator<JsonNode> jsonNodeList = arrayNode.elements();
        List<Object> list = new ArrayList<>(arrayNode.size());

        while (jsonNodeList.hasNext()) {
            JsonNode jsonNode = jsonNodeList.next();
            list.add(evaluateNode(jsonNode));
        }

        return list;
    }

    private Object evaluatePOJONode(POJONode pojoNode) {
        try {
            String jsonString = objectMapper.writeValueAsString(pojoNode.getPojo());
            Map<String, Object> objectMap = objectMapper.readValue(jsonString, Map.class);
            return objectMap;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(String.format("Unable to parse POJONode value %s into Map, the following exception occurred: %s",
                pojoNode.getPojo().toString(), ex.getMessage()));
        }
    }
}
