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
package kraken.testing.matchers;

import java.util.List;
import java.util.Optional;

import kraken.model.payload.PayloadType;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.events.RuleEvent;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.runtime.model.rule.payload.ui.AccessibilityPayload;
import kraken.runtime.model.rule.payload.ui.VisibilityPayload;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@code KrakenMatchers} class.
 *
 * @author Tomas Dapkunas
 * @since 1.0.38
 */
public class KrakenMatchersTest {

    private static final String RULE_NAME = "rule001";

    @Test
    public void shouldEvaluateResultsValueChangeEventsCountMatcher() {
        RuntimeRule rule = setupDefaultRule();

        List<RuleEvent> events = mock(List.class);
        when(events.size()).thenReturn(2);

        DefaultValuePayloadResult payloadResult = mock(DefaultValuePayloadResult.class);
        when(payloadResult.getEvents()).thenReturn(events);

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasValueChangeEvents(2));
    }

    @Test
    public void shouldEvaluateResultsDisabledFieldsCountMatcher() {
        RuntimeRule rule = setupAccessibilityRule();

        AccessibilityPayloadResult payloadResult = mock(AccessibilityPayloadResult.class);
        when(payloadResult.getAccessible()).thenReturn(false);

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasDisabledFields(1));
    }

    @Test
    public void shouldEvaluateResultsHiddenFieldsCountMatcher() {
        RuntimeRule rule = setupVisibilityRule();

        VisibilityPayloadResult payloadResult = mock(VisibilityPayloadResult.class);
        when(payloadResult.getVisible()).thenReturn(false);

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasHiddenFields(1));
    }

    @Test
    public void shouldEvaluateResultsCountMatcher() {
        List<RuleEvaluationResult> evaluationResult = mock(List.class);
        when(evaluationResult.size()).thenReturn(2);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(evaluationResult);

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasRuleResults(2));
    }

    @Test
    public void shouldEvaluateApplicableResultsCountMatcher() {
        List<RuleEvaluationResult> evaluationResult = mock(List.class);
        when(evaluationResult.size()).thenReturn(2);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getApplicableRuleResults()).thenReturn(evaluationResult);

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasApplicableResults(2));
    }

    @Test
    public void shouldEvaluateApplicableResultsCountMatcherNoResults() {
        List<RuleEvaluationResult> evaluationResult = mock(List.class);
        when(evaluationResult.size()).thenReturn(0);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getApplicableRuleResults()).thenReturn(evaluationResult);

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasNoApplicableResults());
    }

    @Test
    public void shouldEvaluateValidationMatcher() {
        RuntimeRule rule = setupVisibilityRule();

        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getSuccess()).thenReturn(false);

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasValidationFailures(1));
    }

    @Test
    public void shouldEvaluateValidationMatcherNoFailures() {
        RuntimeRule rule = setupValidationRule();

        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getSuccess()).thenReturn(true);

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasNoValidationFailures());
    }

    @Test
    public void shouldEvaluateValidationMatcherWithExceptions() {
        RuntimeRule rule = setupValidationRule();

        AssertionPayloadResult payloadResult = mock(AssertionPayloadResult.class);
        when(payloadResult.getSuccess()).thenReturn(false);
        when(payloadResult.getException()).thenReturn(Optional.of(new Exception()));

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasNoValidationFailures());
    }

    @Test
    public void shouldEvaluateValidationMatcherNoFailuresNoResults() {
        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of());

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasNoValidationFailures());
    }

    @Test
    public void shouldEvaluateErrorsMatcher() {
        RuntimeRule rule = setupValidationRule();

        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getSuccess()).thenReturn(true);

        RuleEvaluationResult evaluationResult = setupRuleEvaluationResult(rule, payloadResult);

        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of(evaluationResult));

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldEvaluateErrorsMatcherNoResults() {
        EntryPointResult entryPointResult = mock(EntryPointResult.class);
        when(entryPointResult.getAllRuleResults()).thenReturn(List.of());

        MatcherAssert.assertThat(entryPointResult, KrakenMatchers.hasNoIgnoredRules());
    }

    private RuleEvaluationResult setupRuleEvaluationResult(RuntimeRule rule, PayloadResult payloadResult) {
        RuleInfo ruleInfo = new RuleInfo(
                rule.getName(),
                rule.getContext(),
                rule.getTargetPath(),
                rule.getPayload().getType()
        );
        return new RuleEvaluationResult(ruleInfo, payloadResult, ConditionEvaluationResult.APPLICABLE, null);
    }

    private RuntimeRule setupValidationRule() {
        return setupRule(AssertionPayload.class, PayloadType.ASSERTION);
    }

    private RuntimeRule setupDefaultRule() {
        return setupRule(DefaultValuePayload.class, PayloadType.DEFAULT);
    }

    private RuntimeRule setupVisibilityRule() {
        return setupRule(VisibilityPayload.class, PayloadType.VISIBILITY);
    }

    private RuntimeRule setupAccessibilityRule() {
        return setupRule(AccessibilityPayload.class, PayloadType.ACCESSIBILITY);
    }

    private RuntimeRule setupRule(Class<? extends Payload> payloadClassType, PayloadType payloadType) {
        Payload payload = setupPayload(payloadClassType, payloadType);

        RuntimeRule rule = mock(RuntimeRule.class);
        when(rule.getName()).thenReturn(RULE_NAME);
        when(rule.getPayload()).thenReturn(payload);

        return rule;
    }

    private Payload setupPayload(Class<? extends Payload> payloadClassType, PayloadType payloadType) {
        Payload payload = mock(payloadClassType);
        when(payload.getType()).thenReturn(payloadType);
        return payload;
    }

}
