package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.exceptions.ExtensionElementNotParsableException;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
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
        createMessageTypeExtensionData(processDefinition, startEventElement, true);
    }

    @Override
    public ActivityImpl parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scopeElement, ActivityImpl eventBasedGateway) {
        ActivityImpl result = super.parseIntermediateCatchEvent(intermediateEventElement, scopeElement, eventBasedGateway);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scopeElement.getProcessDefinition();
        createMessageTypeExtensionData(processDefinition, intermediateEventElement, false);

        return result;
    }

    /**
     * Since this no singular method for boundary events, we need to loop through al
     * of the boundary events.
     *
     * @param parentElement
     * @param flowScope
     */
    @Override
    public void parseBoundaryEvents(Element parentElement, ScopeImpl flowScope) {
        super.parseBoundaryEvents(parentElement, flowScope);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) flowScope.getProcessDefinition();

        for (Element boundaryEventElement : parentElement.elements("boundaryEvent")) {
            createMessageTypeExtensionData(processDefinition, boundaryEventElement, false);
        }
    }

    private void createMessageTypeExtensionData(ProcessDefinitionEntity processDefinition,
                                                Element parseElement,
                                                boolean isStartedEvent) {
        Element messageEventDefinitionElement = parseElement.element(MESSAGE_EVENT_DEFINITION);
        Element propertiesElement = extractPropertiesElement(messageEventDefinitionElement, parseElement);

        if (propertiesElement != null) {
            createMessageTypeExtensionData(processDefinition, messageEventDefinitionElement, propertiesElement, isStartedEvent);
        }
    }

    private void createMessageTypeExtensionData(ProcessDefinitionEntity processDefinition,
                                                Element messageEventDefinition,
                                                Element propertiesElement, boolean isStartedEvent) {
        String tenantId = deployment.getTenantId();
        String messageType = getMessageTypeFromElement(messageEventDefinition);

        MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder =
                MessageTypeExtensionData.builder(processDefinition.getKey(), messageType);
        builder.setStartEvent(isStartedEvent);

        addMessageMappingsToBuilder(processDefinition, propertiesElement, builder);
        mapper.add(tenantId, builder.build());
    }

    private void addMessageMappingsToBuilder(ProcessDefinitionEntity processDefinition, Element propertiesElement,
                                             MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder) {

        for (Element propertyElement : propertiesElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "property")) {
            if (attributeNameStartsWith(propertyElement, "name", this.extensionPrefix + ".")) {
                try {
                    builder.addFieldFromPropertyElement(processDefinition, propertyElement);
                } catch (ExtensionElementNotParsableException ex) {
                    addError(ex.getMessage(), propertyElement);
                }
            }
        }
    }

    private Element extractPropertiesElement(Element messageEventDefinitionElement, Element parentElement) {
        if (isElementNotMessageEvent(messageEventDefinitionElement)) {
            return null;
        }

        Element extensionsElement = parentElement.element("extensionElements");
        if (extensionsElement == null) {
            return null;
        }

        return extensionsElement.elementNS(CAMUNDA_BPMN_EXTENSIONS_NS, "properties");
    }

    private boolean isElementNotMessageEvent(Element messageEventDefinitionElement) {
        return messageEventDefinitionElement == null;
    }

    private boolean attributeNameStartsWith(Element element, String attribute, String startsWith) {
        String value = element.attribute(attribute);
        return value != null && value.startsWith(startsWith);
    }

    private String getMessageTypeFromElement(Element messageEventDefinition) {
        String messageRef = messageEventDefinition.attribute("messageRef");
        MessageDefinition messageDefinition = messages.get(resolveName(messageRef));
        return messageDefinition.getExpression().getExpressionText();
    }
}