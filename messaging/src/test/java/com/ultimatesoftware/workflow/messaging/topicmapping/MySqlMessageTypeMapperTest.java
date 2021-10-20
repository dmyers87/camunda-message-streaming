package com.ultimatesoftware.workflow.messaging.topicmapping;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ultimatesoftware.workflow.messaging.bpmnparsing.MessageTypeExtensionData;
import com.ultimatesoftware.workflow.messaging.builders.MessageTypeExtensionDataBuilder;
import com.ultimatesoftware.workflow.messaging.topicmapping.entities.ExtensionData;
import com.ultimatesoftware.workflow.messaging.topicmapping.repositories.ExtensionDataRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MySqlMessageTypeMapperTest {

    @Mock
    ExtensionDataRepository extensionDataRepository;

    private MySqlMessageTypeMapper messageTypeMapper;

    @BeforeEach
    public void setup() {
        messageTypeMapper = new MySqlMessageTypeMapper(extensionDataRepository);
    }

    @Test
    public void whenInitializeProcessDefinitionIdsCalled_shouldUpdateExtensionDataWithIds() {
        String deploymentId = UUID.randomUUID().toString();
        String processDefinitionId = UUID.randomUUID().toString();
        String processDefinitionKey = "processDefinitionKey";
        ProcessDefinitionEntity processDefinitionEntity = mock(ProcessDefinitionEntity.class);
        when(processDefinitionEntity.getId()).thenReturn(processDefinitionId);
        when(processDefinitionEntity.getKey()).thenReturn(processDefinitionKey);

        ExtensionData extensionData = mock(ExtensionData.class);
        when(extensionData.getProcessDefinitionKey()).thenReturn(processDefinitionKey);

        List<ExtensionData> extensionDataList = Collections.singletonList(extensionData);
        when(extensionDataRepository.findAllByDeploymentId(deploymentId)).thenReturn(extensionDataList);

        messageTypeMapper.initializeProcessDefinitionIds(deploymentId, Collections.singletonList(processDefinitionEntity));

        verify(extensionDataRepository).findAllByDeploymentId(deploymentId);
        verify(extensionData).setProcessDefinitionId(processDefinitionId);
        verify(extensionDataRepository).saveAll(extensionDataList);
        verifyNoMoreInteractions(extensionDataRepository);
    }

    @Test
    public void whenInitializeProcessDefinitionIdsCalled_andMismatchExists_shouldThrowException() {
        String deploymentId = UUID.randomUUID().toString();
        String processDefinitionKey = "processDefinitionKey";
        ProcessDefinitionEntity processDefinitionEntity = mock(ProcessDefinitionEntity.class);
        when(processDefinitionEntity.getKey()).thenReturn(processDefinitionKey);

        ExtensionData extensionData = mock(ExtensionData.class);
        when(extensionData.getProcessDefinitionKey()).thenReturn("wrong-key");

        when(extensionDataRepository.findAllByDeploymentId(deploymentId))
            .thenReturn(Collections.singletonList(extensionData));

        assertThrows(RuntimeException.class, () ->
            messageTypeMapper.initializeProcessDefinitionIds(deploymentId, Collections.singletonList(processDefinitionEntity)));
    }

    @Test
    public void whenInitializeProcessDefinitionIdsCalled_andValuesAlreadySet_shouldDoNothing() {
        String deploymentId = UUID.randomUUID().toString();
        String processDefinitionId = UUID.randomUUID().toString();
        ProcessDefinitionEntity processDefinitionEntity = mock(ProcessDefinitionEntity.class);

        ExtensionData extensionData = mock(ExtensionData.class);
        when(extensionData.getProcessDefinitionId()).thenReturn(processDefinitionId);

        List<ExtensionData> extensionDataList = Collections.singletonList(extensionData);
        when(extensionDataRepository.findAllByDeploymentId(deploymentId)).thenReturn(extensionDataList);

        messageTypeMapper.initializeProcessDefinitionIds(deploymentId, Collections.singletonList(processDefinitionEntity));

        verify(extensionData, never()).setProcessDefinitionId(processDefinitionId);
    }

    @Test
    public void whenDeleteCalled_shouldCallExtensionRepositoryToDeleteAllExtensionDataForDeployment() {
        String deploymentId = UUID.randomUUID().toString();
        List<ExtensionData> extensionDataList = Arrays.asList(mock(ExtensionData.class), mock(ExtensionData.class));
        when(extensionDataRepository.findAllByDeploymentId(deploymentId)).thenReturn(extensionDataList);

        messageTypeMapper.delete(deploymentId);

        verify(extensionDataRepository).deleteAll(extensionDataList);
    }

}
