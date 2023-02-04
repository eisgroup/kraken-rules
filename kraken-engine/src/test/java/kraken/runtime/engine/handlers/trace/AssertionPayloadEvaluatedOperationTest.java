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
