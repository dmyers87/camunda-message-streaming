package com.ultimatesoftware.workflow.messaging.correlation;

import com.ultimatesoftware.workflow.messaging.GenericMessage;
import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import org.slf4j.MDC;

public class MDCUtils {

    public static final String TENANT_ID_KEY = "tenant_id";
    public static final String MESSAGE_TYPE_KEY = "message_type";

    public static final String ACTIVITY_ID_KEY = "activity_id";
    public static final String PROCESS_DEFINITION_ID_KEY = "process_definition_id";
    public static final String DEPLOYMENT_ID_KEY = "deployment_id";
    public static final String MESSAGE_TYPE_EXTENSION_DATA_KEY = "message_type_extension_data";
    public static final String MESSAGE_BODY_KEY = "deployment_id";

    public static void addRelevantFieldsToContext(MessageTypeExtensionData messageTypeExtensionData, GenericMessage genericMessage) {
        addExtensionDataToContext(messageTypeExtensionData);
        addMessageBodyToContext(genericMessage);
    }

    public static void clearCorrelationDataFieldsFromContext() {
        clearExtensionDataFieldsFromContext();
        clearMessageBodyFromContext();
    }

    private static void addExtensionDataToContext(MessageTypeExtensionData messageTypeExtensionData) {
        MDC.put(ACTIVITY_ID_KEY, messageTypeExtensionData.getActivityId());
        MDC.put(PROCESS_DEFINITION_ID_KEY, messageTypeExtensionData.getProcessDefinitionId());
        MDC.put(DEPLOYMENT_ID_KEY, messageTypeExtensionData.getDeploymentId());
        MDC.put(MESSAGE_TYPE_EXTENSION_DATA_KEY, messageTypeExtensionData.toString());
    }

    private static void clearExtensionDataFieldsFromContext() {
        MDC.remove(ACTIVITY_ID_KEY);
        MDC.remove(PROCESS_DEFINITION_ID_KEY);
        MDC.remove(DEPLOYMENT_ID_KEY);
        MDC.remove(MESSAGE_TYPE_EXTENSION_DATA_KEY);
    }

    private static void addMessageBodyToContext(GenericMessage genericMessage) {
        MDC.put(MESSAGE_BODY_KEY, genericMessage.toString());
    }

    private static void clearMessageBodyFromContext() {
        MDC.remove(MESSAGE_BODY_KEY);
    }
}
