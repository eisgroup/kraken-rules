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
import kraken.testproduct.domain.*;
import kraken.testproduct.domain.extended.VehicleExtended;
import org.javamoney.moneta.Money;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static org.hamcrest.MatcherAssert.assertThat;

public class EngineSanityFunctionsTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldDefaultWithCountFunctionOutput() {
        final Policy policy = new Policy();
        policy.setPolicies(Arrays.asList("fourth", "sixth"));
        final HashMap<String, Object> context = new HashMap<>();
        context.put("numbers", List.of("first", "second", "third", "fifth"));
        EvaluationConfig evaluationConfig = new EvaluationConfig(context, "USD");
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-With-Count", evaluationConfig);
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(2));
        assertThat(policy.getPolicyNumber(), is("4"));
        assertThat(policy.getState(), is("2"));
    }

    @Test
    public void shouldDefaultWithSumFunctionOutput() {
        final Policy policy = new Policy();
        BillingInfo billingInfo = new BillingInfo();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardCreditLimitAmount(Money.of(1555.35, "USD"));
        creditCardInfo.setCvv(123);
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-With-Sum");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(policy.getPolicyNumber(), is("1678.35"));
    }

    @Test
    public void shouldDefaultWithAvgFunctionOutput() {
        final Policy policy = new Policy();
        Insured insured = new Insured();
        insured.setChildrenAges(Arrays.asList(5, 9, 18));
        policy.setInsured(insured);
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-With-Avg");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result,  hasRuleResults(1));
        assertThat(policy.getPolicyNumber(), is("10.66666666666667"));
    }

    @Test
    public void shouldDefaultWithMaxFunctionOutput() {
        final Policy policy = new Policy();
        Vehicle vehicle = new Vehicle();
        policy.setRiskItems(List.of(vehicle));
        vehicle.setModelYear(2001);
        vehicle.setDeclaredAnnualMiles(1548458l);
        vehicle.setCostNew(BigDecimal.valueOf(12542.652));
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-With-Max");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(vehicle.getNewValue(), is(BigDecimal.valueOf(1548458)));
    }

    @Test
    public void shouldDefaultWithMinFunctionOutput() {
        final Policy policy = new Policy();
        Insured insured = new Insured();
        insured.setChildrenAges(Arrays.asList(5, 9, 18));
        policy.setInsured(insured);
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-With-Min");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(policy.getPolicyNumber(), is("5"));
    }

    @Test
    public void shouldFlatElements() {
//        assert to have 7 items (3 of them serviceHistories)
        final Policy policy = new Policy();
        policy.setRiskItems(List.of(
                new VehicleExtended(List.of(LocalDate.MAX)),
                new VehicleExtended(List.of(LocalDate.MAX, LocalDate.MAX))
                )
        );
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Flat");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(0));
    }
    @Test
    public void shouldFlatElementsAndFailAssertion() {
//        assert to have 7 items (2 of them serviceHistories)
        final Policy policy = new Policy();
        policy.setRiskItems(List.of(
                new VehicleExtended(List.of(LocalDate.MAX)),
                new VehicleExtended(List.of(LocalDate.MAX))
                )
        );
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Flat");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
    }

    @Test
    public void shouldDefaultWithSubstringOutput() {
        final Policy policy = new Policy();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardNumber("378282246310005");
        creditCardInfo.setCardType("American Express");
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-With-Substring");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(2));
        assertThat(policy.getPolicyNumber(), is("22463"));
        assertThat(policy.getState(), is(" Express"));
    }

    @Test
    public void shouldDefaultWithValueFromFunction() {
        final Policy policy = new Policy();
        TermDetails termDetails = new TermDetails();
        termDetails.setTermCd("cd125425464");
        policy.setTermDetails(termDetails);
        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Default-PolicyNumber");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(policy.getPolicyNumber(), is("125425464"));
    }

    @Test
    public void shouldExecuteFunctionAssertsOnPolicy() {
        final Policy policy = new Policy();
        final CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardCreditLimitAmount(Money.of(11, "USD"));
        policy.setBillingInfo(new BillingInfo("", creditCardInfo));

        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-Assert-PolicyNumber");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasRuleResults(29));
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteKelFunction() {
        COLLCoverage coverage1 = new COLLCoverage(new BigDecimal(50));
        COLLCoverage coverage2 = new COLLCoverage(new BigDecimal(25));
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setCollCoverages(List.of(coverage1, coverage2));
        COLLCoverage coverage3 = new COLLCoverage(new BigDecimal(125));
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setCollCoverages(List.of(coverage3));
        Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());
        policy.setRiskItems(List.of(vehicle1, vehicle2));

        EntryPointResult result = engine.evaluate(policy, "Expressions_kel_functions");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasRuleResults(2));
        assertThat(policy.getTransactionDetails().getTotalLimit(), equalTo(new BigDecimal(200)));
    }

    @Test
    public void shouldExecuteFromMoneyFunctionAndPassAssertions() {
        Policy policy = new Policy();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardCreditLimitAmount(Money.of(100, "USD"));
        policy.setBillingInfo(new BillingInfo("ACC", creditCardInfo));

        EntryPointResult result = engine.evaluate(policy, "FunctionCheck-FromMoney");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasRuleResults(1));
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldExecuteFromMoneyFunctionAndFailAssertions() {
        Policy policy = new Policy();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setCardCreditLimitAmount(Money.of(99, "USD"));
        policy.setBillingInfo(new BillingInfo("ACC", creditCardInfo));

        EntryPointResult result = engine.evaluate(policy, "FunctionCheck-FromMoney");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, matchesSnapshot());
    }

}
