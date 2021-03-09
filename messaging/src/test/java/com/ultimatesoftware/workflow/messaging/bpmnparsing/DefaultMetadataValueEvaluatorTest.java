package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import com.ultimatesoftware.workflow.messaging.CamundaMessagingAutoConfiguration;
import com.ultimatesoftware.workflow.messaging.config.CamundaConfiguration;
import com.ultimatesoftware.workflow.messaging.config.CustomSpringProcessApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
        CustomSpringProcessApplication.class,
        CamundaConfiguration.class,
        CamundaMessagingAutoConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
public class DefaultMetadataValueEvaluatorTest {

    @Autowired
    private MetadataValueEvaluator<String> metadataValueEvaluator;

    @Test
    public void whenMetadataValueIsHardcoded_itShouldEvaluateToTheSameValue() {
        String topicValue = "poc";

        String evaluatedValue = metadataValueEvaluator.evaluate(topicValue);

        assertThat(evaluatedValue).isNotNull();
        assertThat(evaluatedValue).isEqualTo(topicValue);
    }

    @Test
    void whenMetadataValueContainsAValidSpelExpression_itShouldEvaluateTheExpressionToAValue() {
        String topicValue = "#{systemProperties['java.runtime.name']}";

        String evaluatedValue = metadataValueEvaluator.evaluate(topicValue);

        assertThat(evaluatedValue).isNotNull();
        assertThat(evaluatedValue).isNotEqualTo(topicValue);
    }

    @Test
    void whenMetadataValueContainsAnUndefinedEnvironmentVariable_itShouldStillEvaluateValue() {
        String prefix = "#{systemEnvironment['SOME_UNDEFINED_ENVIRONMENT_VARIABLE']}";
        String postfix = ".aPostfix";
        String metadataValue = prefix + postfix;

        String evaluatedValue = metadataValueEvaluator.evaluate(metadataValue);

        assertThat(evaluatedValue).isNotNull();
        assertThat(evaluatedValue).isEqualTo(postfix);
    }

    @Test
    void whenMetadataValueContainsAnInvalidSpelExpression_ItShouldThrowAnException() {
        String metadataValue = "#{Invalid${expression";
        assertThrows(RuntimeException.class, () -> metadataValueEvaluator.evaluate(metadataValue));
    }

}
