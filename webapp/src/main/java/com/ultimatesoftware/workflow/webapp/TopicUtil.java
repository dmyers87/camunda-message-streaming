package com.ultimatesoftware.workflow.webapp;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;

import java.util.HashSet;
import java.util.Set;

final class TopicUtil {

    public static Set<String> getTopics(MessageTypeMapper mapper) {
        Set<String> topics = new HashSet<>();

        for (MessageTypeExtensionData data : mapper.getAll()) {
            topics.add(data.getTopic());
        }

        return topics;
    }
}
