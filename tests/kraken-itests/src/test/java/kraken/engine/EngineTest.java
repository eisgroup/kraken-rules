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
package kraken.engine;

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.runtime.engine.result.reducers.validation.ValidationResult;
import kraken.test.TestResources;
import kraken.testproduct.domain.*;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public final class EngineTest extends EngineBaseTest {

    @Override
    protected DataObjectInfoResolver getResolver() { return new SimpleDataObjectInfoResolver(); }

    @Override
    protected TestResources getResources() {
        return TestResources.create(TestResources.Info.TEST_PRODUCT);
    }

    @Override
    protected Object getDataObject() {
        return new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
    }

    @Override
    protected List<IterableContextTypeAdapter> getIterableTypeAdapters() {
        return new ArrayList<>();
    }

    @Override
    protected List<ContextTypeAdapter> getInstanceTypeAdapters() {
        return new ArrayList<>();
    }

    @Test
    public void shouldExecuteCoverageAssertionEntryPoint() {
        final Policy policy = (Policy) getDataObject();
        final COLLCoverage collCoverage = new COLLCoverage();
        collCoverage.setLimitAmount(new BigDecimal("100"));
        policy.setCoverage(collCoverage);

        final EntryPointResult result = engine.evaluate(policy, "CoverageAssertion");
        final List<ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(result, hasValidationFailures(1));
        assertThat(errorResults, hasSize(1));
        assertThat(errorResults, contains(hasProperty("ruleName", is("R0117"))));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteCreditCardInfoRegExpEntryPoint() {
        final String RuleName = "R0066";
        final Policy policy = (Policy) getDataObject();
        final CreditCardInfo creditCardInfo = new CreditCardInfo();
        final BillingInfo billingInfo = new BillingInfo();
        creditCardInfo.setCardType("MasterCard");
        creditCardInfo.setCardNumber("5500000000000004");
        creditCardInfo.setCvv(5555);
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);

        final EntryPointResult result = engine.evaluate(policy, "CreditCardInfoRegExp");
        final List <ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(result, hasRuleResults(4));
        assertThat(errorResults, hasSize(1));
        assertThat(errorResults.get(0).getRuleName(), is(RuleName));
        assertThat(errorResults.get(0).getContextFieldInfo().getContextName(), is("CreditCardInfo"));
        assertThat(errorResults.get(0).getMessage(), is("String doesn't match Regular Expression: ^[0-9]{3}$"));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteCarCoverageUsageEntryPoint() {
        final Policy policy = (Policy) getDataObject();
        policy.setCoverage(new COLLCoverage());

        final EntryPointResult result = engine.evaluate(policy, "CarCoverageUsage");
        final List<ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(result, hasRuleResults(3));
        assertThat(result, hasValidationFailures(3));
        assertThat(errorResults.get(0).getContextFieldInfo().getContextName(), is("COLLCoverage"));
        assertThat(errorResults, containsInAnyOrder(
                hasProperty("ruleName", is("R0111")),
                hasProperty("ruleName", is("R0115")),
                hasProperty("ruleName", is("R0118"))
        ));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteAutoPolicyDefaultEntryPoint () {
        final Policy policy = (Policy) getDataObject();
        final EntryPointResult result = engine.evaluate(policy, "AutoPolicyDefault");

        assertThat(result, hasValueChangeEvents(5));
        assertThat(policy.getTransactionDetails().getTxCreateDate(), is(LocalDate.now()));
        assertThat(policy.getAccessTrackInfo().getCreatedOn(), is(LocalDate.now()));
        assertThat(policy.getAccessTrackInfo().getUpdatedBy(), is("qa2"));
        assertThat(policy.getTermDetails().getTermNo(), is(0));
        assertThat(policy.getTermDetails().getTermCd(), is("ANNUAL"));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteAutoPolicyDefaultAndAssertRulesInOrder() {
        final Policy policy = (Policy) getDataObject();
        final EntryPointResult result = engine.evaluate(policy, "AutoPolicyDefaultAndAssert");
        final List<ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(result.getAllRuleResults(), hasSize(2));
        assertThat(policy.getTermDetails().getTermNo(), is(0));
        assertThat(errorResults, hasSize(0));
        assertThat(result.getAllRuleResults().get(0).getRuleInfo().getRuleName(), is("R0053"));
        assertThat(result.getAllRuleResults().get(1).getRuleInfo().getRuleName(), is("R0051"));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteUsagePayloadAutoPolicyEntryPoint() {
        final Policy policy = (Policy) getDataObject();
        final EntryPointResult resultFromEvaluationWithEntryPointName = engine.evaluate(policy, "UsagePayloadAutoPolicy");
        final List<ValidationResult> errorResultsWithEntryPointName = validationStatusReducer.reduce(resultFromEvaluationWithEntryPointName).getErrorResults();

        assertThat(errorResultsWithEntryPointName, hasSize(21));
        assertThat(resultFromEvaluationWithEntryPointName.getAllRuleResults(), hasSize(21));
        assertThat(resultFromEvaluationWithEntryPointName.getApplicableRuleResults(), hasSize(21));
        assertThat(resultFromEvaluationWithEntryPointName.getFieldResults().entrySet(), hasSize(21));

        assertThat(resultFromEvaluationWithEntryPointName, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecutePartyLengthEntryPoint() {
        final Policy policy = (Policy) getDataObject();
        final Party party = new Party("1");
        final String relationToPrimaryInsured = "String contains more charactes than 20";
        party.setRelationToPrimaryInsured(relationToPrimaryInsured);
        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "PartyLength");
        final List<ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(errorResults, hasSize(1));
        assertThat(errorResults.get(0).getRuleName(), is("R0149A"));
        assertThat(errorResults.get(0).getMessage(), containsString(relationToPrimaryInsured));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldNotThrowOnEmptyEntryPoint() {
        final EntryPointResult result = engine.evaluate(getDataObject(), "NoRules");
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldCreateDateTimeFromDate() {
        final Policy policy = new Policy();
        final TransactionDetails txDetails =
                new TransactionDetails("type", "reason", LocalDateTime.MIN);
        policy.setTransactionDetails(txDetails);
        final EntryPointResult result = engine.evaluate(policy, "dates");
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldExecuteResetRulesWithNodeThatHasNoRule() {
        Policy policy = new MockAutoPolicyBuilder().addValidAutoPolicyWithMockDateTime().build();
        Insured insured = new Insured();
        policy.setInsured(insured);
        final EntryPointResult result = engine.evaluateSubtree(policy, insured, "ResetRules");

        Assert.assertThat(result, hasRuleResults(0));
    }

    @Test
    public void shouldExecuteRuleOnUnknownAttribute() {
        Policy policy = new Policy();
        policy.setRefToCustomer(List.of("a", "b", "c"));

        EntryPointResult result = engine.evaluate(policy, "UnknownAttributeRule");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasNoValidationFailures());
    }

}
