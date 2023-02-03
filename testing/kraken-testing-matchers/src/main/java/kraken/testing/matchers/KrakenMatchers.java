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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import kraken.model.derive.DefaultValuePayload;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationStatus;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Utility class containing kraken matchers.
 *
 * @author psurinin
 * @since 1.0.38
 */
public class KrakenMatchers {

    /**
     * Checks {@link EntryPointResult} number of value change events.
     */
    public static Matcher<EntryPointResult> hasValueChangeEvents(int number) {
        return ResultsValueChangeEventsCountMatcher.hasEvents(number);
    }

    /**
     * Checks {@link EntryPointResult} number of disabled fields.
     */
    public static Matcher<EntryPointResult> hasDisabledFields(int numberOfDisabledFields) {
        return ResultsDisabledFieldsCountMatcher.hasDisabledFields(numberOfDisabledFields);
    }

    /**
     * Checks {@link EntryPointResult} number of disabled fields.
     */
    public static Matcher<EntryPointResult> hasHiddenFields(int numberOfHiddenFields) {
        return ResultsHiddenFieldsCountMatcher.hasHiddenFields(numberOfHiddenFields);
    }

    /**
     * Checks {@link EntryPointResult} number of all rule results evaluated.
     */
    public static  Matcher<EntryPointResult> hasRuleResults(int numberOfResults) {
        return ResultsCountMatcher.hasRuleResults(numberOfResults);
    }

    /**
     * Checks {@link EntryPointResult} number of applicable rule results evaluated.
     */
    public static  Matcher<EntryPointResult> hasApplicableResults(int numberOfResults) {
        return ApplicableResultsCountMatcher.hasRuleResults(numberOfResults);
    }

    /**
     * Checks {@link EntryPointResult} has no applicable rule results.
     */
    public static  Matcher<EntryPointResult> hasNoApplicableResults() {
        return ApplicableResultsCountMatcher.hasRuleResults(0);
    }

    /**
     * Checks {@link EntryPointResult} not to contains expression errors
     */
    public static Matcher<EntryPointResult> hasValidationFailures(int number) {
        return ValidationMatcher.hasValidationFailures(number);
    }

    /**
     * Checks {@link EntryPointResult} not to contains expression errors
     */
    public static Matcher<EntryPointResult> hasNoValidationFailures() {
        return ValidationMatcher.hasNoValidationFailures();
    }

    /**
     * Checks {@link EntryPointResult} not to contains ignored rules due to
     * expression errors.
     */
    public static Matcher<EntryPointResult> hasNoIgnoredRules() {
        return ErrorsMatcher.hasNoErrors();
    }

    /**
     * Checks {@link EntryPointResult} to contain number of ignored rules
     * due to expression errors.
     */
    public static Matcher<EntryPointResult> ignoredRuleCountIs(int number) {
        return ErrorsMatcher.hasErrors(number);
    }

    private static class ResultsValueChangeEventsCountMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        private final int numberOfEvents;

        public ResultsValueChangeEventsCountMatcher(int numberOfResults) {
            this.numberOfEvents = numberOfResults;
        }

        public static Matcher<EntryPointResult> hasEvents(int number) {
            return new ResultsValueChangeEventsCountMatcher<>(number);
        }

