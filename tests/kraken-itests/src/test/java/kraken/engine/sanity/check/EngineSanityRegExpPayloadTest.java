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
import kraken.testproduct.domain.*;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

public final class EngineSanityRegExpPayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteRegExpAutoPolicyEntryPointWithEmptyData() {
        Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        final EntryPointResult result = engine.evaluate(policy, "RegExpAutoPolicy");

        assertThat(result, hasApplicableResults(5));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteRegExpAutoPolicyEntryPointWithValidData() {
        Policy policy = new MockAutoPolicyBuilder()
                .addValidAutoPolicyWithMockDateTime()
                .build();

        final EntryPointResult result = engine.evaluate(policy, "RegExpAutoPolicy");

        assertThat(result, hasApplicableResults(11));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteRegExpAutoPolicyEntryPointWithNotValidData() {
        final BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(new CreditCardInfo("MasterCard", "1545464464454545", 132654, null, null));
        final Party party = new Party("1");
        final PersonInfo personInfo = new PersonInfo();
        personInfo.setAddressInfo(new AddressInfo(
                null, null, "123456", "654321"));
        party.setPersonInfo(personInfo);
        Policy policy = new MockAutoPolicyBuilder()
                .addBillingInfo(billingInfo)
                .addPolicyNumber("Not valid")
                .addState("654321")
                .addParty(party)
                .build();

        final EntryPointResult result = engine.evaluate(policy, "RegExpAutoPolicy");

        assertThat(result, hasApplicableResults(7));
        assertThat(result, hasValidationFailures(6));
        assertThat(result, matchesSnapshot());
    }
}
