package com.ultimatesoftware.workflow.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

public class LoggingMessageListener implements MessageListener<String, String> {
    private final java.util.logging.Logger LOGGER = Logger.getLogger(LoggingMessageListener.class.getName());

    @Transactional
    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        if (record.value().isEmpty()) {
            LOGGER.warning("Consumer received an empty record from subscribed topic: " + record.topic());
            return;
        }

        LOGGER.fine("Message received from topic: \"" + record.topic() + "\" value: \"" + record.value() +"\"");
    }
}
