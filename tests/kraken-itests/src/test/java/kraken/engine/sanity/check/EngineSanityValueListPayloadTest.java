/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.engine.sanity.check;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasNoValidationFailures;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;

import javax.money.Monetary;
import javax.money.MonetaryAmountFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import kraken.utils.MockAutoPolicyBuilder;

/**
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public class EngineSanityValueListPayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteValueListPayloadWithValidData() {
        Policy policy = new MockAutoPolicyBuilder()
            .addPolicyCurrency("USD")
            .addCreatedFromPolicyRev(2)
            .addPolicyValue(Monetary.getDefaultAmountFactory()
                .setCurrency(Monetary.getCurrency("USD"))
                .setNumber(200)
                .create())
            .addValidRiskItems(1)
            .build();

        EntryPointResult result = engine.evaluate(policy, "ValueListPayload");

        assertThat(result, hasRuleResults(4));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteValueListPayloadWithInvalidData() {
        Policy policy = new MockAutoPolicyBuilder()
            .addPolicyCurrency("EUR")
            .addCreatedFromPolicyRev(16)
            .addPolicyValue(Monetary.getDefaultAmountFactory()
                .setCurrency(Monetary.getCurrency("USD"))
                .setNumber(219)
                .create())
            .addValidRiskItems(1)
            .build();

        policy.getRiskItems()
            .get(0).getRentalCoverage().setLimitAmount(BigDecimal.valueOf(999999));

        EntryPointResult result = engine.evaluate(policy, "ValueListPayload");

        assertThat(result, hasRuleResults(4));
        assertThat(result, hasValidationFailures(4));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

}
