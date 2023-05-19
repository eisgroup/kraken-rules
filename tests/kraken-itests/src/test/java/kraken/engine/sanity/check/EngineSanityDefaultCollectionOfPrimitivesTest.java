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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import org.javamoney.moneta.Money;
import org.junit.Test;

import kraken.testproduct.domain.Insured;
import kraken.testproduct.domain.Policy;

/**
 * @author Mindaugas Ulevicius
 */
public final class EngineSanityDefaultCollectionOfPrimitivesTest extends SanityEngineBaseTest {

    @Test
    public void shouldDefaultCollectionOfPrimitivesAndCoerceCollectionAndNumberTypes() {
        Policy policy = new Policy();
        policy.setState("US");
        policy.setPolicyValue(Money.of(5.5, "USD"));
        policy.setInsured(new Insured());

        engine.evaluate(policy, "DefaultCollectionOfPrimitives");

        assertThat(policy.getPolicies(), hasItems("US", "A", "B"));
        assertThat(policy.getInsured().getChildrenAges(), hasItems(1, 10, 5));
    }

}
