/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.handlers;

import kraken.model.payload.PayloadType;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.conditions.RuleApplicabilityEvaluator;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.evaluation.loop.RuleEvaluationInstance;
import kraken.runtime.engine.result.reducers.validation.OverrideDependencyExtractor;
import kraken.runtime.expressions.KrakenExpressionEvaluationException;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@code RulePayloadProcessorImpl} class.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class RulePayloadProcessorTest {

    @InjectMocks
    private RulePayloadProcessorImpl testObject;

    @Mock
    private KrakenExpressionEvaluator krakenExpressionEvaluator;

    @Mock
    private RuleApplicabilityEvaluator applicabilityEvaluator;

    @Mock
    private OverrideDependencyExtractor overrideDependencyExtractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(applicabilityEvaluator.evaluateCondition(any(), any())).thenReturn(ConditionEvaluationResult.APPLICABLE);

        testObject = RulePayloadProcessorImpl.create(krakenExpressionEvaluator, applicabilityEvaluator);
    }

    @Test
    public void shouldNotBuildOverridableContextWhenExpressionEvaluationFails() {
        when(krakenExpressionEvaluator.evaluate(any(), any(), any()))
                .thenThrow(new KrakenExpressionEvaluationException("", "", "", null));

        RuleEvaluationInstance ruleEvaluationInstance = createEvaluationInstance(
                createAssertionPayloadRule(createAssertionPayload(true)), new DataContext());

        RuleEvaluationResult result = testObject.process(ruleEvaluationInstance, null);

        verify(overrideDependencyExtractor, times(0))
                .extractOverrideDependencies(any(), any());
        verify(krakenExpressionEvaluator, times(0))
                .evaluateTargetField(any(), any());

        assertTrue(result.getOverrideInfo().isOverridable());
        assertNull(result.getOverrideInfo().getOverridableRuleContextInfo());
    }

    private RuleEvaluationInstance createEvaluationInstance(RuntimeRule runtimeRule, DataContext dataContext) {
        RuleEvaluationInstance ruleEvaluationInstance = mock(RuleEvaluationInstance.class);
        when(ruleEvaluationInstance.getRule()).thenReturn(runtimeRule);
        when(ruleEvaluationInstance.getDataContext()).thenReturn(dataContext);

        return ruleEvaluationInstance;
    }

    private RuntimeRule createAssertionPayloadRule(AssertionPayload assertionPayload) {
        RuntimeRule runtimeRule = mock(RuntimeRule.class);

        when(runtimeRule.getPayload()).thenReturn(assertionPayload);
        when(runtimeRule.getContext()).thenReturn("RuleCtx");
        when(runtimeRule.getName()).thenReturn("RuleName");

        return runtimeRule;
    }

    private AssertionPayload createAssertionPayload(boolean isOverridable) {
        AssertionPayload assertionPayload = mock(AssertionPayload.class);
        CompiledExpression compiledExpression = mock(CompiledExpression.class);

        when(compiledExpression.getExpressionString()).thenReturn("");
        when(assertionPayload.getType()).thenReturn(PayloadType.ASSERTION);
        when(assertionPayload.getSeverity()).thenReturn(ValidationSeverity.critical);
        when(assertionPayload.isOverridable()).thenReturn(isOverridable);
        when(assertionPayload.getAssertionExpression()).thenReturn(compiledExpression);

        return assertionPayload;
    }

}

