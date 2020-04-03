package com.ultimate.workflow.camunda.streaming;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class KafkaTopicContainerManager implements TopicContainerManager {
    // Credit to Bikas Katwal
    //  * https://medium.com/@bikas.katwal10/start-stop-kafka-consumers-or-subscribe-to-new-topic-programmatically-using-spring-kafka-2d4fb77c9117
    //  * https://github.com/bkatwal/kafka-util/blob/master/src/main/java/com/bkatwal/kafka/util/KafkaConsumerUtil.java)
    //
    // Potentially interesting: https://howtoprogram.xyz/2016/09/25/spring-kafka-multi-threaded-message-consumption/

    private final Logger LOGGER = Logger.getLogger(KafkaTopicContainerManager.class.getName());

    private final Map<String, ConcurrentMessageListenerContainer<String, String>> consumersMap =
            new HashMap<>();

    @Override
    public void createOrStartConsumer(String topic, Object messageListener, Map<String, Object> consumerConfig) {
        LOGGER.fine("creating kafka consumer for topic \"" + topic + "\"");

        ConcurrentMessageListenerContainer<String, String> container = consumersMap.get(topic);

        // start the container if not started
        if (container != null) {
            if (!container.isRunning()) {
                LOGGER.fine("Consumer already created for topic \"" + topic + ",\" starting consumer!!");
                container.start();
                LOGGER.fine("Consumer for topic \"" + topic + "\" started!!!!");
            }
            return;
        }

        container = createConsumer(topic, messageListener, consumerConfig);

        container.start();

        consumersMap.put(topic, container);

        LOGGER.fine("created and started kafka consumer for topic \"" + topic + "\"");
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

    private ConcurrentMessageListenerContainer<String, String> createConsumer(String topic, Object messageListener, Map<String, Object> consumerConfig) {
        ConcurrentMessageListenerContainer<String, String> container;
        ContainerProperties containerProps = new ContainerProperties(topic);

//        containerProps.setPollTimeout(100);
//        Boolean enableAutoCommit = (Boolean) consumerConfig.get(ENABLE_AUTO_COMMIT_CONFIG);
//        if (!enableAutoCommit) {
//            containerProps.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
//        }

        ConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory(consumerConfig);

        container = new ConcurrentMessageListenerContainer<>(factory, containerProps);

        container.setupMessageListener(messageListener);

//        if (concurrency == 0) {
//            container.setConcurrency(1);
//        } else {
//            container.setConcurrency(concurrency);
//        }

        return container;
    }
}
