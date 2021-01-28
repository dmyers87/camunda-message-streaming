package com.ultimatesoftware.workflow.messaging.correlation;

import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TENANT_ID;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TOPIC_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.builders.GenericMessageBuilder;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(record.topic()).thenReturn(GENERIC_TOPIC_NAME);
        when(record.value()).thenReturn(objectMapper.writeValueAsString(genericMessage));

        MessageTypeExtensionData data = MessageTypeExtensionData
            .builder("processDefinitionKey", GENERIC_MESSAGE_TYPE)
            .withBusinessKeyExpression("$.checkNumber")
            .withTopic(GENERIC_TOPIC_NAME)
            .build();

        Iterable<MessageTypeExtensionData> messageTypeExtensionDataList = Arrays.asList(data);
        when(messageTypeMapper.find(GENERIC_TOPIC_NAME, GENERIC_TENANT_ID, GENERIC_MESSAGE_TYPE))
            .thenReturn(messageTypeExtensionDataList);

        correlatingMessageListener.onMessage(record);

        verify(genericMessageCorrelator).correlate(genericMessage, messageTypeExtensionDataList);
        verifyNoMoreInteractions(genericMessageCorrelator);
    }

    @Test
    public void whenMessageReceived_AndCannotDeserialize_shouldThrowException() {
        ConsumerRecord<String, String> record = mock(ConsumerRecord.class);

        when(record.topic()).thenReturn(GENERIC_TOPIC_NAME);
        when(record.value()).thenReturn("bad string");


        assertThrows(RuntimeException.class, () -> correlatingMessageListener.onMessage(record));
        verifyNoInteractions(genericMessageCorrelator);
    }
}
