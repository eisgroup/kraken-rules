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

import static kraken.testing.matchers.KrakenMatchers.hasNoValidationFailures;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.javamoney.moneta.Money;
import org.junit.Test;

import kraken.testproduct.domain.BillingInfo;
import kraken.testproduct.domain.CreditCardInfo;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.TermDetails;
import kraken.testproduct.domain.Vehicle;

public final class EngineSanityNumberSetPayloadTest extends SanityEngineBaseTest {

    @Test
    public void shouldEvaluateNumberSetRulesOnPolicyWithValidValues() {
        Policy policy = new Policy();
        TermDetails termDetails = new TermDetails();
        termDetails.setTermNo(1);
        policy.setTermDetails(termDetails);

        Vehicle vehicle = new Vehicle();
        vehicle.setNewValue(new BigDecimal("7999.99"));
        policy.setRiskItems(List.of(vehicle));

        BillingInfo billingInfo = new BillingInfo();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardCreditLimitAmount(Money.of(505, "USD"));
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);

        var result = engine.evaluate(policy, "NumberSet");

        assertThat(result, hasNoValidationFailures());
    }

    @Test
    public void shouldEvaluateNumberSetRulesOnPolicyWithValidNotValidValues() {
        Policy policy = new Policy();
        TermDetails termDetails = new TermDetails();
        termDetails.setTermNo(0);
        policy.setTermDetails(termDetails);

        Vehicle vehicle = new Vehicle();
        vehicle.setNewValue(new BigDecimal("7999.999"));
        policy.setRiskItems(List.of(vehicle));

        BillingInfo billingInfo = new BillingInfo();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardCreditLimitAmount(Money.of(500, "USD"));
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);

        var result = engine.evaluate(policy, "NumberSet");

        assertThat(result, hasValidationFailures(3));
    }
}
