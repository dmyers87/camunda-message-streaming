package com.ultimatesoftware.workflow.messaging.bpmnparsing;

public interface MetadataValueEvaluator <T> {
    T evaluate (String value);
    T evaluate (String key, String value);
}
