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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNull.nullValue;

import java.math.BigDecimal;
import java.util.List;

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.testproduct.domain.COLLCoverage;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Vehicle;
import org.junit.Test;

/**
 * @author rzukaitis
 * @author avasiliauskas
 * @since 1.0
 */
public final class DefaultRuleTest extends EvaluationEngineBaseTest{
    private static final String ENTRY_POINT_NAME = "DefaultPolicyNumber";

    @Test
    public void shouldDefaultToFirstRiskItemCoverageCode() {
        String epName = "DefaultPolicyStateToVehicleState";

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleState("NY");

        Policy policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        EntryPointResult entryPointResult = engine.evaluate(policy, epName);

        List<RuleEvaluationResult> ruleEvaluationResults = entryPointResult.getAllRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));
        assertThat(policy.getState(), is(equalTo("NY")));
    }

    @Test
    public void shouldDefaultToLAWhenVehicleIsPresent() {
        String epName = "DefaultToLAWhenVehicleIsPresent";

        Vehicle vehicle = new Vehicle();

        Policy policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        EntryPointResult entryPointResult = engine.evaluate(policy, epName);

        List<RuleEvaluationResult> ruleEvaluationResults = entryPointResult.getAllRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));
        assertThat(policy.getState(), is(equalTo("LA")));
    }

    @Test
    public void shouldDefaultStateToSecondCollCoverageCode() {
        String epName = "DefaultStateToLastCollCoverageCode";

        COLLCoverage collCoverageOne = new COLLCoverage();
        collCoverageOne.setCode("collOne");

        COLLCoverage collCoverageTwo = new COLLCoverage();
        collCoverageTwo.setCode("collTwo");


        Vehicle vehicle = new Vehicle();
        vehicle.setCollCoverages(List.of(collCoverageOne, collCoverageTwo));

        Policy policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        EntryPointResult entryPointResult = engine.evaluate(policy, epName);

        List<RuleEvaluationResult> ruleEvaluationResults = entryPointResult.getAllRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));
        assertThat(policy.getState(), is(equalTo("collTwo")));
    }

    @Test
    public void shouldDefaultStateToFirstCollCoverageWithLimitAmtGreaterThanTen() {
        String epName = "DefaultStateToFirstCoverageCodeWithLimitAmountGreaterThanTen";

        COLLCoverage collCoverageOne = new COLLCoverage();
        collCoverageOne.setCode("collOne");
        collCoverageOne.setLimitAmount(BigDecimal.valueOf(11));

        COLLCoverage collCoverageTwo = new COLLCoverage();
        collCoverageTwo.setCode("collTwo");
        collCoverageTwo.setLimitAmount(BigDecimal.valueOf(9));

        COLLCoverage collCoverageThree = new COLLCoverage();
        collCoverageThree.setCode("collThree");
        collCoverageThree.setLimitAmount(BigDecimal.valueOf(12));

        Vehicle vehicle = new Vehicle();
        vehicle.setCollCoverages(List.of(collCoverageTwo, collCoverageOne, collCoverageThree));

        Policy policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        EntryPointResult entryPointResult = engine.evaluate(policy, epName);

        List<RuleEvaluationResult> ruleEvaluationResults = entryPointResult.getAllRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));
        assertThat(policy.getState(), is(equalTo("collOne")));
    }

    @Test
    public void ruleEngineShouldAssignDefaultValueToField() {
        Policy policy = new Policy();
        String defaultPolicyNumber = "Q0001";

        EntryPointResult entryPointResult = engine
                .evaluate(policy, ENTRY_POINT_NAME);
        List<RuleEvaluationResult> ruleEvaluationResults = entryPointResult.getAllRuleResults();
        assertThat(ruleEvaluationResults, hasSize(1));
        assertThat(policy.getPolicyNumber(), is(equalTo(defaultPolicyNumber)));
    }

    @Test
    public void ruleEngineShouldProvideCorrectInformationAboutTheChangedFieldValue() {
        Policy policy = new Policy();
        String defaultPolicyNumber = "Q0001";

        EntryPointResult entryPointResult = engine
                .evaluate(policy, ENTRY_POINT_NAME);
        List<RuleEvaluationResult> ruleEvaluationResults = entryPointResult.getAllRuleResults();
        DefaultValuePayloadResult defaultValuePayloadResult =
                (DefaultValuePayloadResult) ruleEvaluationResults.get(0).getPayloadResult();
        assertThat(defaultValuePayloadResult.getEvents(), hasSize(1));

        ValueChangedEvent valueChangedEvent = (ValueChangedEvent) defaultValuePayloadResult.getEvents().get(0);
        assertThat(valueChangedEvent.getPreviousValue(), is(nullValue()));
        assertThat(valueChangedEvent.getNewValue(), is(equalTo(defaultPolicyNumber)));
    }
}
