/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.runtime.expressions;

import static kraken.runtime.model.expression.ExpressionType.COMPLEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import kraken.context.model.tree.ContextModelTreeMetadata;
import kraken.context.model.tree.impl.ContextModelTreeImpl;
import kraken.el.TargetEnvironment;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.functionregistry.functions.DateFunctions;
import kraken.el.scope.Scope;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;

/**
 * @author mulevicius
 */
public class KrakenExpressionEvaluatorTest {

    private KrakenExpressionEvaluator evaluator = new KrakenExpressionEvaluator();

    @Test
    public void shouldWrapMissingPropertyToException() {
        String expression = "Policy.notExistingAttribute.aaa";

        Policy policy = new Policy("policyCd");

        Map<String, Object> context = Map.of("Policy", policy);

        assertThrows(KrakenExpressionEvaluationException.class,
                () -> evaluator.evaluateGetProperty(expression, context));
    }

    @Test
    public void shouldPassThroughExceptionThrownFromMethod() {
        String expression = "Policy.thrownException";

        Policy policy = new Policy("policyCd");

        Map<String, Object> context = Map.of("Policy", policy);

        assertThrows(KrakenExpressionEvaluationException.class,
                () -> evaluator.evaluateGetProperty(expression, context));
    }

    @Test
    public void shouldEvaluateEmptyTemplateVariables() {
        ErrorMessage errorMessage = new ErrorMessage("code", List.of(), List.of());
        var templateVariables = evaluator.evaluateTemplateVariables(errorMessage, null, session(Map.of()));
        assertThat(templateVariables, empty());
    }

    @Test
    public void shouldEvaluateAndFormatTemplateVariables() {
        Policy policy = new Policy("P01");
        Map<String, Object> context = Map.of("Policy", policy);
        ErrorMessage errorMessage = new ErrorMessage("code", List.of(), List.of(
            expression("'string'"),
            expression("10.123"),
            expression("true"),
            expression("false"),
            expression("null"),
            expression("2020-01-01"),
            expression("2020-01-01T10:00:00Z"),
            expression("Policy.policyCd"),
            expression("Policy.unknownProperty")
        ));
        DataContext dataContext = dataContext("Policy", policy);
        var templateVariables = evaluator.evaluateTemplateVariables(errorMessage, dataContext, session(context));
        assertThat(templateVariables, hasItems(
            "string",
            BigDecimal.valueOf(10.123),
            true,
            false,
            null,
            DateFunctions.date("2020-01-01"),
            DateFunctions.dateTime("2020-01-01T10:00:00Z"),
            "P01",
            null
        ));
    }

    private CompiledExpression expression(String expression) {
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        return new CompiledExpression(expression, expression, COMPLEX, null, null, List.of(), ast);
    }

    private DataContext dataContext(String name, Object root) {
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(root);
        dataContext.setContextId(name);
        dataContext.setContextName(name);
        return dataContext;
    }

    private EvaluationSession session(Map<String, Object> context) {
        return new EvaluationSession(
            new EvaluationConfig(context, "USD"),
            mock(KrakenTypeProvider.class),
            Map.of(),
            "",
            new ContextModelTreeImpl(
                Map.of(),
                Map.of(),
                new ContextModelTreeMetadata("test", TargetEnvironment.JAVA))
        );
    }

    public static class Policy {

        private String policyCd;

        public Policy(String policyCd) {
            this.policyCd = policyCd;
        }

        public String getPolicyCd() {
            return policyCd;
        }
    }
}
