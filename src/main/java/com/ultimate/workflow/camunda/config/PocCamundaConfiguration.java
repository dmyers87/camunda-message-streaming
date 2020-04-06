package com.ultimate.workflow.camunda.config;

import com.ultimate.workflow.camunda.CustomSpringProcessEngineConfiguration;
import com.ultimate.workflow.camunda.MultiTenantProcessEnginePlugin;
import com.ultimate.workflow.camunda.streaming.MemoryMessageTypeMapper;
import com.ultimate.workflow.camunda.streaming.MessageTypeMapper;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class PocCamundaConfiguration {

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(List<ProcessEnginePlugin> processEnginePlugins) throws IOException {
        // This was helpful https://github.com/camunda/camunda-bpm-platform/blob/3028aa69381b7f55868ba66063774ae73207341c/spring-boot-starter/starter/src/main/java/org/camunda/bpm/spring/boot/starter/CamundaBpmConfiguration.java
        SpringProcessEngineConfiguration config =
                CamundaSpringBootUtil.initCustomFields(new CustomSpringProcessEngineConfiguration(getMapper()));
        config.getProcessEnginePlugins().add(new CompositeProcessEnginePlugin(processEnginePlugins));
        // TODO: need to check if this is coming in on the list above
        config.getProcessEnginePlugins().add(multiTenantProcessPlugin());
        return config;
    }

    @Bean
    public ProcessEnginePlugin multiTenantProcessPlugin() {
        return new MultiTenantProcessEnginePlugin();
    }

    @Bean
    public MessageTypeMapper getMapper() {
        return new MemoryMessageTypeMapper();
    }

}
