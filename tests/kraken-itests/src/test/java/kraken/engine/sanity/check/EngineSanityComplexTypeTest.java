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

import com.google.common.collect.ImmutableList;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.AddressInfo;
import kraken.testproduct.domain.AddressLine;
import kraken.testproduct.domain.AddressLine1;
import kraken.testproduct.domain.AddressLine2;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.CreditCardInfo;
import kraken.testproduct.domain.AnubisCoverage;
import kraken.testproduct.domain.Vehicle;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.junit.Assert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

/**
 * @author mulevicius
 */
public class EngineSanityComplexTypeTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldReferenceComplexCollectionFieldType(){
        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setAddressLine1(new AddressLine1("addressLine1"));
        addressInfo.setAddressLine2(new AddressLine2("addressLine2"));

        Vehicle vehicle = new Vehicle();
        vehicle.setAddressInfo(addressInfo);
        vehicle.setAnubisCoverages(ImmutableList.of(new AnubisCoverage(), new AnubisCoverage()));

        Policy policy = new MockAutoPolicyBuilder()
                .addCreditCardInfo(new CreditCardInfo())
                .addRiskItems(ImmutableList.of(vehicle))
                .build();

        final EntryPointResult result = engine.evaluate(policy, "complex-field-type-test");

        assertThat(result, hasRuleResults(2));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

}
