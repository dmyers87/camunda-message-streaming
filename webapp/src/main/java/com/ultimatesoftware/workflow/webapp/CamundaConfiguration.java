package com.ultimatesoftware.workflow.webapp;

import java.io.IOException;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfiguration {

    @Autowired
    public BpmnParseFactory bpmnParseFactory;

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(List<ProcessEnginePlugin> processEnginePlugins) throws IOException {
        // This was helpful https://github.com/camunda/camunda-bpm-platform/blob/3028aa69381b7f55868ba66063774ae73207341c/spring-boot-starter/starter/src/main/java/org/camunda/bpm/spring/boot/starter/CamundaBpmConfiguration.java
        SpringProcessEngineConfiguration config =
                CamundaSpringBootUtil.initCustomFields(
                        new CustomSpringProcessEngineConfiguration(bpmnParseFactory));

        config.getProcessEnginePlugins().add(new CompositeProcessEnginePlugin(processEnginePlugins));
        return config;
    }
}
