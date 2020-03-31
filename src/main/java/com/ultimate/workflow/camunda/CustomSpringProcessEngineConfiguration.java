package com.ultimate.workflow.camunda;

import com.ultimate.workflow.camunda.streaming.MessageTypeExtensionData;
import com.ultimate.workflow.camunda.streaming.MessageTypeMapper;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.bpmn.parser.MessageDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;

public class CustomSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    private MessageTypeMapper mapper;

    public CustomSpringProcessEngineConfiguration(MessageTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BpmnDeployer getBpmnDeployer() {
        // Copied from org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
        BpmnDeployer bpmnDeployer = new CustomBpmnDeployer();
        bpmnDeployer.setExpressionManager(expressionManager);
        bpmnDeployer.setIdGenerator(idGenerator);

        if (bpmnParseFactory == null) {
            // Replace parse factory with custom parse factory
            bpmnParseFactory = new CustomBpmnParseFactory();
        }

        BpmnParser bpmnParser = new BpmnParser(expressionManager, bpmnParseFactory);

        if (preParseListeners != null) {
            bpmnParser.getParseListeners().addAll(preParseListeners);
        }
        bpmnParser.getParseListeners().addAll(getDefaultBPMNParseListeners());
        if (postParseListeners != null) {
            bpmnParser.getParseListeners().addAll(postParseListeners);
        }

        bpmnDeployer.setBpmnParser(bpmnParser);

        return bpmnDeployer;
    }

    private class CustomBpmnDeployer extends BpmnDeployer {
        public CustomBpmnDeployer() {
        }

        @Override
        protected void addMessageStartEventSubscription(EventSubscriptionDeclaration messageEventDefinition, ProcessDefinitionEntity processDefinition) {
            super.addMessageStartEventSubscription(messageEventDefinition, processDefinition);
//
//            List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionManager()
//                    .findEventSubscriptionsByNameAndTenantId(EventType.MESSAGE.name(), messageEventDefinition.getUnresolvedEventName(), processDefinition.getTenantId());
//
//            for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
//                eventSubscription.
//            }
        }

        @Override
        protected void removeObsoleteEventSubscriptions(ProcessDefinitionEntity processDefinition, ProcessDefinitionEntity latestProcessDefinition) {
            super.removeObsoleteEventSubscriptions(processDefinition, latestProcessDefinition);
        }
    }

    private class CustomBpmnParseFactory implements org.camunda.bpm.engine.impl.cfg.BpmnParseFactory {
        @Override
        public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
            return new CustomBpmnParse(bpmnParser);
        }
    }

    private class CustomBpmnParse extends BpmnParse {
        public CustomBpmnParse(BpmnParser bpmnParser) {
            super(bpmnParser);
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
            MessageTypeExtensionData messageTypeExtensionData = new MessageTypeExtensionData(messageType);
            for (Element propertyElement : propertiesElement.elementsNS(CAMUNDA_BPMN_EXTENSIONS_NS, "property")) {
                if (attributeNameStartsWith(propertyElement, "name", "ultimate.workflow.")) {
                    createMessageMapping(processDefinition, messageEventDefinition, propertyElement, messageTypeExtensionData);
                }
            }
            mapper.add(tenantId, messageTypeExtensionData);
        }

        private boolean attributeNameStartsWith(Element element, String attribute, String startsWith) {
            String value = element.attribute(attribute);
            return value != null ? value.startsWith(startsWith) : false;
        }

        private void createMessageMapping(ProcessDefinitionEntity processDefinition, Element messageEventDefinition, Element propertyElement, MessageTypeExtensionData messageTypeExtensionData) {
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
                    messageTypeExtensionData.setBusinessKeyExpression(value);
                    break;
                case "input-var":
                    // input variable mappings
                    String variableName = parts[3];
                    messageTypeExtensionData.getVariables().put(variableName, value);
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
}
