package com.ultimate.workflow.camunda;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.EnumSet;
import java.util.Set;

import static com.ultimate.workflow.camunda.Constants.TENANT_1;
import static com.ultimate.workflow.camunda.Constants.TENANT_2;

@EnableProcessApplication
@SpringBootApplication
public class CamundaMessagingApplication {

    /**
     * Goals:
     * read extension data from the BMPN in order to creating mappings between Kafka and BMPN message objects
     *
     * using the messaging specification, queue a message into Kafka
     * read that message from topic in Camunda and change the state of a BPMN
     *
     * Completed:
     *   Correlation of messages based on message type
     *   Correlation of messages based on business key (defined as expression)
     *
     * Todos:
     *   Versioning of the message types
     *   Multi-Tenant BPMNs (https://docs.camunda.org/manual/7.5/user-guide/process-engine/multi-tenancy/)
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

        // Tenant 1 Mappings
        MessageTypeExtensionData messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("payment.employee-pay-check.paid");
        messageTypeExtensionData.setBusinessKeyExpression("$.checkNumber");
        mapper.add(TENANT_1, messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.delivery.DeliveryCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.deliveryId");
        mapper.add(TENANT_1, messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.message-router.EmailNotificationCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.headers.deliveryId");
        mapper.add(TENANT_1, messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.message-router.SmsNotificationCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.headers.deliveryId");
        mapper.add(TENANT_1, messageTypeExtensionData);

        // Tenant 2 Mappings
        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("payment.employee-pay-check.paid");
        messageTypeExtensionData.setBusinessKeyExpression("$.checkNumber");
        mapper.add(TENANT_2, messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.delivery.DeliveryCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.correlationId");
        mapper.add(TENANT_2, messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.message-router.EmailNotificationCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.headers.deliveryId");
        mapper.add(TENANT_2, messageTypeExtensionData);

        messageTypeExtensionData = new MessageTypeExtensionData();
        messageTypeExtensionData.setMessageType("naas.message-router.SmsNotificationCreatedEvent");
        messageTypeExtensionData.setBusinessKeyExpression("$.headers.deliveryId");
        mapper.add(TENANT_2, messageTypeExtensionData);

        return mapper;
    }

}
