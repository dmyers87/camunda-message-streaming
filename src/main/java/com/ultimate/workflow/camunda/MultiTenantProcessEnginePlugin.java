package com.ultimate.workflow.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ultimate.workflow.camunda.Constants.TENANT_1;
import static com.ultimate.workflow.camunda.Constants.TENANT_2;

@Component
public class MultiTenantProcessEnginePlugin extends AbstractProcessEnginePlugin {

    @Override
    public void postProcessEngineBuild(ProcessEngine processEngine) {
        List<String> tenants = Arrays.asList(
                TENANT_1,
                TENANT_2
        );

        for (String tenant: tenants) {
            try {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources("classpath:processes/" + tenant + "/*");

                for (Resource resource : resources) {
                    processEngine.getRepositoryService()
                            .createDeployment()
                            .tenantId(tenant)
                            .name(tenant)
                            .addInputStream(resource.getFilename(), resource.getInputStream())
                            .deploy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
