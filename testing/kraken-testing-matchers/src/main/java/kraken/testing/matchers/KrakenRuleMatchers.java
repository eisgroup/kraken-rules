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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.money.MonetaryAmount;

import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.events.RuleEvent;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.ExceptionAwarePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Utility class containing kraken rule matchers.
 *
 * @author psurinin
 * @since 1.0.38
 */
public class KrakenRuleMatchers {

    private static class Converters {
        public static final Function<Object, Object> DATETIME_TO_DATE = dateTime -> ((LocalDateTime) dateTime).toLocalDate();
        public static final Function<Object, Object> MONEY_TO_NUMBER = money -> ((MonetaryAmount) money).getNumber().numberValue(BigDecimal.class);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code VisibilityPayload} and visibility payload result is true.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isVisible())</pre>
     */
    public static Matcher<RuleEvaluationResult> isVisible() {
        return new VisibilityMatcher<>(true);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code VisibilityPayload} and visibility payload result is false.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isNotVisible())</pre>
     */
    public static Matcher<RuleEvaluationResult> isNotVisible() {
        return new VisibilityMatcher<>(false);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code AccessibilityPayloadResult} and field is accessible (not disabled).
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isAccessible())</pre>
     */
    public static Matcher<RuleEvaluationResult> isAccessible() {
        return new AccessibilityMatcher<>(true);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code AccessibilityPayloadResult} and field is not accessible (disabled).
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isNotAccessible())</pre>
     */
    public static Matcher<RuleEvaluationResult> isNotAccessible() {
        return new AccessibilityMatcher<>(false);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code ValidationPayloadResult} and rule validation was successful.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, hasValidationSucceeded())</pre>
     */
    public static Matcher<RuleEvaluationResult> hasValidationSucceeded() {
        return new ValidationMatcher<>(true);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code ValidationPayloadResult} and rule validation was failed.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, hasValidationFailed())</pre>
     */
    public static Matcher<RuleEvaluationResult> hasValidationFailed() {
        return new ValidationMatcher<>(false);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code ValidationPayloadResult} and payload severity level is info.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isStatusInfo())</pre>
     */
    public static Matcher<RuleEvaluationResult> isStatusInfo() {
        return new ValidationStatusMatcher<>(ValidationSeverity.info);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code ValidationPayloadResult} and payload severity level is warning.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isStatusWarning())</pre>
     */
    public static Matcher<RuleEvaluationResult> isStatusWarning() {
        return new ValidationStatusMatcher<>(ValidationSeverity.warning);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code ValidationPayloadResult} and payload severity level is critical.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, isStatusCritical())</pre>
     */
    public static Matcher<RuleEvaluationResult> isStatusCritical() {
        return new ValidationStatusMatcher<>(ValidationSeverity.critical);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result has a rule
     * with target path equal to given value.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, forAttribute(attr))</pre>
     */
    public static Matcher<RuleEvaluationResult> forAttribute(String attributeName) {
        return new AttributeMatcher<>(attributeName);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code DefaultValuePayloadResult} and contains value change event with newValue
     * equal to given value.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, hasValueChangeTo(obj))</pre>
     */
    public static Matcher<RuleEvaluationResult> hasValueChangeTo(Object newValue) {
        return new ValueMatcher<>(newValue);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code DefaultValuePayloadResult} and contains value change event with newValue
     * equal to given value. Can be used for LocalDateTime values stored in event as it
     * converts LocalDateTime to LocalDate before comparing.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, hasValueChangeToDateTime(dateObj))</pre>
     */
    public static Matcher<RuleEvaluationResult> hasValueChangeToDateTime(LocalDate newValue) {
        return new ValueMatcher<>(newValue, Converters.DATETIME_TO_DATE);
    }

    /**
     * Creates a matcher that matches if rule evaluation payload result is an instance of
     * {@code DefaultValuePayloadResult} and contains value change event with newValue
     * equal to given value.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, hasValueChangeToMoneyOf(numberObj))</pre>
     */
    public static Matcher<RuleEvaluationResult> hasValueChangeToMoneyOf(BigDecimal newValue) {
        return new ValueMatcher<>(newValue, Converters.MONEY_TO_NUMBER);
    }

    /**
     * Creates a matcher that matches if no value change was triggered as a result of
     * rule evaluation.
     * <p/>
     * For example:
     * <pre>assertThat(evaluation, hasNoValueChange())</pre>
     */
    public static Matcher<RuleEvaluationResult> hasNoValueChange() {
        return new NoValueChangeMatcher<>();
    }

    public static RuleEvaluationResult ruleResult(EntryPointResult result, String ruleName) {
        return result.getAllRuleResults().stream()
                .filter(r -> r.getRuleInfo().getRuleName().equals(ruleName))
                .findFirst()
                .orElseThrow((() -> new RuntimeException(String.format(
                        "Failed to find rule '%s' in entry point result.",
                        ruleName
                ))));
    }

    public static Collection<RuleEvaluationResult> ruleResults(EntryPointResult result, String ruleName) {
        return result.getAllRuleResults().stream()
                .filter(r -> r.getRuleInfo().getRuleName().equals(ruleName))
                .collect(Collectors.toList());
    }

    private static class VisibilityMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        private final boolean isVisible;

        private VisibilityMatcher(boolean isVisible) {
            this.isVisible = isVisible;
        }

        @Override
        protected boolean matchesSafely(T result, Description mismatchDescription) {
            if (result.getPayloadResult() instanceof VisibilityPayloadResult) {
                boolean isEvaluatedVisibility = result.getPayloadResult() == null
                        || ((VisibilityPayloadResult) result.getPayloadResult()).getVisible();

                mismatchDescription.appendText(String.format(
                        "Rule '%s' is evaluated visibility to '%s', but expected '%s'",
                        result.getRuleInfo().getRuleName(),
                        isEvaluatedVisibility,
                        isVisible
                ));

                return isVisible == isEvaluatedVisibility;
            }

            return isVisible;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Rule to set visibility to '" + isVisible + "'.");
        }
    }

    private static class AccessibilityMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        private final boolean isAccessible;

        private AccessibilityMatcher(boolean isAccessible) {
            this.isAccessible = isAccessible;
        }

        @Override
        protected boolean matchesSafely(T result, Description mismatchDescription) {
            if (result.getPayloadResult() instanceof AccessibilityPayloadResult) {
                boolean isEvaluatedAccessible = result.getPayloadResult() == null
                        || ((AccessibilityPayloadResult) result.getPayloadResult()).getAccessible();

                mismatchDescription.appendText(String.format(
                        "Rule '%s' is evaluated accessibility to '%s', but expected '%s'",
                        result.getRuleInfo().getRuleName(),
                        isEvaluatedAccessible,
                        isAccessible
                ));

                return isAccessible == isEvaluatedAccessible;
            }

            return isAccessible;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Rule to set accessibility to '" + isAccessible + "'.");
        }

    }

    private static class ValidationMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        private final boolean isSuccess;

        private ValidationMatcher(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        @Override
        protected boolean matchesSafely(T result, Description mismatchDescription) {
            if (result.getPayloadResult() instanceof ValidationPayloadResult) {
                var payloadResult = (ValidationPayloadResult) result.getPayloadResult();

                boolean isEvaluatedSuccess = payloadResult == null
                        || Boolean.TRUE.equals(payloadResult.getSuccess())
                        || payloadResult instanceof ExceptionAwarePayloadResult && ((ExceptionAwarePayloadResult) payloadResult).getException().isPresent();

                String message = result.getPayloadResult() != null ?
                        ((ValidationPayloadResult) result.getPayloadResult()).getMessage() : "";

                if (isSuccess) {
                    mismatchDescription.appendText(String.format(
                            "Rule '%s' validation has failed, but expected to be validated successfully, error message is: "
                                    + System.lineSeparator()
                                    + "\t\t'%s'",
                            result.getRuleInfo().getRuleName(),
                            message
                    ));
                } else {
                    mismatchDescription.appendText(String.format(
                            "Rule '%s' validation result is success, but expected to fail",
                            result.getRuleInfo().getRuleName()
                    ));
                }

                return isSuccess == isEvaluatedSuccess;
            }

            return isSuccess;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Rule to set validation result to '" + isSuccess + "'.");
        }

    }

    private static class ValidationStatusMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        private final ValidationSeverity validationSeverity;

        private ValidationStatusMatcher(ValidationSeverity validationSeverity) {
            this.validationSeverity = validationSeverity;
        }

        @Override
        protected boolean matchesSafely(T result, Description mismatchDescription) {
            return Optional.ofNullable(result.getPayloadResult())
                    .filter(ValidationPayloadResult.class::isInstance)
                    .map(ValidationPayloadResult.class::cast)
                    .map(v -> {
                        final boolean isSeverityMatch = v.getValidationSeverity().equals(validationSeverity);

                        if (!isSeverityMatch) {
                            mismatchDescription.appendText(String.format(
                                    "Rule '%s' payload validation severity is '%s', but expected '%s'",
                                    result.getRuleInfo().getRuleName(),
                                    v.getValidationSeverity(),
                                    validationSeverity
                            ));
                        }

                        return isSeverityMatch;
                    })
                    .orElseGet(() -> {
                        mismatchDescription.appendText("The rule was not executed, maybe is missing some Payload information'");

                        return false;
                    });
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Rule to check the result payload validation status");
        }
    }

    private static class AttributeMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        private final String attributeName;

        private AttributeMatcher(String attributeName) {
            this.attributeName = attributeName;
        }

        @Override
        protected boolean matchesSafely(T result, Description mismatchDescription) {
            mismatchDescription.appendText(String.format(
                    "Rule '%s' target path is set to '%s', but expected '%s'",
                    result.getRuleInfo().getRuleName(),
                    result.getRuleInfo().getTargetPath(),
                    attributeName
            ));

            return result.getRuleInfo().getTargetPath().equals(attributeName);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Rule targetPath must be '" + attributeName + "'.");
        }
    }

    private static class NoValueChangeMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        @Override
        protected boolean matchesSafely(T item, Description mismatchDescription) {
            return Optional.ofNullable(item.getPayloadResult())
                    .filter(payloadResult -> payloadResult instanceof DefaultValuePayloadResult)
                    .map(DefaultValuePayloadResult.class::cast)
                    .map(payloadResult -> {
                        var noChangeEvents = payloadResult.getEvents().stream()
                                .noneMatch(ruleEvent -> ruleEvent instanceof ValueChangedEvent);
                        var hasExceptions = ((ExceptionAwarePayloadResult) payloadResult).getException().isPresent();

                        mismatchDescription.appendText(String.format(
                                "Rule '%s' triggered Default value change, but expected no default value change",
                                item.getRuleInfo().getRuleName()));

                        return hasExceptions || noChangeEvents;
                    }).orElse(true);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Payload evaluation must have no value changes.");
        }

    }

    private static class ValueMatcher<T extends RuleEvaluationResult> extends TypeSafeDiagnosingMatcher<T> {

        private final Object newValue;
        private final Optional<Function<Object, Object>> transformationFunction;

        private ValueMatcher(Object newValue) {
            this.newValue = newValue;
            this.transformationFunction = Optional.empty();
        }

        private ValueMatcher(Object newValue, Function<Object, Object> transformationFunction) {
            this.newValue = newValue;
            this.transformationFunction = Optional.ofNullable(transformationFunction);
        }

        @Override
        protected boolean matchesSafely(T result, Description mismatchDescription) {
            if (result.getPayloadResult() != null) {
                if (result.getPayloadResult() instanceof DefaultValuePayloadResult
                        && ((DefaultValuePayloadResult) result.getPayloadResult()).getException().isEmpty()) {
                    Object changeValue = getChangeValue(((DefaultValuePayloadResult) result.getPayloadResult()).getEvents());
                    Object transformedValue = transformationFunction
                            .map(c -> c.apply(changeValue))
                            .orElse(changeValue);

                    mismatchDescription.appendText(String.format(
                            "Rule '%s' triggered value change to '%s', but expected '%s'",
                            result.getRuleInfo().getRuleName(),
                            transformedValue,
                            newValue
                    ));

                    return Objects.equals(transformedValue, newValue);
                }

                mismatchDescription.appendText(String.format(
                        "Rule '%s' triggered no value change. Either rule payload type is incorrect " +
                                "(expected 'DefaultValue') or payload contains exceptions.",
                        result.getRuleInfo().getRuleName()
                ));
            }

            return false;
        }

        private Object getChangeValue(List<RuleEvent> events) {
            return events.stream().findFirst()
                    .map(ValueChangedEvent.class::cast)
                    .map(ValueChangedEvent::getNewValue)
                    .orElse(null);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Default value payload evaluation must set value '" + newValue + "'.");
        }
    }

}