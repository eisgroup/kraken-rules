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

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.testing.matchers.KrakenRuleMatchers;
import kraken.testproduct.domain.Policy;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Function;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static kraken.testing.matchers.KrakenRuleMatchers.hasValidationFailed;
import static kraken.testing.matchers.KrakenRuleMatchers.ruleResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

public final class EngineSanityUsagePayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteUsagePayloadAutoPolicyEntryPointWithValidData() {
        final Policy policy = new MockAutoPolicyBuilder()
                .addValidAutoPolicyWithMockDateTime()
                .build();
        final EntryPointResult result = engine.evaluate(policy, "UsagePayloadAutoPolicy");

        assertThat(result, hasRuleResults(26));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }
    @Test
    public void shouldExecuteUsagePayloadAutoPolicyEntryPointWithEmptyData() {
        final Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        final EntryPointResult result = engine.evaluate(policy, "UsagePayloadAutoPolicy");

        assertThat(result, hasRuleResults(21));
        assertThat(result, hasValidationFailures(21));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteUsagePayload_must_be_empty() {
        final Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        policy.setPolicyNumber("P1");
        final EntryPointResult result = engine.evaluate(policy, "policy number must be empty");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(ruleResult(result, "policy number must be empty"), hasValidationFailed());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteOnUnknownTypeField() {
        Function<String, Policy> policy = ref -> {
            final Policy data = new Policy();
            data.setRefToCustomer(ref);
            return data;
        };

        final EntryPointResult result = engine.evaluate(policy.apply(null), "Usage-UnknownField");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, matchesSnapshot());

        final EntryPointResult resultValid = engine.evaluate(policy.apply("uri"), "Usage-UnknownField");
        assertThat(resultValid, hasNoIgnoredRules());
        assertThat(resultValid, hasRuleResults(1));
        assertThat(resultValid, hasNoValidationFailures());
    }
}
