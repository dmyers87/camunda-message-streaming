package com.ultimatesoftware.workflow.messaging.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ultimatesoftware.workflow.messaging.GenericMessage;

import java.util.UUID;

import static com.ultimatesoftware.workflow.messaging.Constants.ZERO_UUID;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_VALUE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_NESTED_VARIABLE_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_NESTED_VARIABLE_VALUE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TENANT_ID;

public class GenericMessageBuilder {

    private final String id = UUID.randomUUID().toString();
    private final String schemaVersion = "1.0";
    private String tenantId = GENERIC_TENANT_ID;
    private final String messageType = GENERIC_MESSAGE_TYPE;
    private ObjectNode body = new ObjectMapper().createObjectNode()
        .put(GENERIC_BUSINESS_PROCESS_KEY_FIELD, GENERIC_BUSINESS_PROCESS_KEY_VALUE)
        .put("name", "name")
        .put(GENERIC_NESTED_VARIABLE_FIELD, GENERIC_NESTED_VARIABLE_VALUE)
        .put("nullKey", (Boolean) null);

    public GenericMessageBuilder withSystemTenant() {
        this.tenantId = ZERO_UUID;
        return this;
    }

    public GenericMessage build() {
        return new GenericMessage(id, schemaVersion, tenantId, messageType, body);
    }
}
