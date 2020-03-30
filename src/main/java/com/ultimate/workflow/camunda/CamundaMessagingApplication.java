package com.ultimate.workflow.camunda;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.EnumSet;
import java.util.Set;

@SpringBootApplication
public class CamundaMessagingApplication {

    /**
     * Goals:
     * read extension data from the BMPN in order to creating mappings between Kafka and BMPN message objects
     *
     * using the messaging specification, queue a message into Kafka
     * read that message from topic in Camunda and change the state of a BPMN
     *
     * Todos:
     *   Versioning of the message types
     *   Notifications BPMN example
     *   Multi-Tenant BPMNs
     *   Parsing of extension data
     *   Multiple topics per Message Type
     *   Kakfa cluster (aggregate or local) per Message Type
     *
     * @param args
     */
    public static void main(String[] args) {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });

        SpringApplication.run(CamundaMessagingApplication.class, args);
    }

    @Bean
    MessageTypeMapper getMapper() {
        MessageTypeMapper mapper = new MessageTypeMapper();
        MessageTypeExtensionData messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("payment.employee-pay-check.paid");
        messageTypeExtensionData.setBusinessKeyExpression("$.checkNumber");
        mapper.add(messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.delivery.DeliveryCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.deliveryId");
        mapper.add(messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.message-router.EmailNotificationCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.headers.deliveryId");
        mapper.add(messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.message-router.SmsNotificationCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.headers.deliveryId");
        mapper.add(messageTypeExtensionData);

        return mapper;
    }

}
