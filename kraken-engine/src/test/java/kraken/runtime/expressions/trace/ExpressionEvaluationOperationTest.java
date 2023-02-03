/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.expressions.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.ExternalDataReference;
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

    @Mock
    private DataContext dataContext;

    @Mock
    private ExternalDataReference ccrExtDataContext;

    @Mock
    private DataContext ccrDataContext;

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationWithCrossContexts() {
        var ccrVariable = new ExpressionVariable("CCRContext", ExpressionVariableType.CROSS_CONTEXT);

        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");
        when(expression.getExpressionVariables()).thenReturn(List.of(ccrVariable));
        when(dataContext.getExternalReferences()).thenReturn(Map.of("CCRContext", ccrExtDataContext));

        when(ccrDataContext.getContextName()).thenReturn("CCRContext");
        when(ccrDataContext.getContextId()).thenReturn("ID");
        when(ccrExtDataContext.getDataContexts()).thenReturn(List.of(ccrDataContext));
        when(ccrExtDataContext.getName()).thenReturn("ccrCtx");

        var expEvalOp = new ExpressionEvaluationOperation(expression, dataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'. Cross context references: "
                + System.lineSeparator()
                + "ccrCtx: [ CCRContext:ID ]"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationNoCrossContexts() {
        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");

        var expEvalOp = new ExpressionEvaluationOperation(expression, dataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForExpressionEvaluationUnresolvedCrossContexts() {
        var ccrVariable = new ExpressionVariable("CCRContext", ExpressionVariableType.CROSS_CONTEXT);

        when(expression.getExpressionVariables()).thenReturn(List.of(ccrVariable));
        when(expression.getExpressionString()).thenReturn("(CCRContext.field == false)");

        var expEvalOp = new ExpressionEvaluationOperation(expression, dataContext);

        assertThat(expEvalOp.describe(),
            is("Evaluating expression '(CCRContext.field == false)'. Cross context references: "
                + System.lineSeparator()
                + "No cross contexts resolved for 'CCRContext'"));
    }

}
