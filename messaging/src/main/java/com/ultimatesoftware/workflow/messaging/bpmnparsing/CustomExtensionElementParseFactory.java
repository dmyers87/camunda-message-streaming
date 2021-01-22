package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.exceptions.ExtensionElementNotParsableException;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class CustomExtensionElementParseFactory {

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

        // system defined token
        String token = parts[2];

        switch (token) {
            case "topic":
                builder.withTopic(value);
                break;
            case "business-process-key":
                builder.withBusinessKeyExpression(value);
                break;
            case "match-var":
                // match variable mappings
                String matchVariableName = parts[3];
                if (matchVariableName.equals("business-process-key")) {
                    // supports 3 and 4 part declarations
                    builder.withBusinessKeyExpression(value);
                } else {
                    builder.withMatchVariable(matchVariableName, value);
                }
                break;
            case "input-var":
                // match variable mappings
                String inputVariableName = parts[3];
                builder.withInputVariable(inputVariableName, value);
                break;
            default:
                throw new ExtensionElementNotParsableException("unknown token \"" + token + "\"");
        }

    }
}
