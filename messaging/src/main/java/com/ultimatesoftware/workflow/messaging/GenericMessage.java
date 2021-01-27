package com.ultimatesoftware.workflow.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;

import static com.ultimatesoftware.workflow.messaging.Constants.ZERO_UUID;

import java.util.Objects;

public class GenericMessage {
    @JsonProperty("id")
    private final String id;

    @JsonProperty("schemaVersion")
    private final String schemaVersion;

    @JsonProperty(value = "tenantId", defaultValue = ZERO_UUID)
    private final String tenantId;

    @JsonProperty("type")
    private final String messageType;

    @JsonRawValue
    private final JsonNode body;

    @JsonCreator
    public GenericMessage(@JsonProperty("id") String id,
                          @JsonProperty("schemaVersion") String schemaVersion,
                          @JsonProperty(value = "tenantId", defaultValue = ZERO_UUID) String tenantId,
                          @JsonProperty("type") String messageType,
                          @JsonProperty("body") JsonNode body) {
        this.id = id;
        this.schemaVersion = schemaVersion;
        this.tenantId = tenantId;
        this.messageType = messageType;
        this.body = body;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GenericMessage)) {
            return false;
        }
        GenericMessage that = (GenericMessage) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(schemaVersion, that.schemaVersion) &&
            Objects.equals(tenantId, that.tenantId) &&
            Objects.equals(messageType, that.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schemaVersion, tenantId, messageType, body);
    }

    @Override
    public String toString() {
        return "GenericMessage{" +
            "id='" + id + '\'' +
            ", schemaVersion='" + schemaVersion + '\'' +
            ", tenantId='" + tenantId + '\'' +
            ", messageType='" + messageType + '\'' +
            ", body=" + body +
            '}';
    }
}
