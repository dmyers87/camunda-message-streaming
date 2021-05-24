package com.ultimatesoftware.workflow.messaging.config;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessagingProperties;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.h2.Driver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
@EnableConfigurationProperties({MessagingProperties.class})
public class CamundaConfiguration {

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(BpmnParseFactory bpmnParseFactory,
                                                                       DataSourceTransactionManager transactionManager,
                                                                       SimpleDriverDataSource dataSource) {
        CustomSpringProcessEngineConfiguration processEngineConfiguration =
            new CustomSpringProcessEngineConfiguration(bpmnParseFactory);
        processEngineConfiguration.setTransactionManager(transactionManager);
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate("create-drop");

        return CamundaSpringBootUtil.initCustomFields(processEngineConfiguration);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(SimpleDriverDataSource ds) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(ds);
        return transactionManager;
    }

    @Bean
    public SimpleDriverDataSource dataSource() {
        SimpleDriverDataSource ds = new SimpleDriverDataSource();
        ds.setDriverClass(Driver.class);
        ds.setUrl("jdbc:h2:mem:process-engine;DB_CLOSE_DELAY=1000");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }
}
