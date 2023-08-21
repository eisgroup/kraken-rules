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
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.Test;

import kraken.runtime.EvaluationConfig;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.TransactionDetails;

/**
 * @author mulevicius
 */
public class EngineSanityDateFunctionsTest extends SanityEngineBaseTest {

    @Test
    public void shouldEvaluateDateFunctionsShouldHandleDST() {
        Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());

        EvaluationConfig evaluationConfig = new EvaluationConfig(Map.of(), "USD");
        engine.evaluate(policy, "DateFunctions-DefaultPlusDays", evaluationConfig);

        assertThat(
            policy.getTransactionDetails().getTxEffectiveDate(),
            equalTo(LocalDateTime.parse("2022-03-29T10:00:00"))
        );
    }

}
