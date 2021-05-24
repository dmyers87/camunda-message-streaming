package com.ultimatesoftware.workflow.messaging.kafka;

import java.util.UUID;

public class TestConstants {

    public static final String GENERIC_TOPIC_NAME = "poc";
    public static final String GENERIC_TENANT_ID = UUID.randomUUID().toString();
    public static final String GENERIC_MESSAGE_TYPE = "payment.employee-pay-check.paid";
    public static final String GENERIC_BUSINESS_PROCESS_KEY_FIELD = "genericBusinessProcessKeyField";
    public static final String GENERIC_BUSINESS_PROCESS_KEY_VALUE = "genericBusinessProcessKeyValue";

    public static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();
}
