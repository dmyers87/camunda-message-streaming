package com.ultimatesoftware.workflow.messaging;

import static com.ultimatesoftware.workflow.messaging.Constants.ZERO_UUID;

public final class TenantUtils {

    private TenantUtils() {}

    public static boolean isSystemTenant(String tenantId) {
        return ZERO_UUID.equals(tenantId);
    }
}
