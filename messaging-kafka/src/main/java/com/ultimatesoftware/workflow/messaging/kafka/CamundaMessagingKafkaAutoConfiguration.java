package com.ultimatesoftware.workflow.messaging.kafka;

import com.ultimatesoftware.workflow.messaging.CamundaMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({CamundaMessagingAutoConfiguration.class})
public class CamundaMessagingKafkaAutoConfiguration {
}
