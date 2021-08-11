package com.ultimatesoftware.workflow.messaging.builders;

import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_NESTED_VARIABLE_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TOPIC_NAME;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import java.util.HashMap;
import java.util.Map;

public class MessageTypeExtensionDataBuilder {

    private boolean isStartEvent = false;
    private final Map<String, String> inputVariables =
        new HashMap<String, String>() {{
            put("name", "$.name");
            put("constant", "constant");
            put(GENERIC_NESTED_VARIABLE_FIELD, "$." + GENERIC_NESTED_VARIABLE_FIELD);
    }};

    public MessageTypeExtensionDataBuilder isStartEvent() {
        this.isStartEvent = true;
        return this;
    }

    public MessageTypeExtensionDataBuilder withInputVariable(String key, String value) {
        inputVariables.put(key, value);
        return this;
    }

    public MessageTypeExtensionData build() {
        MessageTypeExtensionData.MessageTypeExtensionDataBuilder builder = MessageTypeExtensionData
            .builder("processDefinitionKey", GENERIC_MESSAGE_TYPE)
            .withBusinessKeyExpression("$." + GENERIC_BUSINESS_PROCESS_KEY_FIELD)
            .withTopic(GENERIC_TOPIC_NAME);

        inputVariables.forEach(builder::withInputVariable);

        return builder
            .setStartEvent(isStartEvent)
            .build();
    }
}