        @Override
        protected boolean matchesSafely(T results, Description mismatchDescription) {
            long count = results.getAllRuleResults().stream()
                .filter(rr -> rr.getRuleEvaluationStatus() == RuleEvaluationStatus.APPLIED)
                .filter(ruleEvaluationResult -> ruleEvaluationResult.getPayloadResult() instanceof DefaultValuePayloadResult)
                .mapToLong(rer -> ((DefaultValuePayloadResult) rer.getPayloadResult()).getEvents().size())
                .sum();
            mismatchDescription.appendText("EntryPointResult has " + count + " default value change events");
            return count == numberOfEvents;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("EntryPointResult to have " + numberOfEvents + " rule results");
        }
    }

    private static class ResultsDisabledFieldsCountMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static Matcher<EntryPointResult> hasDisabledFields(int numberOfResults) {
            return new ResultsDisabledFieldsCountMatcher<>(numberOfResults);
        }

        private final int numberOfFields;

        public ResultsDisabledFieldsCountMatcher(int numberOfResults) {
            this.numberOfFields = numberOfResults;
        }

        @Override
        protected boolean matchesSafely(T results, Description mismatchDescription) {
            Predicate<RuleEvaluationResult> isAccessible =
                    rer -> !((AccessibilityPayloadResult) rer.getPayloadResult()).getAccessible();
            long count = results.getAllRuleResults().stream()
                .filter(rr -> rr.getRuleEvaluationStatus() == RuleEvaluationStatus.APPLIED)
                .filter(ruleEvaluationResult -> ruleEvaluationResult.getPayloadResult() instanceof AccessibilityPayloadResult)
                .filter(isAccessible)
                .count();
            mismatchDescription.appendText("EntryPointResult has " + count + " disabled fields");
            return count == numberOfFields;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("EntryPointResult to have " + numberOfFields + " rule results");
        }
    }

    private static class ResultsHiddenFieldsCountMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static Matcher<EntryPointResult> hasHiddenFields(int number) {
            return new ResultsHiddenFieldsCountMatcher<>(number);
        }

        private final int numberOfFields;

        public ResultsHiddenFieldsCountMatcher(int numberOfResults) {
            this.numberOfFields = numberOfResults;
        }

        @Override
        protected boolean matchesSafely(T results, Description mismatchDescription) {
            Predicate<RuleEvaluationResult> isVisible =
                    rer -> !((VisibilityPayloadResult) rer.getPayloadResult()).getVisible();
            long count = results.getAllRuleResults().stream()
                    .filter(ruleEvaluationResult -> ruleEvaluationResult.getPayloadResult() instanceof VisibilityPayloadResult)
                    .filter(isVisible)
                    .count();
            mismatchDescription.appendText("EntryPointResult has " + count + " hidden fields");
            return count == numberOfFields;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("EntryPointResult to have " + numberOfFields + " rule results");
        }
    }

    private static class ApplicableResultsCountMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static Matcher<EntryPointResult> hasRuleResults(int numberOfResults) {
            return new ApplicableResultsCountMatcher<>(numberOfResults);
        }

        private final int numberOfResults;

        public ApplicableResultsCountMatcher(int numberOfResults) {
            this.numberOfResults = numberOfResults;
        }

        @Override
        protected boolean matchesSafely(T results, Description mismatchDescription) {
            long count = results.getApplicableRuleResults().size();
            mismatchDescription.appendText("EntryPointResult has " + count + " applicable rule results");
            return count == numberOfResults;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("EntryPointResult to have " + numberOfResults + " rule results");
        }
    }

    private static class ResultsCountMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static Matcher<EntryPointResult> hasRuleResults(int numberOfResults) {
            return new ResultsCountMatcher<>(numberOfResults);
        }

        private final int numberOfResults;

        ResultsCountMatcher(int numberOfResults) {
            this.numberOfResults = numberOfResults;
        }

        @Override
        protected boolean matchesSafely(T results, Description mismatchDescription) {
            mismatchDescription.appendText("EntryPointResult has " + results.getAllRuleResults().size() + " rule results");
            return results.getAllRuleResults().size() == numberOfResults;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("EntryPointResult to have " + numberOfResults + " rule results");
        }
    }

    private static class ValidationMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static Matcher<EntryPointResult> hasNoValidationFailures() {
            return new ValidationMatcher<>();
        }

        static Matcher<EntryPointResult> hasValidationFailures(int number) {
            return new ValidationMatcher<>(number);
        }

        private int numberOfFailures;

        ValidationMatcher(int numberOfFailures) {
            this.numberOfFailures = numberOfFailures;
        }

        ValidationMatcher() {
        }

        @Override
        protected boolean matchesSafely(T item, Description mismatchDescription) {
            final List<RuleEvaluationResult> failed = failed(item);

            if (failed.size() != numberOfFailures) {
                Consumer<RuleEvaluationResult> addFailures = ruleEvaluationResult -> {
                    mismatchDescription.appendText(String.format(
                            "\nRule '%s' failed severity: %s",
                            ruleEvaluationResult.getRuleInfo().getRuleName(),
                            ((ValidationPayloadResult) ruleEvaluationResult.getPayloadResult()).getValidationSeverity()
                    ));
                };
                mismatchDescription.appendText("Results has " + failed.size() + " validation failures");
                failed.forEach(addFailures);

                return false;
            }

            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Results has " + numberOfFailures + " validation failures");
        }

    }

    private static class ErrorsMatcher<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static ErrorsMatcher<EntryPointResult> hasErrors(int number) {
            return new ErrorsMatcher<>(number);
        }

        static ErrorsMatcher<EntryPointResult> hasNoErrors() {
            return new ErrorsMatcher<>();
        }

        private int numberOfErrors;

        ErrorsMatcher(Integer numberOfErrors) {
            this.numberOfErrors = numberOfErrors;
        }

        ErrorsMatcher() {
        }

        @Override
        protected boolean matchesSafely(T item, Description mismatchDescription) {
            final List<RuleEvaluationResult> failed = errored(item);
            if (failed.size() != numberOfErrors) {
                mismatchDescription.appendText("Results has " + failed.size() + " expression errors");
                failed.forEach(addErrorMessage(mismatchDescription));
                return false;
            }
            return true;
        }

        private Consumer<RuleEvaluationResult> addErrorMessage(Description mismatchDescription) {
            return rer -> {
                final boolean isDefault =
                        rer.getPayloadResult() instanceof DefaultValuePayloadResult;
                final boolean isErrorOnDefault =
                        isDefault && ((DefaultValuePayloadResult) rer.getPayloadResult()).getException().isPresent();

                if (isDefault && isErrorOnDefault) {
                    mismatchDescription.appendText(String.format(
                            "\n\n\t\tRule '%s' in %s expression thrown error %s",
                            rer.getRuleInfo().getRuleName(),
                            DefaultValuePayload.class.getSimpleName(),
                            ((DefaultValuePayloadResult) rer.getPayloadResult()).getException().get()
                    ));
                }

                final boolean isAssert =
                        rer.getPayloadResult() instanceof AssertionPayloadResult;
                final boolean isErrorOnAssert =
                        isAssert && ((AssertionPayloadResult) rer.getPayloadResult()).getException().isPresent();

                if (isErrorOnAssert) {
                    mismatchDescription.appendText(String.format(
                            "\n\t\tRule '%s' in %s expression thrown error %s",
                            rer.getRuleInfo().getRuleName(),
                            DefaultValuePayload.class.getSimpleName(),
                            ((AssertionPayloadResult) rer.getPayloadResult()).getException().get()
                    ));
                }

                final boolean isErrorOnCondition = Objects.nonNull(rer.getConditionEvaluationResult().getError());

                if (isErrorOnCondition) {
                    mismatchDescription.appendText(String.format(
                            "\n\t\tRule '%s' in conditional expression thrown error %s",
                            rer.getRuleInfo().getRuleName(),
                            rer.getConditionEvaluationResult().getError()
                    ));
                }
            };
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Results has " + numberOfErrors + " errors");
        }
    }

    private static List<RuleEvaluationResult> failed(EntryPointResult result) {
        return result.getAllRuleResults().stream()
            .filter(rr -> rr.getRuleEvaluationStatus() == RuleEvaluationStatus.APPLIED)
            .filter(rr -> rr.getPayloadResult() instanceof ValidationPayloadResult)
            .filter(rr -> Boolean.FALSE.equals(((ValidationPayloadResult) rr.getPayloadResult()).getSuccess()))
            .collect(Collectors.toList());
    }

    private static List<RuleEvaluationResult> errored(EntryPointResult result) {
        return result.getAllRuleResults().stream()
            .filter(r -> r.getRuleEvaluationStatus() == RuleEvaluationStatus.IGNORED)
            .collect(Collectors.toList());
    }

}
