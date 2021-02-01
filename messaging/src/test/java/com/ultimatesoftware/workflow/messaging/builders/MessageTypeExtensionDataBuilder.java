package com.ultimatesoftware.workflow.messaging.builders;

import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TOPIC_NAME;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;

public class MessageTypeExtensionDataBuilder {

    private boolean isStartEvent = false;

    public MessageTypeExtensionDataBuilder isStartEvent() {
        this.isStartEvent = true;
        return this;
    }

    public MessageTypeExtensionData build() {
        return MessageTypeExtensionData
            .builder("processDefinitionKey", GENERIC_MESSAGE_TYPE)
            .withBusinessKeyExpression("$." + GENERIC_BUSINESS_PROCESS_KEY_FIELD)
            .withTopic(GENERIC_TOPIC_NAME)
            .withInputVariable("name", "name")
            .setStartEvent(isStartEvent)
            .build();
    }
}
