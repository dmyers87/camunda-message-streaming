package com.ultimatesoftware.workflow.messaging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestConstants {

    public static final String GENERIC_TOPIC_NAME = "poc";
    public static final String GENERIC_TENANT_ID = UUID.randomUUID().toString();
    public static final String GENERIC_MESSAGE_TYPE = "payment.employee-pay-check.paid";
    public static final String GENERIC_BUSINESS_PROCESS_KEY_FIELD = "genericBusinessProcessKeyField";
    public static final String GENERIC_BUSINESS_PROCESS_KEY_VALUE = "genericBusinessProcessKeyValue";
    public static final String GENERIC_NESTED_VARIABLE_FIELD = "genericExtraInformationField";
    public static final Map<String, Object> GENERIC_NESTED_VARIABLE_VALUE = new HashMap<String, Object>()
        {{
            put("dollarAmount", 10000);
            put("deliver", new HashMap<String, Object>(){{
                put("deliverItems", Arrays.asList("computer", "apple"));
                put("arrived", false);
            }});
        }};
    public static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();
}
