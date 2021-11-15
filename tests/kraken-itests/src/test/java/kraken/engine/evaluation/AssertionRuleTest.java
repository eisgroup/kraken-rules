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

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.PersonInfo;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

/**
 * @author rzukaitis
 * @author avasiliauskas
 * @since 1.0
 */
public final class AssertionRuleTest extends EvaluationEngineBaseTest {
    private static final String ENTRY_POINT_NAME = "FirstNameNotEqualToLastName";
    private static final String ASSERTION_RULE_MESSAGE = "Value didn't match asserton: firstName != lastName";

    private Policy getAutoPolicySummary(String firstName, String lastName) {
        PersonInfo personInfo = new PersonInfo();
        personInfo.setFirstName(firstName);
        personInfo.setLastName(lastName);

        Party party = new Party("1");
        party.setPersonInfo(personInfo);

        Policy policy = new Policy();
        policy.setParties(Collections.singletonList(party));

        return policy;
    }

    @Test
    public void ruleEngineShouldIdentifyThatFieldValuesCannotBeTheSameIfTheyAreBothNull() {
        List<RuleEvaluationResult> ruleEvaluationResults = engine
                .evaluate(getAutoPolicySummary(null, null), ENTRY_POINT_NAME)
                .getApplicableRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));

        ValidationPayloadResult validationPayloadResult =
                (ValidationPayloadResult) ruleEvaluationResults.get(0).getPayloadResult();
        assertThat(validationPayloadResult.getMessage(), is(equalTo(ASSERTION_RULE_MESSAGE)));
    }

    @Test
    public void ruleEngineShouldIdentifyThatFieldValuesCannotBeTheSameIfTheyAreBothTheSame() {
        String name = "Jonas";
        List<RuleEvaluationResult> ruleEvaluationResults = engine
                .evaluate(getAutoPolicySummary(name, name), ENTRY_POINT_NAME)
                .getApplicableRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));

        ValidationPayloadResult validationPayloadResult =
                (ValidationPayloadResult) ruleEvaluationResults.get(0).getPayloadResult();
        assertThat(validationPayloadResult.getMessage(), is(equalTo(ASSERTION_RULE_MESSAGE)));
    }

    @Test
    public void ruleEngineShouldIdentifyThatFieldValuesAreNotTheSameIfJustOneOfThemIsEmpty() {
        final EntryPointResult result = engine.evaluate(getAutoPolicySummary("Jonas", ""), ENTRY_POINT_NAME);
        assertThat(result, hasNoValidationFailures());
    }

    @Test
    public void ruleEngineShouldIdentifyThatFieldValuesAreNotTheSameIfTheyAreDifferent() {
        final EntryPointResult result = engine
                .evaluate(getAutoPolicySummary("Jonas", "Petras"), ENTRY_POINT_NAME);
        assertThat(result, hasNoValidationFailures());
    }
}
