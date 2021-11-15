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
package kraken.engine.evaluation;

import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.Arrays;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.PartyRole;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.Test;

/**
 * @author rzukaitis
 * @author avasiliauskas
 * @since 1.0
 */
public final class RulesEvaluationITest extends EvaluationEngineBaseTest{

    @Test
    public void shouldEvaluateFourRulesOnFourFields() {
        Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        String entryPointName = "EvaluateFourRulesOnFourFields";

        final EntryPointResult entryPointResult = engine.evaluate(policy, entryPointName);

        assertThat(entryPointResult.getAllRuleResults(), hasSize(4));
    }

    @Test
    public void shouldEvaluateTwoRulesOnFiveFields() {
        Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        Party party1 = new Party("1");
        Party party2 = new Party("1");
        PartyRole role1 = new PartyRole();
        PartyRole role2 = new PartyRole();
        PartyRole role3 = new PartyRole();
        party1.setRoles(Arrays.asList(role1, role2, role3));
        policy.setParties(Arrays.asList(party1, party2));
        String entryPointName = "EvaluateTwoRulesOnFiveFields";

        EntryPointResult entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasValidationFailures(5));
    }

    @Test
    public void shouldEvaluateRuleOnTwoFields(){
        Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        policy.setParties(Arrays.asList(new Party("1"), new Party("2")));
        String entryPointName = "EvaluateRuleOnTwoFields";

        EntryPointResult entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasValidationFailures(2));

    }

    @Test
    public void shouldEvaluateMandatoryRulesOnTwoNotEmptyFields(){
        Policy policy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
        Party party1 = new Party("1");
        party1.setRelationToPrimaryInsured("Related");
        Party party2 = new Party("2");
        party2.setRelationToPrimaryInsured("Not related");
        policy.setParties(Arrays.asList(party1, party2));
        String entryPointName = "EvaluateRuleOnTwoFields";

        EntryPointResult entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasRuleResults(2));
        assertThat(entryPointResult, hasNoValidationFailures());
    }
}