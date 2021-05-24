package com.ultimatesoftware.workflow.messaging.correlation;

import static com.ultimatesoftware.workflow.messaging.Constants.ZERO_UUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import com.ultimatesoftware.workflow.messaging.TenantUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class TenantUtilsTest {

    @Test
    public void whenTenantIsSystemTenant_shouldReturnTrue() {
        assertThat(TenantUtils.isSystemTenant(ZERO_UUID))
            .isTrue();
    }

    @Test
    public void whenTenantIsNotSystemTenant_shouldReturnFalse() {
        assertThat(TenantUtils.isSystemTenant(UUID.randomUUID().toString()))
            .isFalse();
    }
}
