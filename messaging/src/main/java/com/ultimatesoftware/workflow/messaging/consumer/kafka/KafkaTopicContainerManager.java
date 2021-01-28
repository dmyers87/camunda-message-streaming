package com.ultimatesoftware.workflow.messaging.consumer.kafka;

import com.ultimatesoftware.workflow.messaging.consumer.TopicContainerManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.context.Lifecycle;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

/** Credit to Bikas Katwal
 * https://medium.com/@bikas.katwal10/start-stop-kafka-consumers-or-subscribe-to-new-topic-programmatically-using-spring-kafka-2d4fb77c9117
 * https://github.com/bkatwal/kafka-util/blob/master/src/main/java/com/bkatwal/kafka/util/KafkaConsumerUtil.java)

 * Potentially interesting: https://howtoprogram.xyz/2016/09/25/spring-kafka-multi-threaded-message-consumption/
 */
public class KafkaTopicContainerManager implements TopicContainerManager {

    private final Logger LOGGER = Logger.getLogger(KafkaTopicContainerManager.class.getName());

    private final ConsumerFactory<String, String> factory;

    private final Map<String, ConcurrentMessageListenerContainer<String, String>> consumersMap =
            new HashMap<>();

    public KafkaTopicContainerManager(ConsumerFactory<String, String> factory) {
        this.factory = factory;
    }

    @Override
    public void createOrStartConsumers(Iterable<String> topics, Object listener) {
        topics.forEach(t -> createOrStartConsumer(t, listener, factory.getConfigurationProperties()));
    }

    @Override
    public void createOrStartConsumer(String topic, Object listener) {
        createOrStartConsumer(topic, listener, factory.getConfigurationProperties());
    }

    @Override
    public void stopConsumers(Iterable<String> topics) {
        topics.forEach(t -> stopConsumer(t));
    }

    @Override
    public void stopConsumer(String topic) {
        LOGGER.fine("stopping consumer for topic \"" + topic + "\"");
        ConcurrentMessageListenerContainer<String, String> container = consumersMap.get(topic);
        if (container == null) {
            return;
        }
        container.stop();
        LOGGER.fine("consumer for topic \"" + topic + "\" stopped!!");
    }

    @Override
    public Lifecycle getConsumer(String topic) {
        return consumersMap.get(topic);
    }

    private void createOrStartConsumer(String topic, Object messageListener, Map<String, Object> consumerConfig) {
        LOGGER.fine("creating kafka consumer for topic \"" + topic + "\"");

        ConcurrentMessageListenerContainer<String, String> container = consumersMap.get(topic);

        if (container != null) {
            startConsumer(container, topic);
            return;
        }

        container = createConsumer(topic, messageListener, consumerConfig);
        container.start();

        consumersMap.put(topic, container);

        LOGGER.fine("created and started kafka consumer for topic \"" + topic + "\"");
    }

    private void startConsumer(ConcurrentMessageListenerContainer<String, String> container, String topic) {
        if (container.isRunning()) {
            LOGGER.fine("Consumer for topic \"" + topic + "\" is already running.");
            return;
        }

        LOGGER.fine("Consumer already created for topic \"" + topic + ",\" starting consumer!!");
        container.start();
        LOGGER.fine("Consumer for topic \"" + topic + "\" started!!!!");
    }

    private ConcurrentMessageListenerContainer<String, String> createConsumer(String topic, Object messageListener, Map<String, Object> consumerConfig) {
        ConcurrentMessageListenerContainer<String, String> container;
        ContainerProperties containerProps = new ContainerProperties(topic);

        container = new ConcurrentMessageListenerContainer<>(factory, containerProps);
        container.setupMessageListener(messageListener);

        return container;
    }
}
