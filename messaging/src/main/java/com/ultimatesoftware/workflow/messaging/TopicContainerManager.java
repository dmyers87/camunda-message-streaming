package com.ultimatesoftware.workflow.messaging;

import java.util.Map;

public interface TopicContainerManager {

    void createOrStartConsumer(String topic, Object messageListener, Map<String, Object> consumerProperties);

    void stopConsumer(String topic);
}
