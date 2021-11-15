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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import kraken.model.payload.PayloadType;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.runtime.model.rule.payload.ui.AccessibilityPayload;
import kraken.runtime.model.rule.payload.ui.VisibilityPayload;
import kraken.runtime.model.rule.payload.validation.ValidationPayload;
import org.javamoney.moneta.Money;
import org.junit.Test;

import static kraken.testing.matchers.KrakenRuleMatchers.forAttribute;
import static kraken.testing.matchers.KrakenRuleMatchers.hasNoValueChange;
import static kraken.testing.matchers.KrakenRuleMatchers.hasValidationFailed;
import static kraken.testing.matchers.KrakenRuleMatchers.hasValidationSucceeded;
import static kraken.testing.matchers.KrakenRuleMatchers.hasValueChangeTo;
import static kraken.testing.matchers.KrakenRuleMatchers.hasValueChangeToDateTime;
import static kraken.testing.matchers.KrakenRuleMatchers.hasValueChangeToMoneyOf;
import static kraken.testing.matchers.KrakenRuleMatchers.isAccessible;
import static kraken.testing.matchers.KrakenRuleMatchers.isNotAccessible;
import static kraken.testing.matchers.KrakenRuleMatchers.isNotVisible;
import static kraken.testing.matchers.KrakenRuleMatchers.isStatusCritical;
import static kraken.testing.matchers.KrakenRuleMatchers.isStatusInfo;
import static kraken.testing.matchers.KrakenRuleMatchers.isStatusWarning;
import static kraken.testing.matchers.KrakenRuleMatchers.isVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@code KrakenRuleMatchers} class.
 *
 * @author Tomas Dapkunas
 * @since 1.0.38
 */
public class KrakenRuleMatchersTest {

    private static final String RULE_NAME = "rule001";
    private static final String TARGET_PATH = "targetPath";

