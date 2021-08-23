package com.ultimatesoftware.workflow.messaging.correlation;

import com.jayway.jsonpath.PathNotFoundException;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.builders.GenericMessageBuilder;
import com.ultimatesoftware.workflow.messaging.builders.MessageTypeExtensionDataBuilder;
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

import java.util.Collections;

import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_VALUE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_NESTED_VARIABLE_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_NESTED_VARIABLE_VALUE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.PROCESS_INSTANCE_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

        verifyProcessInstanceBusinessKey();

        verify(messageCorrelationBuilder).tenantId(genericMessage.getTenantId());
        verify(messageCorrelationBuilder).startMessageOnly();

        verifyMessageCorrelationBuilder();

    }

    @Test
    public void whenCorrelateNestedJson_shouldCorrelateSuccessfully() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .isStartEvent()
            .build();
        messageCorrelationBuilder = mockStartMessageCorrelationBuilder();
        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(messageCorrelationBuilder).setVariable(GENERIC_NESTED_VARIABLE_FIELD,
            GENERIC_NESTED_VARIABLE_VALUE);
        verify(messageCorrelationBuilder).correlateWithResult();
    }

    @Test
    public void whenCorrelateNullKey_shouldCorrelateSuccessfully() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .isStartEvent()
            .withInputVariable("nullKey", "$.nullKey")
            .build();
        messageCorrelationBuilder = mockStartMessageCorrelationBuilder();
        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(messageCorrelationBuilder).setVariable("nullKey", null);
        verify(messageCorrelationBuilder).correlateWithResult();
    }

    @Test
    public void whenCorrelateWithMissingField_shouldNotCorrelateSuccessfully() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .isStartEvent()
            .withInputVariable("missingField", "$.missingField")
            .build();
        assertThrows(PathNotFoundException.class, () -> genericMessageCorrelator.correlate(genericMessage,
            Collections.singletonList(data)));
    }

    @Test
    public void whenCorrelateCalledForStartEvent_withZeroTenantId_shouldCallRuntimeServiceToCorrelate() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .withSystemTenant()
            .build();

        MessageTypeExtensionData data = new MessageTypeExtensionDataBuilder()
            .isStartEvent()
            .build();

        messageCorrelationBuilder = mockStartMessageCorrelationBuilder();
        genericMessageCorrelator.correlate(genericMessage, Collections.singletonList(data));

        verify(runtimeService).createMessageCorrelation(GENERIC_MESSAGE_TYPE);
        verifyNoMoreInteractions(runtimeService);

        verifyProcessInstanceBusinessKey();
        verify(messageCorrelationBuilder).startMessageOnly();

        verifyMessageCorrelationBuilder();
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

        verifyMessageCorrelationBuilder();
    }

    @Test
    public void whenCorrelateCalledForCatchEvent_withZeroTenantId_shouldCallRuntimeServiceToCorrelate() {
        GenericMessage genericMessage = new GenericMessageBuilder()
            .withSystemTenant()
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

        verifyMessageCorrelationBuilder();
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

    private void verifyProcessInstanceBusinessKey() {
        verify(messageCorrelationBuilder).processInstanceBusinessKey(GENERIC_BUSINESS_PROCESS_KEY_VALUE);
    }

    private void verifyMessageCorrelationBuilder() {
        verify(messageCorrelationBuilder).setVariable("name", "name");
        verify(messageCorrelationBuilder).setVariable("constant", "constant");
        verify(messageCorrelationBuilder).setVariable("nullField", null);
        verify(messageCorrelationBuilder).setVariable(GENERIC_NESTED_VARIABLE_FIELD, GENERIC_NESTED_VARIABLE_VALUE);
        verify(messageCorrelationBuilder).correlateWithResult();
        verifyNoMoreInteractions(messageCorrelationBuilder);
    }

    private MessageCorrelationBuilder mockMessageCorrelationBuilder() {
        MessageCorrelationBuilder messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(anyString())).thenReturn(messageCorrelationBuilder);

        return messageCorrelationBuilder;
    }

    private MessageCorrelationBuilder mockStartMessageCorrelationBuilder() {
        MessageCorrelationBuilder messageCorrelationBuilder = mockMessageCorrelationBuilder();

        when(messageCorrelationBuilder.processInstanceBusinessKey(anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(anyString(), any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.startMessageOnly()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(mock(MessageCorrelationResult.class));

        return messageCorrelationBuilder;
    }

    private MessageCorrelationBuilder mockCatchMessageCorrelationBuilder() {
        MessageCorrelationBuilder messageCorrelationBuilder = mockMessageCorrelationBuilder();

        when(messageCorrelationBuilder.processInstanceId(anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(anyString(), any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(mock(MessageCorrelationResult.class));

        return messageCorrelationBuilder;
    }
}
