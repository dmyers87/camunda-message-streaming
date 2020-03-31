package com.ultimate.workflow.camunda.config;

import com.ultimate.workflow.camunda.CustomSpringProcessEngineConfiguration;
import com.ultimate.workflow.camunda.MultiTenantProcessEnginePlugin;
import com.ultimate.workflow.camunda.streaming.MessageTypeMapper;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.SpringProcessEngineServicesConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@Import( SpringProcessEngineServicesConfiguration.class )
public class PocCamundaConfiguration {

    @Value("${camunda.bpm.history-level:none}")
    private String historyLevel;

    @Autowired
    private DataSource dataSource;

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() throws IOException {
        SpringProcessEngineConfiguration config = new CustomSpringProcessEngineConfiguration(getMapper());
        CamundaSpringBootUtil.initCustomFields(config);

        config.setDataSource(dataSource);
        config.setDatabaseSchemaUpdate("true");

        config.setTransactionManager(transactionManager());

        config.setHistory(historyLevel);

        config.setJobExecutorActivate(true);
        config.setMetricsEnabled(false);

        config.getProcessEnginePlugins().add(multiTenantProcessPlugin());
        return config;
    }

    @Bean
    public ProcessEnginePlugin multiTenantProcessPlugin() {
        return new MultiTenantProcessEnginePlugin();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ProcessEngineFactoryBean processEngine() throws IOException {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }

    @Bean
    MessageTypeMapper getMapper() {
        MessageTypeMapper mapper = new MessageTypeMapper();
        return mapper;
    }
}
