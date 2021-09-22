package com.ultimatesoftware.workflow.messaging.correlation;

import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.TenantUtils;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericMessageCorrelator {

    private final Logger LOGGER = LoggerFactory.getLogger(GenericMessageCorrelator.class.getName());

    private final RuntimeService runtimeService;

    public GenericMessageCorrelator(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public List<MessageCorrelationResult> correlate(GenericMessage genericMessage, Iterable<MessageTypeExtensionData> messageTypeExtensionDataIterable) {
        List<MessageTypeExtensionData> messageTypeExtensionDataList =
            StreamSupport.stream(messageTypeExtensionDataIterable.spliterator(), false)
                .collect(Collectors.toList());

        return Stream.concat(
            correlateStartMessages(genericMessage, messageTypeExtensionDataList).stream(),
            correlateCatchMessages(genericMessage, messageTypeExtensionDataList).stream())
            .collect(Collectors.toList());
    }

    private List<MessageCorrelationResult> correlateStartMessages(GenericMessage genericMessage,
                                                                  List<MessageTypeExtensionData> messageTypeExtensionDataList) {
        return messageTypeExtensionDataList.stream()
            .filter(MessageTypeExtensionData::isStartEvent)
            .map(MessageTypeExtensionData::getProcessDefinitionKey)
            .distinct()
            .map(processDefinitionKey -> correlateLatestStartMessage(processDefinitionKey, genericMessage, messageTypeExtensionDataList))
            .collect(Collectors.toList());
    }

    private MessageCorrelationResult correlateLatestStartMessage(String processDefinitionKey,
                                                                 GenericMessage genericMessage,
                                                                 List<MessageTypeExtensionData> messageTypeExtensionDataStream) {

        MessageTypeExtensionData latestVersionExtensionData = getLatestVersionExtensionData(processDefinitionKey, messageTypeExtensionDataStream);
        CorrelationData correlationData = CorrelationDataUtils.buildCorrelationData(genericMessage, latestVersionExtensionData);
        return executeStartMessageEventCorrelation(correlationData);
    }

    private MessageTypeExtensionData getLatestVersionExtensionData(String processDefinitionKey,
                                                                   List<MessageTypeExtensionData> messageTypeExtensionDataList) {
        return messageTypeExtensionDataList.stream()
            .filter(messageTypeExtensionData -> messageTypeExtensionData.getProcessDefinitionKey().equals(processDefinitionKey))
            .max(Comparator.comparing(MessageTypeExtensionData::getVersion))
            .get();
    }

    private List<MessageCorrelationResult> correlateCatchMessages(GenericMessage genericMessage, List<MessageTypeExtensionData> messageTypeExtensionDataList) {
        List<MessageCorrelationResult> results = new ArrayList<>();

        messageTypeExtensionDataList.stream()
            .filter(MessageTypeExtensionData::isCatchEvent)
            .forEach(messageTypeExtensionData -> {
                CorrelationData correlationData = CorrelationDataUtils.buildCorrelationData(genericMessage, messageTypeExtensionData);
                results.addAll(executeCatchMessageEventCorrelation(correlationData));
        });

        return results;
    }

    private MessageCorrelationResult executeStartMessageEventCorrelation(CorrelationData correlationData) {
        MessageCorrelationBuilder messageCorrelationBuilder =
            runtimeService.createMessageCorrelation(correlationData.getMessageType())
                .processInstanceBusinessKey(correlationData.getBusinessKey());

        if (!TenantUtils.isSystemTenant(correlationData.getTenantId())) {
            messageCorrelationBuilder.tenantId(correlationData.getTenantId());
        }

        correlationData.getInputVariables()
            .forEach(messageCorrelationBuilder::setVariable);

        return correlate(messageCorrelationBuilder.startMessageOnly(), correlationData);
    }

    private List<MessageCorrelationResult> executeCatchMessageEventCorrelation(CorrelationData correlationData) {
        return determineCorrelatableProcessesInstances(correlationData)
            .map((execution -> executeCatchMessageEventCorrelationByExecution(correlationData, execution)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Stream<Execution> determineCorrelatableProcessesInstances(CorrelationData correlationData) {
        ExecutionQuery executionQuery = this.runtimeService
            .createExecutionQuery()
            .messageEventSubscriptionName(correlationData.getMessageType())
            .processInstanceBusinessKey(correlationData.getBusinessKey())
            .processDefinitionId(correlationData.getProcessDefinitionId())
            .activityId(correlationData.getActivityId());

        if (!TenantUtils.isSystemTenant(correlationData.getTenantId())) {
            executionQuery.tenantIdIn(correlationData.getTenantId());
        }

        correlationData.getMatchVariables().forEach(executionQuery::processVariableValueEquals);
        correlationData.getMatchLocalVariables().forEach(executionQuery::variableValueEquals);

        return executionQuery.list().stream();
    }

    private MessageCorrelationResult executeCatchMessageEventCorrelationByExecution(CorrelationData correlationData, Execution execution) {
        MessageCorrelationBuilder messageCorrelationBuilder =
            runtimeService.createMessageCorrelation(correlationData.getMessageType())
                .processInstanceId(execution.getProcessInstanceId())
                .processInstanceBusinessKey(correlationData.getBusinessKey());

        correlationData.getMatchVariables().forEach(messageCorrelationBuilder::processInstanceVariableEquals);
        correlationData.getMatchLocalVariables().forEach(messageCorrelationBuilder::localVariableEquals);
        correlationData.getInputVariables().forEach(messageCorrelationBuilder::setVariable);

        return correlate(messageCorrelationBuilder, correlationData);
    }

    public MessageCorrelationResult correlate(MessageCorrelationBuilder messageCorrelationBuilder, CorrelationData correlationData) {
        try {
            return messageCorrelationBuilder.correlateWithResult();
        } catch (MismatchingMessageCorrelationException ex) {
            LOGGER.warn("No correlation result was found with Message Type {} on Tenant {} for Correlation Data {}",
                correlationData.getMessageType(), correlationData.getTenantId(), correlationData);
            return null;
        } catch (Exception ex) {
            LOGGER.warn("Failed to correlate request", ex);
            throw ex;
        }
    }
}
