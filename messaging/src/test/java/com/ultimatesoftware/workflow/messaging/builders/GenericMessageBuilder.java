package com.ultimatesoftware.workflow.messaging.builders;

import static com.ultimatesoftware.workflow.messaging.Constants.ZERO_UUID;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_FIELD;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_BUSINESS_PROCESS_KEY_VALUE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TENANT_ID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultimatesoftware.workflow.messaging.GenericMessage;
import java.util.UUID;

public class GenericMessageBuilder {

    private final String id = UUID.randomUUID().toString();
    private final String schemaVersion = "1.0";
    private String tenantId = GENERIC_TENANT_ID;
    private final String messageType = GENERIC_MESSAGE_TYPE;
    private JsonNode body = new ObjectMapper().createObjectNode()
        .put(GENERIC_BUSINESS_PROCESS_KEY_FIELD, GENERIC_BUSINESS_PROCESS_KEY_VALUE)
        .put("name", "name");

    public GenericMessageBuilder withSystemTenant() {
        this.tenantId = ZERO_UUID;
        return this;
    }

    public GenericMessageBuilder withBody(JsonNode body) {
        this.body = body;
        return this;
    }

    public GenericMessage build() {
        return new GenericMessage(id, schemaVersion, tenantId, messageType, body);
    }
}
