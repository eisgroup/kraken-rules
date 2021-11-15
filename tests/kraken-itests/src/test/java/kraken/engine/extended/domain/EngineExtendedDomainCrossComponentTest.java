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
package kraken.engine.extended.domain;

import kraken.engine.sanity.check.SanityEngineBaseTest;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluationException;
import kraken.test.TestResources;
import kraken.testproduct.domain.*;
import kraken.testproduct.domain.extended.BillingInfoExtended;
import kraken.testproduct.domain.extended.CreditCardInfoExtended;
import kraken.testproduct.domain.extended.PolicyExtended;
import kraken.testproduct.domain.extended.RRCoverageExtended;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.Collections.singletonList;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EngineExtendedDomainCrossComponentTest extends SanityEngineBaseTest {

    @Override
    protected TestResources getResources() {
        return TestResources.create(TestResources.Info.TEST_PRODUCT_EXTENDED);
    }

    @Test
    public void shouldFindReferenceFrom2ndLevelToRoot_ConditionTrue_ExtendedModel() {
        final PolicyExtended filled = PolicyExtended.filled();
        filled.setPolicyCurrency("USD");

        final EntryPointResult result = engine.evaluate(filled, "Cross-1");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldReferenceFrom3rdTo2ndLevelDifferentBranchWithRestriction_Extended() {
        final Party party = PolicyExtended.filled().getParties().get(0);
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final PolicyExtended extended = PolicyExtended.filled();
            final DriverInfo driverInfo = extended.getParties().get(0).getDriverInfo();
            driverInfo.setDriverType(a);
            extended.getBillingInfo().getCreditCardInfo().setCardType(b);
            return extended;
        };
        engine.evaluateSubtree(model.apply("A", "A"), party, "R-CCR-assert-DriverInfo-CreditCardInfo");
        final EntryPointResult resultTrue = engine.evaluateSubtree(model.apply("A", "A"), party, "R-CCR-assert-DriverInfo-CreditCardInfo");
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(resultTrue, hasNoIgnoredRules());


        final EntryPointResult resultFalse = engine.evaluateSubtree(model.apply("A", "B"), party, "R-CCR-assert-DriverInfo-CreditCardInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
        assertThat(resultFalse, hasNoIgnoredRules());
    }

    @Test
    public void shouldCheckIsSelfReferenceWithInheritedContextWillBeSkipped() {
        final PolicyExtended policy = new PolicyExtended();
        policy.setPolicyNumber("P01");

        final EntryPointResult result = engine.evaluate(policy, "R-CCR-Policy-ExPolicy");
        assertThat(result.getApplicableRuleResults(), hasSize(1));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldEvaluateInheritanceCCRRulesWithoutExceptions() {
        final PolicyExtended policy = new PolicyExtended();
        policy.setPolicyCurrency("USD");
        final BillingInfoExtended billingInfo = new BillingInfoExtended();
        final CreditCardInfoExtended creditCardInf = new CreditCardInfoExtended();
        creditCardInf.setCardType("USD");
        billingInfo.setCreditCardInfo(creditCardInf);
        policy.setBillingInfo(billingInfo);

        final EntryPointResult result = engine.evaluate(policy, "Inheritance-CCR");
        assertThat(result.getApplicableRuleResults(), hasSize(2));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, hasNoIgnoredRules());
    }
}