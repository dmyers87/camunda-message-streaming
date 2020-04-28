package com.ultimatesoftware.workflow.messaging.kafka;

import com.ultimatesoftware.workflow.messaging.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.MessageTypeMapper;

import java.util.HashSet;
import java.util.Set;

public final class KafkaUtils {

    public static Set<String> getTopics(MessageTypeMapper mapper) {
        Set<String> topics = new HashSet<>();

        for (MessageTypeExtensionData data : mapper.getAll()) {
            topics.add(data.getTopic());
        }

        return topics;
    }
}
