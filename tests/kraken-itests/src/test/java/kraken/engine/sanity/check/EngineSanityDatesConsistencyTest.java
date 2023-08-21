/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.engine.sanity.check;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import kraken.runtime.engine.EntryPointResult;
import kraken.testing.matchers.KrakenMatchers;
import kraken.testproduct.domain.Policy;

public class EngineSanityDatesConsistencyTest extends SanityEngineBaseTest {
    @Test
    public void shouldValidate_Date_time_creation_time_must_be_consistent() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date time creation time must be consistent");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_creation_time_must_be_consistent() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date creation time must be consistent");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_DateTime_conversion_is_consistent() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "DateTime conversion is consistent");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_conversion_is_consistent() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date conversion is consistent");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_conversion_is_consistent_PlusYears_Months_Days() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date conversion is consistent PlusYears_Months_Days");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_conversion_is_consistent_WithYear() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date conversion is consistent WithYear");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_conversion_is_consistent_WithMonth() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date conversion is consistent WithMonth");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_conversion_is_consistent_WithDay() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date conversion is consistent WithDay");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Now_conversion() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Now conversion");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Date_Getters() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Date Getters");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
    }

    @Test
    public void shouldValidate_Create_date_with_string_format() {
        var policy = new Policy();
         EntryPointResult results = engine.evaluate(policy, "Create date with string format");
        assertThat(results, KrakenMatchers.hasRuleResults(1));
        assertThat(results, KrakenMatchers.hasApplicableResults(1));
        assertThat(results, KrakenMatchers.hasNoIgnoredRules());
        assertThat(results, KrakenMatchers.hasNoValidationFailures());
    }
}
