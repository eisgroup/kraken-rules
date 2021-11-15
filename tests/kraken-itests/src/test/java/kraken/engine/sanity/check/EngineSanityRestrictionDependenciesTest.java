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

import java.util.List;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.RRCoverage;
import kraken.testproduct.domain.Vehicle;
import org.junit.Test;

import static kraken.testing.matchers.KrakenMatchers.hasApplicableResults;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static org.junit.Assert.assertThat;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.42
 */
public class EngineSanityRestrictionDependenciesTest extends SanityEngineBaseTest {

    @Test
    public void shouldValidateEvaluateRulesInOrder() {
        final Policy policy = new Policy();

        RRCoverage rc1 = new RRCoverage();
        rc1.setId("rc1");
        final Vehicle vehicle1 = new Vehicle();
        vehicle1.setId("vehicle1");
        vehicle1.setRentalCoverage(rc1);
        vehicle1.setModelYear(2020);

        RRCoverage rc2 = new RRCoverage();
        rc2.setId("rc2");
        final Vehicle vehicle2 = new Vehicle();
        vehicle2.setId("vehicle2");
        vehicle2.setRentalCoverage(rc2);
        vehicle2.setModelYear(2021);

        policy.setRiskItems(List.of(vehicle1, vehicle2));

        final EntryPointResult result1 = engine.evaluateSubtree(
                policy,
                policy,
                "ForRestrictionCache"
        );
        final EntryPointResult result2 = engine.evaluateSubtree(
                policy,
                policy.getRiskItems().get(0),
                "ForRestrictionCache"
        );
        final EntryPointResult result3 = engine.evaluateSubtree(
                policy,
                policy.getRiskItems().get(1),
                "ForRestrictionCache"
        );

        assertThat(result1, hasNoIgnoredRules());
        assertThat(result1, hasApplicableResults(1));

        assertThat(result2, hasNoIgnoredRules());
        assertThat(result2, hasApplicableResults(1));

        assertThat(result3, hasNoIgnoredRules());
        assertThat(result3, hasApplicableResults(0));
    }

}
