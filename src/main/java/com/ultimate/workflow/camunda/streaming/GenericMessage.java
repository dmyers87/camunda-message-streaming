package com.ultimate.workflow.camunda.streaming;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;

import static com.ultimate.workflow.camunda.Constants.ZERO_UUID;

public class GenericMessage {
    @JsonProperty("id")
    private String id;

    @JsonProperty("schemaVersion")
    private String schemaVersion;

    @JsonProperty(value = "tenantId", defaultValue = ZERO_UUID)
    private String tenantId;

    @JsonProperty("type")
    private String messageType;

    @JsonProperty("body")
    @JsonRawValue
    private JsonNode body;

    public String getId() {
        return id;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMessageType() {
        return messageType;
    }

    public JsonNode getBody() {
        return body;
    }
}