    @Test
    public void shouldEvaluateVisibilityMatcherToTrue() {
        VisibilityPayloadResult payloadResult = mock(VisibilityPayloadResult.class);
        when(payloadResult.getVisible()).thenReturn(true);

        RuleInfo ruleInfo = rule(PayloadType.VISIBILITY);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isVisible());
    }

    @Test
    public void shouldEvaluateVisibilityMatcherToTrueNoPayload() {
        RuleInfo ruleInfo = rule(PayloadType.VISIBILITY);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, null, null, null);

        assertThat(evaluationResult, isVisible());
    }

    @Test
    public void shouldEvaluateVisibilityMatcherToFalse() {
        VisibilityPayloadResult payloadResult = mock(VisibilityPayloadResult.class);
        when(payloadResult.getVisible()).thenReturn(false);

        RuleInfo ruleInfo = rule(PayloadType.VISIBILITY);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isNotVisible());
    }

    @Test
    public void shouldEvaluateAccessibilityMatcherToTrue() {
        AccessibilityPayloadResult payloadResult = mock(AccessibilityPayloadResult.class);
        when(payloadResult.getAccessible()).thenReturn(true);

        RuleInfo ruleInfo = rule(PayloadType.ACCESSIBILITY);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isAccessible());
    }

    @Test
    public void shouldEvaluateAccessibilityMatcherToTrueNoPayload() {
        RuleInfo ruleInfo = rule(PayloadType.ACCESSIBILITY);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, null, null, null);

        assertThat(evaluationResult, isAccessible());
    }

    @Test
    public void shouldEvaluateAccessibilityMatcherToFalse() {
        AccessibilityPayloadResult payloadResult = mock(AccessibilityPayloadResult.class);
        when(payloadResult.getAccessible()).thenReturn(false);

        RuleInfo ruleInfo = rule(PayloadType.ACCESSIBILITY);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isNotAccessible());
    }

    @Test
    public void shouldEvaluateValidationMatcherToTrue() {
        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getSuccess()).thenReturn(true);

        RuleInfo ruleInfo = rule(PayloadType.ASSERTION);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasValidationSucceeded());
    }

    @Test
    public void shouldEvaluateValidationMatcherToTrueNoPayload() {
        RuleInfo ruleInfo = rule(PayloadType.ASSERTION);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, null, null, null);

        assertThat(evaluationResult, hasValidationSucceeded());
    }

    @Test
    public void shouldEvaluateValidationMatcherToFalse() {
        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getSuccess()).thenReturn(false);

        RuleInfo ruleInfo = rule(PayloadType.ASSERTION);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasValidationFailed());
    }

    @Test
    public void shouldEvaluateValidationStatusMatcherInfoLevel() {
        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getValidationSeverity()).thenReturn(ValidationSeverity.info);

        RuleInfo ruleInfo = rule(PayloadType.ASSERTION);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isStatusInfo());
    }

    @Test
    public void shouldEvaluateValidationStatusMatcherWarningLevel() {
        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getValidationSeverity()).thenReturn(ValidationSeverity.warning);

        RuleInfo ruleInfo = rule(PayloadType.ASSERTION);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isStatusWarning());
    }

    @Test
    public void shouldEvaluateValidationStatusMatcherCriticalLevel() {
        ValidationPayloadResult payloadResult = mock(ValidationPayloadResult.class);
        when(payloadResult.getValidationSeverity()).thenReturn(ValidationSeverity.critical);

        RuleInfo ruleInfo = rule(PayloadType.ASSERTION);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, isStatusCritical());
    }

    @Test
    public void shouldEvaluateAttributeMatcher() {
        RuleInfo ruleInfo = rule(PayloadType.DEFAULT);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, null, null, null);

        assertThat(evaluationResult, forAttribute(TARGET_PATH));
    }

    @Test
    public void shouldEvaluateValueMatcher() {
        String changeValue = "changedToValue";

        ValueChangedEvent valueChangedEvent = mock(ValueChangedEvent.class);
        when(valueChangedEvent.getNewValue()).thenReturn(changeValue);

        DefaultValuePayloadResult payloadResult = mock(DefaultValuePayloadResult.class);
        when(payloadResult.getException()).thenReturn(Optional.empty());
        when(payloadResult.getEvents()).thenReturn(List.of(valueChangedEvent));

        RuleInfo ruleInfo = rule(PayloadType.DEFAULT);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasValueChangeTo(changeValue));
    }

    @Test
    public void shouldEvaluateValueMatcherDateTime() {
        LocalDateTime changeValue = LocalDateTime.now();

        ValueChangedEvent valueChangedEvent = mock(ValueChangedEvent.class);
        when(valueChangedEvent.getNewValue()).thenReturn(changeValue);

        DefaultValuePayloadResult payloadResult = mock(DefaultValuePayloadResult.class);
        when(payloadResult.getException()).thenReturn(Optional.empty());
        when(payloadResult.getEvents()).thenReturn(List.of(valueChangedEvent));

        RuleInfo ruleInfo = rule(PayloadType.DEFAULT);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasValueChangeToDateTime(LocalDateTime.now().toLocalDate()));
    }

    @Test
    public void shouldEvaluateValueMatcherMoney() {
        Money changeValue = Money.of(BigDecimal.TEN, "USD");

        ValueChangedEvent valueChangedEvent = mock(ValueChangedEvent.class);
        when(valueChangedEvent.getNewValue()).thenReturn(changeValue);

        DefaultValuePayloadResult payloadResult = mock(DefaultValuePayloadResult.class);
        when(payloadResult.getException()).thenReturn(Optional.empty());
        when(payloadResult.getEvents()).thenReturn(List.of(valueChangedEvent));

        RuleInfo ruleInfo = rule(PayloadType.DEFAULT);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasValueChangeToMoneyOf(BigDecimal.TEN));
    }

    @Test
    public void shouldEvaluateNoValueChangeMatcherException() {
        ValueChangedEvent valueChangedEvent = mock(ValueChangedEvent.class);

        DefaultValuePayloadResult payloadResult = mock(DefaultValuePayloadResult.class);
        when(payloadResult.getException()).thenReturn(Optional.of(new Exception()));
        when(payloadResult.getEvents()).thenReturn(List.of(valueChangedEvent));

        RuleInfo ruleInfo = rule(PayloadType.DEFAULT);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasNoValueChange());
    }

    @Test
    public void shouldEvaluateNoValueChangeMatcher() {
        DefaultValuePayloadResult payloadResult = mock(DefaultValuePayloadResult.class);
        when(payloadResult.getException()).thenReturn(Optional.empty());
        when(payloadResult.getEvents()).thenReturn(List.of());

        RuleInfo ruleInfo = rule(PayloadType.DEFAULT);
        RuleEvaluationResult evaluationResult = new RuleEvaluationResult(ruleInfo, payloadResult, null, null);

        assertThat(evaluationResult, hasNoValueChange());
    }

    private RuleInfo rule(PayloadType payloadType) {
        return new RuleInfo(RULE_NAME, "Context", TARGET_PATH, payloadType);
    }

}
