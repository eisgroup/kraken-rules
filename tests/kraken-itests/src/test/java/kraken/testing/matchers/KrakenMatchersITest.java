/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
 */

package kraken.testing.matchers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import kraken.engine.sanity.check.SanityEngineBaseTest;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.testproduct.domain.Policy;

public class KrakenMatchersITest extends SanityEngineBaseTest {

    @Test
    public void testDefaultMatchers() {
        testDoesNotMatch(
            "default state when vehicle model year is 2020",
            KrakenRuleMatchers.hasValueChangeTo("CA")
        );
        testMatches(
            "default state when vehicle model year is 2020",
            KrakenRuleMatchers.hasNoValueChange()
        );
    }

    @Test
    public void testVisibility() {
        testMatches(
            "hide state when vehicle model year is 2020",
            KrakenRuleMatchers.isVisible()
        );
        testDoesNotMatch(
            "hide state when vehicle model year is 2020",
            KrakenRuleMatchers.isNotVisible()
        );
    }

    @Test
    public void testAccessibility() {
        testMatches(
            "disable state when vehicle model year is 2020",
            KrakenRuleMatchers.isAccessible()
        );
        testDoesNotMatch(
            "disable state when vehicle model year is 2020",
            KrakenRuleMatchers.isNotAccessible()
        );
    }

    @Test
    public void testValidationMatchers() {
        testDoesNotMatch(
            "validate state when vehicle model year is 2020",
            KrakenRuleMatchers.hasValidationFailed()
        );
        testDoesNotMatch(
            "validate state when vehicle model year is 2020",
            KrakenRuleMatchers.hasValidationSucceeded()
        );
        testDoesNotMatch(
            "validate state when vehicle model year is 2020",
            KrakenRuleMatchers.isStatusCritical()
        );
        testDoesNotMatch(
            "validate state when vehicle model year is 2020",
            KrakenRuleMatchers.isStatusWarning()
        );
        testDoesNotMatch(
            "validate state when vehicle model year is 2020",
            KrakenRuleMatchers.isStatusInfo()
        );
    }

    private void testMatches(String ruleName, Matcher<RuleEvaluationResult> matcher) {
        test(ruleName, matcher, true);
    }

    private void testDoesNotMatch(String ruleName, Matcher<RuleEvaluationResult> matcher) {
        test(ruleName, matcher, false);
    }

    private void test(String ruleName, Matcher<RuleEvaluationResult> matcher, boolean matches) {
        EntryPointResult result = engine.evaluate(new Policy(), "test ignored");
        RuleEvaluationResult<?> ruleResult = KrakenRuleMatchers.ruleResult(
            result, ruleName
        );
        boolean actualMatches = matcher.matches(ruleResult);
        StringDescription description = new StringDescription();
        matcher.describeMismatch(ruleResult, description);
        assertThat(description.toString(), containsString("Rule is ignored"));
        assertThat(actualMatches, is(matches));
    }

}
