package com.ultimate.workflow.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;

public class GenericMessage {
    private static final String ZERO_UUID = "";

    @JsonProperty("id")
    private String id;

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
