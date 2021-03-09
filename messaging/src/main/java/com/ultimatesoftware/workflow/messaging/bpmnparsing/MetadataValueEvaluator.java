package com.ultimatesoftware.workflow.messaging.bpmnparsing;

public interface MetadataValueEvaluator  {
    <T> T evaluate(String key, String value, Class<T> clazz);
}
