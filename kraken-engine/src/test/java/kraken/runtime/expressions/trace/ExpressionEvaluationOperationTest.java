/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.expressions.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.model.context.Cardinality;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataReference;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionVariable;
import kraken.runtime.model.expression.ExpressionVariableType;

/**
 * Unit tests for {@code ExpressionEvaluationOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpressionEvaluationOperationTest {

    @Mock
    private CompiledExpression expression;

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationWithCrossContexts() {
        var ccrVariable = new ExpressionVariable("CCRContext", ExpressionVariableType.CROSS_CONTEXT);
        var referenceDataContext = context("SpecificCCRContext", "id", List.of());
        var reference = new DataReference("CCRContext", List.of(referenceDataContext), Cardinality.MULTIPLE);
        var rootDataContext = context("Root", "1", List.of(reference));

        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");
        when(expression.getExpressionVariables()).thenReturn(List.of(ccrVariable));

        var expEvalOp = new ExpressionEvaluationOperation(expression, rootDataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'. Cross context references: "
                + System.lineSeparator()
                + "CCRContext=[SpecificCCRContext:id]"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationNoCrossContexts() {
        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");
        var rootDataContext = context("Root", "1", List.of());

        var expEvalOp = new ExpressionEvaluationOperation(expression, rootDataContext);

        assertThat(expEvalOp.describe(), is("Evaluating expression '(CCRContext.field == false)'."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationUnresolvedCrossContexts() {
        var ccrVariable = new ExpressionVariable("CCRContext", ExpressionVariableType.CROSS_CONTEXT);
        var rootDataContext = context("Root", "1", List.of());

        when(expression.getExpressionVariables()).thenReturn(List.of(ccrVariable));
        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");

        var expEvalOp = new ExpressionEvaluationOperation(expression, rootDataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'. Cross context references: "
                + System.lineSeparator()
                + "CCRContext=null"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationNullSingularCrossContexts() {
        var ccrVariable = new ExpressionVariable("CCRContext", ExpressionVariableType.CROSS_CONTEXT);
        var reference = new DataReference("CCRContext", List.of(), Cardinality.SINGLE);
        var rootDataContext = context("Root", "1", List.of(reference));

        when(expression.getExpressionVariables()).thenReturn(List.of(ccrVariable));
        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");

        var expEvalOp = new ExpressionEvaluationOperation(expression, rootDataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'. Cross context references: "
                + System.lineSeparator()
                + "CCRContext=null"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationEmptyMultipleCrossContexts() {
        var ccrVariable = new ExpressionVariable("CCRContext", ExpressionVariableType.CROSS_CONTEXT);
        var reference = new DataReference("CCRContext", List.of(), Cardinality.MULTIPLE);
        var rootDataContext = context("Root", "1", List.of(reference));

        when(expression.getExpressionVariables()).thenReturn(List.of(ccrVariable));
        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");

        var expEvalOp = new ExpressionEvaluationOperation(expression, rootDataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'. Cross context references: "
                + System.lineSeparator()
                + "CCRContext=[]"));
    }

    private DataContext context(String name, String id, List<DataReference> references) {
        var context = new DataContext();
        context.setContextName(name);
        context.setContextId(id);
        references.forEach(context::updateReference);
        return context;
    }
}
