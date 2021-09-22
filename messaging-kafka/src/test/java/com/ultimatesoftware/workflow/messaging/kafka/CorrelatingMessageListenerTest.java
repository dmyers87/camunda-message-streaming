package com.ultimatesoftware.workflow.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.correlation.GenericMessageCorrelator;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CorrelatingMessageListenerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GenericMessageCorrelator genericMessageCorrelator;

    @Mock
    private MessageTypeMapper messageTypeMapper;

    private CorrelatingMessageListener correlatingMessageListener;

    @BeforeEach
    public void setup() {
        correlatingMessageListener = new CorrelatingMessageListener(
            genericMessageCorrelator, messageTypeMapper);
    }

    @Test
    public void whenMessageReceived_shouldCallCorrelator() throws JsonProcessingException {
        ConsumerRecord<String, String> record = mock(ConsumerRecord.class);
        GenericMessage genericMessage = new GenericMessageBuilder().build();

        when(record.topic()).thenReturn(TestConstants.GENERIC_TOPIC_NAME);
        when(record.value()).thenReturn(objectMapper.writeValueAsString(genericMessage));

        MessageTypeExtensionData data = MessageTypeExtensionData
            .builder("deploymentId", "processDefinitionKey", TestConstants.GENERIC_MESSAGE_TYPE)
            .withActivityId("activityId")
            .withBusinessKeyExpression("$.checkNumber")
            .withTopic(TestConstants.GENERIC_TOPIC_NAME)
            .build();

        Iterable<MessageTypeExtensionData> messageTypeExtensionDataList = Arrays.asList(data);
        when(messageTypeMapper.find(TestConstants.GENERIC_TOPIC_NAME, TestConstants.GENERIC_TENANT_ID, TestConstants.GENERIC_MESSAGE_TYPE))
            .thenReturn(messageTypeExtensionDataList);

        correlatingMessageListener.onMessage(record);

        verify(genericMessageCorrelator).correlate(genericMessage, messageTypeExtensionDataList);
        verifyNoMoreInteractions(genericMessageCorrelator);
    }

    @Test
    public void whenMessageReceived_AndCannotDeserialize_shouldThrowException() {
        ConsumerRecord<String, String> record = mock(ConsumerRecord.class);

        when(record.topic()).thenReturn(TestConstants.GENERIC_TOPIC_NAME);
        when(record.value()).thenReturn("bad string");


        assertThrows(RuntimeException.class, () -> correlatingMessageListener.onMessage(record));
        verifyNoInteractions(genericMessageCorrelator);
    }
}
