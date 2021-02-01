package com.ultimatesoftware.workflow.messaging.correlation;

import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

public class GenericMessageCorrelator {

    private final RuntimeService runtimeService;

    public GenericMessageCorrelator(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public List<MessageCorrelationResult> correlate(GenericMessage genericMessage, Iterable<MessageTypeExtensionData> messageTypeExtensionDataList) {
        List<MessageCorrelationResult> results = new ArrayList<>();
        for (MessageTypeExtensionData messageTypeExtensionData : messageTypeExtensionDataList) {
            CorrelationData correlationData = CorrelationData.buildCorrelationData(genericMessage, messageTypeExtensionData);

            results.addAll(executeCorrelation(correlationData));
        }

        return results;
    }

    private List<MessageCorrelationResult> executeCorrelation(CorrelationData correlationData) {
        if (correlationData.isStartEvent()) {
            return Collections.singletonList(executeStartMessageEventCorrelation(correlationData));
        } else {
            return executeCatchMessageEventCorrelation(correlationData);
        }
    }

    private MessageCorrelationResult executeStartMessageEventCorrelation(CorrelationData correlationData) {
        GenericMessageCorrelationBuilder messageCorrelationBuilder =
            GenericMessageCorrelationBuilder.builder(runtimeService, correlationData.getMessageType())
                .withBusinessKey(correlationData.getBusinessKey());

        if (correlationData.hasNonZeroTenantId()) {
            messageCorrelationBuilder.withTenantId(correlationData.getTenantId());
        }

        return messageCorrelationBuilder.withVariables(correlationData.getInputVariables())
            .correlateStartMessageOnly();
    }

    private List<MessageCorrelationResult> executeCatchMessageEventCorrelation(CorrelationData correlationData) {
        return determineCorrelatableProcessesInstances(correlationData)
            .map((execution -> executeCatchMessageEventCorrelationByExecution(correlationData, execution)))
            .collect(Collectors.toList());
    }

    private Stream<Execution> determineCorrelatableProcessesInstances(CorrelationData correlationData) {
        ExecutionQuery executionQuery = this.runtimeService
                .createExecutionQuery()
                .messageEventSubscriptionName(correlationData.getMessageType())
                .processInstanceBusinessKey(correlationData.getBusinessKey());

        if (correlationData.hasNonZeroTenantId()) {
            executionQuery.tenantIdIn(correlationData.getTenantId());
        }

        // search using match variables
        correlationData.getMatchVariables().forEach(executionQuery::processVariableValueEquals);

        return executionQuery.list().stream();
    }

    private MessageCorrelationResult executeCatchMessageEventCorrelationByExecution(CorrelationData correlationData, Execution execution) {
       return GenericMessageCorrelationBuilder.builder(runtimeService, correlationData.getMessageType())
           .withProcessInstanceId(execution.getProcessInstanceId())
           .withVariables(correlationData.getInputVariables())
           .correlate();
    }

}
