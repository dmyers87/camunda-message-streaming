package com.ultimatesoftware.workflow.messaging.consumer;

import java.util.Map;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

public interface TopicContainerManager {

    void createOrStartConsumers(Iterable<String> topics, Object listener);

    void createOrStartConsumer(String topic, Object listener);

    void stopConsumers(Iterable<String> topics);

    void stopConsumer(String topic);

    Map<String, ConcurrentMessageListenerContainer<String, String>> getConsumers();

    ConcurrentMessageListenerContainer<String, String> getConsumer(String topic);
}
