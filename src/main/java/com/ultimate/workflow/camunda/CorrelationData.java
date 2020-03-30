package com.ultimate.workflow.camunda;

import java.util.Map;

public class CorrelationData {
    private String tenantId;
    private String businessKey;
    private Map<String, Object> inputVaribles;

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Map<String, Object> getInputVaribles() {
        return inputVaribles;
    }

    public void setInputVaribles(Map<String, Object> inputVaribles) {
        this.inputVaribles = inputVaribles;
    }
}
