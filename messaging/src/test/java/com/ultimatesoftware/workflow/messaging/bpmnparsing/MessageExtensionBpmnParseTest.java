package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_MESSAGE_TYPE;
import static com.ultimatesoftware.workflow.messaging.TestConstants.GENERIC_TOPIC_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.ultimatesoftware.workflow.messaging.CamundaMessagingAutoConfiguration;
import com.ultimatesoftware.workflow.messaging.config.CamundaConfiguration;
import com.ultimatesoftware.workflow.messaging.config.CustomSpringProcessApplication;
import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import java.util.UUID;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = {
    CustomSpringProcessApplication.class,
    CamundaConfiguration.class,
    CamundaMessagingAutoConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
public class MessageExtensionBpmnParseTest {

    @Autowired
    private MessageTypeMapper messageTypeMapper;

    @Autowired
    private RepositoryService repositoryService;

    private Deployment deployment;

    private String tenantId;

    @BeforeEach
    public void setup() {
        tenantId = UUID.randomUUID().toString();
    }

    @AfterEach
    public void teardown() {
        repositoryService.deleteDeployment(deployment.getId());
    }

    @Test
    public void parseStartEvent() {
        deployment = repositoryService.createDeployment()
            .tenantId(tenantId)
            .addClasspathResource("processes/simple+start-message.bpmn")
            .deploy();

        MessageTypeExtensionData messageTypeExtensionData =
            messageTypeMapper.find(GENERIC_TOPIC_NAME, tenantId, GENERIC_MESSAGE_TYPE)
                .iterator().next();

        assertThat(messageTypeExtensionData).isNotNull();
        assertThat(messageTypeExtensionData.getActivityId()).isEqualTo("simpleStartEvent");
        assertThat(messageTypeExtensionData.getProcessDefinitionId()).isNotNull();
    }

    @Test
    public void parseIntermediateCatchEvent() {
        deployment = repositoryService.createDeployment()
            .tenantId(tenantId)
            .addClasspathResource("processes/simple+intermediate-catch-message.bpmn")
            .deploy();

        MessageTypeExtensionData messageTypeExtensionData =
            messageTypeMapper.find(GENERIC_TOPIC_NAME, tenantId, GENERIC_MESSAGE_TYPE)
                .iterator().next();

        assertThat(messageTypeExtensionData).isNotNull();
        assertThat(messageTypeExtensionData.getActivityId()).isEqualTo("simpleIntermediateCatchEvent");
        assertThat(messageTypeExtensionData.getProcessDefinitionId()).isNotNull();
    }

    @Test
    public void parseBoundaryEvents() {
        deployment = repositoryService.createDeployment()
            .tenantId(tenantId)
            .addClasspathResource("processes/simple+interupting-boundary-message.bpmn")
            .deploy();

        MessageTypeExtensionData messageTypeExtensionData =
            messageTypeMapper.find(GENERIC_TOPIC_NAME, tenantId, GENERIC_MESSAGE_TYPE)
                .iterator().next();

        assertThat(messageTypeExtensionData).isNotNull();
        assertThat(messageTypeExtensionData.getActivityId()).isEqualTo("simpleBoundaryEvent");
        assertThat(messageTypeExtensionData.getProcessDefinitionId()).isNotNull();
    }

    @Test
    public void parseReceiveTask() {
        deployment = repositoryService.createDeployment()
            .tenantId(tenantId)
            .addClasspathResource("processes/simple+receive-task.bpmn")
            .deploy();

        MessageTypeExtensionData messageTypeExtensionData =
            messageTypeMapper.find(GENERIC_TOPIC_NAME, tenantId, GENERIC_MESSAGE_TYPE)
                .iterator().next();

        assertThat(messageTypeExtensionData).isNotNull();
        assertThat(messageTypeExtensionData.getActivityId()).isEqualTo("simpleReceiveTask");
        assertThat(messageTypeExtensionData.getProcessDefinitionId()).isNotNull();
    }
}
