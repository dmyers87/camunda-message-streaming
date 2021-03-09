package com.ultimatesoftware.workflow.messaging.bpmnparsing;

import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DefaultMetadataValueEvaluator implements MetadataValueEvaluator {

    private final EvaluationContext evaluationContext;
    private final ExpressionParser expressionParser;

    public DefaultMetadataValueEvaluator(Environment environment) {
        evaluationContext = new StandardEvaluationContext(environment);
        expressionParser = new SpelExpressionParser();
    }

    @Override
    public <T> T evaluate(String key, String value, Class<T> clazz) {
        return expressionParser.parseExpression(value, ParserContext.TEMPLATE_EXPRESSION)
                .getValue(evaluationContext, clazz);
    }
}
