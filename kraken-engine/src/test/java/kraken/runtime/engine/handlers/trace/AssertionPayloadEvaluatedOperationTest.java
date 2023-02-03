/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.model.payload.PayloadType;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;

/**
 * Unit tests for {@code AssertionPayloadEvaluatedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AssertionPayloadEvaluatedOperationTest {

    @Mock
    private AssertionPayload assertionPayload;

    @Mock
    private CompiledExpression compiledExpression;

    @Test
    public void shouldCreateCorrectDescriptionForAssertPayloadEvaluatedToTrue() {
        when(assertionPayload.getType()).thenReturn(PayloadType.ASSERTION);
        when(compiledExpression.getExpressionString()).thenReturn("(field == true)");
        when(assertionPayload.getAssertionExpression()).thenReturn(compiledExpression);

        var assertionPayloadOp = new AssertionPayloadEvaluatedOperation(assertionPayload, Boolean.TRUE);
        
        assertThat(assertionPayloadOp.describe(),
            is("Evaluated 'AssertionPayload' expression '(field == true)' to true."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForAssertPayloadEvaluatedToFalse() {
        when(assertionPayload.getType()).thenReturn(PayloadType.ASSERTION);
        when(compiledExpression.getExpressionString()).thenReturn("(field == true)");
        when(assertionPayload.getAssertionExpression()).thenReturn(compiledExpression);

        var assertionPayloadOp = new AssertionPayloadEvaluatedOperation(assertionPayload, Boolean.FALSE);
        
        assertThat(assertionPayloadOp.describe(),
            is("Evaluated 'AssertionPayload' expression '(field == true)' to false."));
    }

}
