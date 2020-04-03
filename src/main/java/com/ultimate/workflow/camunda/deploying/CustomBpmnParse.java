package com.ultimate.workflow.camunda.deploying;

import com.ultimate.workflow.camunda.streaming.MessageTypeExtensionData;
import com.ultimate.workflow.camunda.streaming.MessageTypeMapper;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.MessageDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class CustomBpmnParse extends BpmnParse {
    private final MessageTypeMapper mapper;

    public CustomBpmnParse(BpmnParser bpmnParser, MessageTypeMapper mapper) {
        super(bpmnParser);
        this.mapper = mapper;
    }

    @Override
    protected void parseProcessDefinitionStartEvent(ActivityImpl startEventActivity, Element startEventElement, Element parentElement, ScopeImpl scope) {
        super.parseProcessDefinitionStartEvent(startEventActivity, startEventElement, parentElement, scope);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope;

        // Is this start element a message start event
        Element messageEventDefinitionElement = startEventElement.element(MESSAGE_EVENT_DEFINITION);
        if (messageEventDefinitionElement == null) {
            return;
        }

        parseProcessDefinitionStartEventMessageExtensions(processDefinition, startEventElement, messageEventDefinitionElement);
    }

    private void parseProcessDefinitionStartEventMessageExtensions(ProcessDefinitionEntity processDefinition, Element startEventElement, Element messageEventDefinitionElement) {
        Element extensionsElement = startEventElement.element("extensionElements");
        if (extensionsElement == null) {
            return;
        }

        parseExtensionProperties(processDefinition, messageEventDefinitionElement, extensionsElement);
    }

    private void parseExtensionProperties(ProcessDefinitionEntity processDefinition, Element messageEventDefinition, Element extensionsElement) {
        Element propertiesElement = extensionsElement.elementNS(CAMUNDA_BPMN_EXTENSIONS_NS, "properties");
        if (propertiesElement == null) {
            return;
        }

        // property data elements
        String tenantId = deployment.getTenantId();
        String messageType = translateToMessageTypeName(messageEventDefinition);

        MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder =
                MessageTypeExtensionData.builder(messageType);
        for (Element propertyElement : propertiesElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "property")) {
            if (attributeNameStartsWith(propertyElement, "name", "ultimate.workflow.")) {
                createMessageMapping(processDefinition, propertyElement, builder);
            }
        }
        mapper.add(tenantId, builder.build());
    }

    private boolean attributeNameStartsWith(Element element, String attribute, String startsWith) {
        String value = element.attribute(attribute);
        return value != null ? value.startsWith(startsWith) : false;
    }

    private void createMessageMapping(ProcessDefinitionEntity processDefinition, Element propertyElement, MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder) {
        String name = propertyElement.attribute("name");
        String value = propertyElement.attribute("value");

        if (name == null || value == null){
            throw new IllegalArgumentException("Name and value cannot be null, please correct " + processDefinition.getName() + " message definition");
        }

        String[] parts = name.split("[.]");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException("Unsupported number of parts in property name \"" + name + "\"");
        }

        // system defined token
        String token = parts[2];

        switch (token) {
            case "business-process-key":
                builder.withBusinessKeyExpression(value);
                break;
            case "match-var":
                // match variable mappings
                String matchVariableName = parts[3];
                builder.withMatchVariable(matchVariableName, value);
                break;
            case "input-var":
                // match variable mappings
                String inputVariableName = parts[3];
                builder.withInputVariable(inputVariableName, value);
                break;
            default:
                throw new IllegalArgumentException("unknown token \"" + token + "\"");
        }
    }

    private String translateToMessageTypeName(Element messageEventDefinition) {
        String messageRef = messageEventDefinition.attribute("messageRef");
        MessageDefinition messageDefinition = messages.get(resolveName(messageRef));
        return messageDefinition.getExpression().getExpressionText();
    }
}