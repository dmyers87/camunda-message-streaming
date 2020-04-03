package com.ultimate.workflow.camunda.streaming;

import java.util.Map;

public interface TopicContainerManager {

    void createOrStartConsumer(String topic, Object messageListener, Map<String, Object> consumerProperties);

    void stopConsumer(String topic);
}
