package com.ultimatesoftware.workflow.messaging;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.MessageDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class MessageExtensionBpmnParse extends BpmnParse {
    private final MessageTypeMapper mapper;
    private final String extensionPrefix;

    public MessageExtensionBpmnParse(BpmnParser bpmnParser, MessageTypeMapper mapper, String extensionPrefix) {
        super(bpmnParser);
        this.mapper = mapper;
        this.extensionPrefix = extensionPrefix;
    }

    @Override
    protected void parseProcessDefinitionStartEvent(ActivityImpl startEventActivity, Element startEventElement, Element parentElement, ScopeImpl scope) {
        super.parseProcessDefinitionStartEvent(startEventActivity, startEventElement, parentElement, scope);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope.getProcessDefinition();

        // Is this element a message event
        Element messageEventDefinitionElement = startEventElement.element(MESSAGE_EVENT_DEFINITION);
        if (messageEventDefinitionElement == null) {
            return;
        }

        parseElementExtensions(processDefinition, startEventElement, messageEventDefinitionElement, true);
    }

    @Override
    public ActivityImpl parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scopeElement, ActivityImpl eventBasedGateway) {
        ActivityImpl result = super.parseIntermediateCatchEvent(intermediateEventElement, scopeElement, eventBasedGateway);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scopeElement.getProcessDefinition();

        // Is this element a message event
        Element messageEventDefinitionElement = intermediateEventElement.element(MESSAGE_EVENT_DEFINITION);
        if (messageEventDefinitionElement == null) {
            return result;
        }

        parseElementExtensions(processDefinition, intermediateEventElement, messageEventDefinitionElement, false);

        return result;
    }

    @Override
    public void parseBoundaryEvents(Element parentElement, ScopeImpl flowScope) {
        super.parseBoundaryEvents(parentElement, flowScope);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) flowScope.getProcessDefinition();

        for (Element boundaryEventElement : parentElement.elements("boundaryEvent")) {
            // Is this element a message event
            Element messageEventDefinitionElement = parentElement.element(MESSAGE_EVENT_DEFINITION);
            if (messageEventDefinitionElement == null) {
                return;
            }

            parseElementExtensions(processDefinition, parentElement, messageEventDefinitionElement, false);

        }
    }

    private void parseElementExtensions(
            ProcessDefinitionEntity processDefinition,
            Element element,
            Element messageEventDefinitionElement,
            boolean isStarteEvent) {
        Element extensionsElement = element.element("extensionElements");
        if (extensionsElement == null) {
            return;
        }

        parseElementExtensionProperties(processDefinition, messageEventDefinitionElement, extensionsElement, isStarteEvent);
    }

    private void parseElementExtensionProperties(
            ProcessDefinitionEntity processDefinition,
            Element messageEventDefinition,
            Element extensionsElement,
            boolean isStarteEvent) {
        Element propertiesElement = extensionsElement.elementNS(CAMUNDA_BPMN_EXTENSIONS_NS, "properties");
        if (propertiesElement == null) {
            return;
        }

        // property data elements
        String tenantId = deployment.getTenantId();
        String messageType = translateToMessageTypeName(messageEventDefinition);

        MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder =
                MessageTypeExtensionData.builder(processDefinition.getKey(), messageType);
        builder.setStartEvent(isStarteEvent);

        for (Element propertyElement : propertiesElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "property")) {
            if (attributeNameStartsWith(propertyElement, "name", this.extensionPrefix + ".")) {
                createMessageMapping(processDefinition, propertyElement, builder);
            }
        }
        mapper.add(tenantId, builder.build());
    }

    private boolean attributeNameStartsWith(Element element, String attribute, String startsWith) {
        String value = element.attribute(attribute);
        return value != null ? value.startsWith(startsWith) : false;
    }

    private void createMessageMapping(
            ProcessDefinitionEntity processDefinition,
            Element propertyElement,
            MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder) {
        String name = propertyElement.attribute("name");
        String value = propertyElement.attribute("value");

        if (name == null || value == null){
            addError("Name and value cannot be null, please correct " + processDefinition.getName() + " message definition", propertyElement);
        }

        String[] parts = name.split("[.]");
        if (parts.length < 3 || parts.length > 4) {
            addError("Unsupported number of parts in property name \"" + name + "\"", propertyElement);
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
                addError("unknown token \"" + token + "\"", propertyElement);
        }
    }

    private String translateToMessageTypeName(Element messageEventDefinition) {
        String messageRef = messageEventDefinition.attribute("messageRef");
        MessageDefinition messageDefinition = messages.get(resolveName(messageRef));
        return messageDefinition.getExpression().getExpressionText();
    }
}