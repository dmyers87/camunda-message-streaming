package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.ultimatesoftware.workflow.messaging.topicmapping.MessageTypeMapper;
import java.util.ArrayList;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;

/**
 * In order to prevent Extension Data elements from attempting correlation cross incompatible versions,
 * we need to use the Process Definition Id during the execution query in the GenericMessageCorrelator.
 *
 * However, this value is not available until after the deployment has been completed
 * and the BPMN has been deployed.
 *
 * This plugin initializes the Process Definition Id value in the newly created ExtensionData elements,
 * to prevent additional lookups at runtime.
 */
public class PostBpmnDeployExtensionDataInitializerPlugin extends AbstractProcessEnginePlugin {

    private final MessageTypeMapper messageTypeMapper;

    public PostBpmnDeployExtensionDataInitializerPlugin(MessageTypeMapper messageTypeMapper) {
        this.messageTypeMapper = messageTypeMapper;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        if (processEngineConfiguration.getCustomPostDeployers() == null) {
            processEngineConfiguration.setCustomPostDeployers(new ArrayList<>());
        }
        processEngineConfiguration.getCustomPostDeployers().add(
            new ExtensionDataDeployer(messageTypeMapper));
    }

    public static class ExtensionDataDeployer implements Deployer {

        private final MessageTypeMapper messageTypeMapper;

        public ExtensionDataDeployer(MessageTypeMapper messageTypeMapper) {
            this.messageTypeMapper = messageTypeMapper;
        }

        @Override
        public void deploy(DeploymentEntity deployment) {
            messageTypeMapper.initializeProcessDefinitionIds(deployment.getId(), deployment.getDeployedProcessDefinitions());
        }
    }
}
