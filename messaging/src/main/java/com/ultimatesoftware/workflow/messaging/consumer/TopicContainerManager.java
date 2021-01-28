package com.ultimatesoftware.workflow.messaging.consumer;

import org.springframework.context.Lifecycle;

public interface TopicContainerManager {

    void createOrStartConsumers(Iterable<String> topics, Object listener);

    void createOrStartConsumer(String topic, Object listener);

    void stopConsumers(Iterable<String> topics);

    void stopConsumer(String topic);

    Lifecycle getConsumer(String topic);
}
