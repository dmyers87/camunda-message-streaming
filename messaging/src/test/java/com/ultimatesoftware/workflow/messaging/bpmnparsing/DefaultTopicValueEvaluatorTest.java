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
public class DefaultTopicValueEvaluatorTest {

    @Autowired
    private TopicValueEvaluator topicValueEvaluator;

    @Test
    public void whenValueForTopicIsHardcoded_shouldEvaluateToSameValue() {
        String topicValue = "poc";

        String evaluatedValue = topicValueEvaluator.evaluate(topicValue);

        assertThat(evaluatedValue).isNotNull();
        assertThat(evaluatedValue).isEqualTo(topicValue);
    }

    @Test
    void whenValueForTopicContainsValidSpelExpression_shouldEvaluateExpressionToValue() {
        String topicValue = "#{systemProperties['java.runtime.name']}";

        String evaluatedValue = topicValueEvaluator.evaluate(topicValue);

        assertThat(evaluatedValue).isNotNull();
        assertThat(evaluatedValue).isNotEqualTo(topicValue);
    }

    @Test
    void whenTopicValueContainsUndefinedEnvironmentVariable_shouldStillEvaluateValue() {
        String prefix = "#{systemEnvironment['SOME_UNDEFINED_ENVIRONMENT_VARIABLE']}";
        String postfix = ".aPostfix";
        String topicValue = prefix + postfix;

        String evaluatedValue = topicValueEvaluator.evaluate(topicValue);

        assertThat(evaluatedValue).isNotNull();
        assertThat(evaluatedValue).isEqualTo(postfix);
    }

    @Test
    void whenValueForTopicContainsInvalidSpelExpression_shouldThrowAnException() {
        String topicValue = "#{Invalid${expression";
        assertThrows(RuntimeException.class, () -> topicValueEvaluator.evaluate(topicValue));
    }

}
