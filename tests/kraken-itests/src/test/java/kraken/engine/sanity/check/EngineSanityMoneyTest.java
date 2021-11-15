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

import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.CreditCardInfo;
import kraken.testproduct.domain.Policy;
import kraken.utils.MockAutoPolicyBuilder;
import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EngineSanityMoneyTest extends SanityEngineBaseTest {

    @Test
    public void shouldSetDefultMoneyAmmount() {

        final Policy data = ((Policy) getDataObject());
        final String currency = "USD";
        data.setPolicyCurrency(currency);
        data.getBillingInfo().getCreditCardInfo().setCardCreditLimitAmount(Money.of(0, "USD"));
        final EntryPointResult result = engine.evaluate(data, "default-money", new EvaluationConfig("USD"));
        final MonetaryAmount limitAmmount = data.getBillingInfo().getCreditCardInfo().getCardCreditLimitAmount();

        assertThat(limitAmmount.getNumber().intValueExact(), is(2500) );
        assertThat(limitAmmount.getCurrency().getCurrencyCode(), is(currency) );
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldNotExecuteRuleDueToFailedConditionWhenEmptyData() {
        //  condition: limit > 2000
        Policy policy = new MockAutoPolicyBuilder()
                .addValidAutoPolicy()
                .build();
        final EntryPointResult result = engine.evaluate(policy, "assert-money", new EvaluationConfig("USD"));
        assertThat(result, hasNoApplicableResults());
        assertThat(result, ignoredRuleCountIs(1));
    }

    @Test
    public void shouldNotExecuteRuleDueToFailedCondition() {
        //  condition: limit > 2000
        final EntryPointResult result = engine.evaluate(policyWithCCLimitAmmount(1500), "assert-money", new EvaluationConfig("USD"));
        assertThat(result, hasNoApplicableResults());
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteMoneyAssertionRule() {
        //  condition: limit > 2000
        //  assertion: limit > 3000
        final EntryPointResult result = engine.evaluate(policyWithCCLimitAmmount(2500), "assert-money", new EvaluationConfig("USD"));
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, hasNoIgnoredRules());
    }

    private Policy policyWithCCLimitAmmount(Integer ammount) {
            MonetaryAmount limitAmmount = Monetary.getDefaultAmountFactory()
                    .setCurrency(Monetary.getCurrency("USD"))
                    .setNumber(ammount)
                    .create();
            return new MockAutoPolicyBuilder()
                    .addValidAutoPolicy()
                    .addCreditCardInfo(new CreditCardInfo(
                            "",
                            "",
                            1,
                            null,
                            limitAmmount))
                    .build();
        }

}
