/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;

import kraken.testproduct.domain.AddressInfo;
import kraken.testproduct.domain.BillingAddress;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Vehicle;

/**
 * @author Mindaugas Ulevicius
 */
public class EngineSanityForbiddenFieldTest extends SanityEngineBaseTest {

    @Test
    public void shouldApplyRuleOnFieldNotForbiddenAsTarget() {
        var addressInfo = new AddressInfo();
        var vehicle = new Vehicle();
        vehicle.setAddressInfo(addressInfo);
        var policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        var result = engine.evaluate(policy, "ForbiddenField");

        assertThat(result, hasValidationFailures(1));
    }

    @Test
    public void shouldNotApplyRuleOnFieldForbiddenAsTarget() {
        var addressInfo = new BillingAddress();
        var vehicle = new Vehicle();
        vehicle.setAddressInfo(addressInfo);
        var policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        var result = engine.evaluate(policy, "ForbiddenField");

        assertThat(result, hasValidationFailures(0));
    }
}
