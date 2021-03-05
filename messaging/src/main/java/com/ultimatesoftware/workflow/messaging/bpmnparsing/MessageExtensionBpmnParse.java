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
    private final TopicValueEvaluator topicValueEvaluator;

    public MessageExtensionBpmnParse(BpmnParser bpmnParser,
                                     MessageTypeMapper mapper,
                                     String extensionPrefix,
                                     TopicValueEvaluator topicValueEvaluator) {
        super(bpmnParser);
        this.mapper = mapper;
        this.extensionPrefix = extensionPrefix;
        this.topicValueEvaluator = topicValueEvaluator;
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

    @Override
    public ActivityImpl parseReceiveTask(Element receiveTaskElement, ScopeImpl scopeElement) {
        ActivityImpl result = super.parseReceiveTask(receiveTaskElement, scopeElement);

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scopeElement.getProcessDefinition();

        createMessageTypeExtensionData(processDefinition, receiveTaskElement, receiveTaskElement, false);
        return result;
    }

    private Element getMessageEventDefinitionElement(Element parseElement) {
        Element messageEventDefinitionElement = parseElement.element(MESSAGE_EVENT_DEFINITION);

        if (isElementNotMessageEvent(messageEventDefinitionElement)) {
            return null;
        }

        return messageEventDefinitionElement;
    }

    private void createMessageTypeExtensionData(ProcessDefinitionEntity processDefinition,
                                                Element parseElement,
                                                boolean isStartEvent) {

        Element messageEventDefinitionElement = getMessageEventDefinitionElement(parseElement);

        if (messageEventDefinitionElement != null) {
            createMessageTypeExtensionData(processDefinition, parseElement, messageEventDefinitionElement, isStartEvent);
        }
    }

    private void createMessageTypeExtensionData(ProcessDefinitionEntity processDefinition,
                                                Element parseElement,
                                                Element messageEventDefinition,
                                                boolean isStartEvent) {

        Element propertiesElement = extractPropertiesElement(parseElement);

        if (propertiesElement == null) {
            return;
        }

        String tenantId = deployment.getTenantId();
        String messageType = getMessageTypeFromElement(messageEventDefinition);

        MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder =
                MessageTypeExtensionData.builder(processDefinition.getKey(), messageType);
        builder.setStartEvent(isStartEvent);

        addMessageMappingsToBuilder(processDefinition, propertiesElement, builder);
        mapper.add(tenantId, builder.build());
    }

    private void addMessageMappingsToBuilder(ProcessDefinitionEntity processDefinition, Element propertiesElement,
                                             MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder) {

        for (Element propertyElement : propertiesElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "property")) {
            if (attributeNameStartsWith(propertyElement, "name", this.extensionPrefix + ".")) {
                try {
                    builder.addFieldFromPropertyElement(processDefinition, propertyElement, topicValueEvaluator);
                } catch (ExtensionElementNotParsableException ex) {
                    addError(ex.getMessage(), propertyElement);
                }
            }
        }
    }

    private Element extractPropertiesElement(Element parentElement) {
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