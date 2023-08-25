
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.Test;

import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;
import kraken.utils.Dates;
import kraken.utils.MockAutoPolicyBuilder;

public final class EngineSanityRawMessageParameterTest extends SanityEngineBaseTest {

    @Test
    public void shouldReturnRawMessageParameters() {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setTxEffectiveDate(Dates.convertISOToLocalDateTime("2021-01-01T11:00:00Z"));

        Policy policy = new Policy();
        policy.setPolicyValue(Money.of(10.01, "USD"));

        var result = engine.evaluate(policy, "RawTemplates");
        var ruleResult = result.getAllRuleResults().stream()
            .filter(rr -> rr.getRuleInfo().getRuleName().equals("RawTemplates_R01_Policy.state"))
            .findFirst()
            .get();
        var variables = ((ValidationPayloadResult) ruleResult.getPayloadResult()).getRawTemplateVariables();

        var classes = Arrays.asList(
            Boolean.class,
            Boolean.class,
            null,
            String.class,
            String.class,
            BigDecimal.class,
            LocalDate.class,
            LocalDateTime.class,
            String.class,
            MonetaryAmount.class
        );

        assertThat(variables, hasSize(classes.size()));
        for (int i = 0; i < classes.size(); i++) {
            if (classes.get(i) == null) {
                assertThat(variables.get(i), nullValue());
            } else {
                assertThat(variables.get(i), instanceOf(classes.get(i)));
            }
        }
    }
}
