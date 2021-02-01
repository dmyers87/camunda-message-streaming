package com.ultimatesoftware.workflow.messaging.correlation;

import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_VALUE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.PROCESS_INSTANCE_ID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.builders.GenericMessageBuilder;
import com.ultimatesoftware.workflow.messaging.builders.MessageTypeExtensionDataBuilder;
import java.util.Collections;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.extension.mockito.QueryMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GenericMessageCorrelatorTest {

    @Mock
    RuntimeService runtimeService;

    private MessageCorrelationBuilder messageCorrelationBuilder;

    private GenericMessageCorrelator genericMessageCorrelator;

    @BeforeEach
    public void setup() {
        genericMessageCorrelator = new GenericMessageCorrelator(runtimeService);
    }

    @Test
    public void whenCorrelateCalledForStartEvent_shouldCallRuntimeServiceToCorrelate() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .isStartEvent()
            .build();

        messageCorrelationBuilder = mockStartMessageCorrelationBuilder();
        when(messageCorrelationBuilder.tenantId(anyString())).thenReturn(messageCorrelationBuilder);

        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(runtimeService).createMessageCorrelation(GENERIC_MESSAGE_TYPE);
        verifyNoMoreInteractions(runtimeService);

        verify(messageCorrelationBuilder).processInstanceBusinessKey(GENERIC_BUSINESS_PROCESS_KEY_VALUE);
        verify(messageCorrelationBuilder).tenantId(genericMessage.getTenantId());
        verify(messageCorrelationBuilder).setVariable("name", "name");
        verify(messageCorrelationBuilder).startMessageOnly();
        verify(messageCorrelationBuilder).correlateWithResult();
        verifyNoMoreInteractions(messageCorrelationBuilder);
    }

    @Test
    public void whenCorrelateCalledForStartEvent_withZeroTenantId_shouldCallRuntimeServiceToCorrelate() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .withZeroTenantId()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .isStartEvent()
            .build();

        messageCorrelationBuilder = mockStartMessageCorrelationBuilder();
        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(runtimeService).createMessageCorrelation(GENERIC_MESSAGE_TYPE);
        verifyNoMoreInteractions(runtimeService);

        verify(messageCorrelationBuilder).processInstanceBusinessKey(GENERIC_BUSINESS_PROCESS_KEY_VALUE);
        verify(messageCorrelationBuilder).setVariable("name", "name");
        verify(messageCorrelationBuilder).startMessageOnly();
        verify(messageCorrelationBuilder).correlateWithResult();
        verifyNoMoreInteractions(messageCorrelationBuilder);
    }

    @Test
    public void whenCorrelateCalledForCatchEvent_shouldCallRuntimeServiceToCorrelate() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .build();

        Execution execution = mock(Execution.class);
        when(execution.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        QueryMocks.mockExecutionQuery(runtimeService).list(Collections.singletonList(execution));

        messageCorrelationBuilder = mockCatchMessageCorrelationBuilder();
        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(runtimeService).createExecutionQuery();
        verify(runtimeService).createMessageCorrelation(GENERIC_MESSAGE_TYPE);

        verify(messageCorrelationBuilder).processInstanceId(PROCESS_INSTANCE_ID);
        verify(messageCorrelationBuilder).setVariable("name", "name");
        verify(messageCorrelationBuilder).correlateWithResult();
        verifyNoMoreInteractions(messageCorrelationBuilder);
    }

    @Test
    public void whenCorrelateCalledForCatchEvent_withZeroTenantId_shouldCallRuntimeServiceToCorrelate() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .withZeroTenantId()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .build();

        Execution execution = mock(Execution.class);
        when(execution.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        QueryMocks.mockExecutionQuery(runtimeService).list(Collections.singletonList(execution));

        messageCorrelationBuilder = mockCatchMessageCorrelationBuilder();
        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(runtimeService).createExecutionQuery();
        verify(runtimeService).createMessageCorrelation(GENERIC_MESSAGE_TYPE);

        verify(messageCorrelationBuilder).processInstanceId(PROCESS_INSTANCE_ID);
        verify(messageCorrelationBuilder).setVariable("name", "name");
        verify(messageCorrelationBuilder).correlateWithResult();
        verifyNoMoreInteractions(messageCorrelationBuilder);
    }

    @Test
    public void whenCorrelateCalledForCatchEvent_AndNoResultsFound_shouldDoNothing() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .build();

        QueryMocks.mockExecutionQuery(runtimeService).list(Collections.emptyList());

        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(runtimeService).createExecutionQuery();
        verifyNoMoreInteractions(runtimeService);
    }

    private MessageCorrelationBuilder mockMessageCorrelationBuilder() {
        MessageCorrelationBuilder messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(anyString())).thenReturn(messageCorrelationBuilder);

        return messageCorrelationBuilder;
    }

    private MessageCorrelationBuilder mockStartMessageCorrelationBuilder() {
        MessageCorrelationBuilder messageCorrelationBuilder = mockMessageCorrelationBuilder();

        when(messageCorrelationBuilder.processInstanceBusinessKey(anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(anyString(), anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.startMessageOnly()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(mock(MessageCorrelationResult.class));

        return messageCorrelationBuilder;
    }

    private MessageCorrelationBuilder mockCatchMessageCorrelationBuilder() {
        MessageCorrelationBuilder messageCorrelationBuilder = mockMessageCorrelationBuilder();

        when(messageCorrelationBuilder.processInstanceId(anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(anyString(), anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(mock(MessageCorrelationResult.class));

        return messageCorrelationBuilder;
    }
}
