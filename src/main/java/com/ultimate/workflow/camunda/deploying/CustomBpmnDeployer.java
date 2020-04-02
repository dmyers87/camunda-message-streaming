package com.ultimate.workflow.camunda.deploying;

import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

public class CustomBpmnDeployer extends BpmnDeployer {

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
