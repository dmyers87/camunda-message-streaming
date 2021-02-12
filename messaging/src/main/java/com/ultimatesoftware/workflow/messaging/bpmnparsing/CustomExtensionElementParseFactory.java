package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.jayway.jsonpath.JsonPath;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.exceptions.ExtensionElementNotParsableException;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class CustomExtensionElementParseFactory {

    private static final int TOKEN_INDEX = 2;
    private static final String INPUT_VAR = "input-var";
    private static final String TOPIC = "topic";
    private static final String MATCH_VAR = "match-var";
    private static final String BUSINESS_PROCESS_KEY = "business-process-key";

    public static void parseExtensionElement(ProcessDefinitionEntity processDefinition,
                                             Element propertyElement,
                                             MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder) {
        String name = propertyElement.attribute("name");
        String value = propertyElement.attribute("value");

        if (name == null || value == null) {
            throw new ExtensionElementNotParsableException(
                "Name and value cannot be null, please correct " + processDefinition.getName() + " message definition");
        }

        String[] parts = name.split("[.]");
        if (parts.length < 3 || parts.length > 4) {
            throw new ExtensionElementNotParsableException(
                "Unsupported number of parts in property name \"" + name + "\"");
        }

        String token = parts[TOKEN_INDEX];

        if (!token.equals(TOPIC)) {
            assertIsValidJsonPath(value);
        }

        switch (token) {
            case TOPIC:
                builder.withTopic(value);
                break;
            case BUSINESS_PROCESS_KEY:
                builder.withBusinessKeyExpression(value);
                break;
            case MATCH_VAR:
                // match variable mappings
                String matchVariableName = parts[3];
                if (matchVariableName.equals("business-process-key")) {
                    // supports 3 and 4 part declarations
                    builder.withBusinessKeyExpression(value);
                } else {
                    builder.withMatchVariable(matchVariableName, value);
                }
                break;
            case INPUT_VAR:
                // match variable mappings
                String inputVariableName = parts[3];
                builder.withInputVariable(inputVariableName, value);
                break;
            default:
                throw new ExtensionElementNotParsableException("unknown token \"" + token + "\"");
        }
    }

    private static void assertIsValidJsonPath(String value) {
        try {
            JsonPath.compile(value);
        } catch (Exception e) {
            throw new ExtensionElementNotParsableException("Provided expression \""
                    + value + "\" is not a valid JSON path: "
                    + e.getMessage());
        }
    }
}
