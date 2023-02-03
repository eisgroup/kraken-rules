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
import kraken.runtime.engine.result.reducers.validation.ValidationResult;
import kraken.testproduct.domain.*;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

/**
 * This test is sanity check test, which results must match ts-engine test results.
 * Tests on both engines should match same asserts.
 *
 * @author psurinin
 * @author avasiliauskas
 * @since 1.0
 */
public final class EngineSanityAssertionPayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteAssertionAutoPolicyEntryPointWithValidData() {
        Policy policy = new MockAutoPolicyBuilder().addValidAutoPolicyWithMockDateTime().build();
        final EntryPointResult result = engine.evaluate(policy, "AssertionAutoPolicy");

        assertThat(result, hasRuleResults(6));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteAssertionAutoPolicyEntryPointWithEmptyData() {
        Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        final EntryPointResult result = engine.evaluate(policy, "AssertionAutoPolicy");

        assertThat(result.getAllRuleResults(), hasSize(4)); 
        assertThat(result, hasValidationFailures(3));
        assertThat(result, ignoredRuleCountIs(1));
    }

    @Test
    public void shouldExecuteAssertionAutoPolicyEntryPointWithNotValidData() {
        final PersonInfo personInfo = new PersonInfo("Antanas", "Antanas", new AddressInfo());
        Policy policy = new MockAutoPolicyBuilder()
                .addTermDetails(new TermDetails(101))
                .addBillingInfo(new BillingInfo())
                .addRiskItems(Collections.singletonList(new Vehicle()))
                .addParty(new Party(personInfo))
                .build();

        final EntryPointResult result = engine.evaluate(policy, "AssertionAutoPolicy");
        final List<ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(result.getAllRuleResults(), hasSize(4)); 
        assertThat(errorResults, containsInAnyOrder(
                hasProperty("ruleName", is("R0155")),
                hasProperty("ruleName", is("R0150")),
                hasProperty("ruleName", is("R0051")),
                hasProperty("ruleName", is("R0073"))
        ));
        assertThat(result, hasValidationFailures(4));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteAssertionAutoPolicyEntrypointWithNotValidDataWithRestriction() {
        final Vehicle vehicle = new Vehicle();
        vehicle.setId("-1");

        final PersonInfo personInfo = new PersonInfo("Antanas", "Antanas", new AddressInfo());
        Policy policy = new MockAutoPolicyBuilder()
                .addTermDetails(new TermDetails(101))
                .addBillingInfo(new BillingInfo())
                .addRiskItems(Collections.singletonList(vehicle))
                .addParty(new Party(personInfo))
                .build();
        final EntryPointResult result = engine.evaluateSubtree(
                policy,
                policy.getRiskItems().get(0),
                "AssertionAutoPolicy"
        );
        assertThat(result.getAllRuleResults(), hasSize(1));
        assertThat(result.getAllRuleResults().get(0).getRuleInfo().getContext(), is("Vehicle"));

        assertThat(result, hasValidationFailures(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldEvaluateAssertionAutoPolicyWithNodeThatHasNoRule() {
        Policy policy = new MockAutoPolicyBuilder().addValidAutoPolicyWithMockDateTime().build();
        Insured insured = new Insured();
        policy.setInsured(insured);
        final EntryPointResult result = engine.evaluateSubtree(policy, insured, "AssertionAutoPolicy");

        assertThat(result.getAllRuleResults(), hasSize(0)); 
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }
}
