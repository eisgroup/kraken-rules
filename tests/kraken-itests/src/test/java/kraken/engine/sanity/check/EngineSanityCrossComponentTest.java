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

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluationException;
import kraken.testproduct.domain.AddressInfo;
import kraken.testproduct.domain.AddressLine1;
import kraken.testproduct.domain.AddressLine2;
import kraken.testproduct.domain.BillingAddress;
import kraken.testproduct.domain.BillingInfo;
import kraken.testproduct.domain.CreditCardInfo;
import kraken.testproduct.domain.DriverInfo;
import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.PartyRole;
import kraken.testproduct.domain.PersonInfo;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.RRCoverage;
import kraken.testproduct.domain.Vehicle;
import kraken.testproduct.domain.extended.CreditCardInfoExtended;
import kraken.testproduct.domain.extended.RRCoverageExtended;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.Collections.singletonList;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class EngineSanityCrossComponentTest extends SanityEngineBaseTest {

    @Test
    public void shouldFindReferenceFrom2ndLevelToRoot_ConditionFalse() {
        final EntryPointResult evaluate = engine.evaluate(getDataObject(), "Cross-1");
        assertThat(evaluate, hasRuleResults(1));
        assertThat(evaluate.getAllRuleResults().get(0).getConditionEvaluationResult().isApplicable(), is(false));
    }

    @Test
    public void shouldFindReferenceFrom2ndLevelToRoot_ConditionTrue() {
        final Policy policySummary = (Policy) getDataObject();
        policySummary.setPolicyCurrency("USD");
        final EntryPointResult result = engine.evaluate(policySummary, "Cross-1");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldSetDefaultValueFromPolicyToCreditCard() {
        final Policy policySummary = (Policy) getDataObject();
        policySummary.setPolicyCurrency("1");
        final EntryPointResult result = engine.evaluate(policySummary, "R-CCR-default-CreditCardInfo-fromAutoPolicy");
        assertThat(policySummary.getBillingInfo().getCreditCardInfo().getCardNumber(), is("1"));
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldFailCrossContextAssertionFromRRCoverage() {
        final RRCoverage rrCoverage = new RRCoverage();
        rrCoverage.setCombinedLimit("test");
        final Vehicle vehicle = new Vehicle();
        vehicle.setModel("test");
        vehicle.setRentalCoverage(rrCoverage);

        final Policy policySummary = (Policy) getDataObject();
        policySummary.setRiskItems(List.of(vehicle));

        final EntryPointResult result = engine.evaluate(policySummary, "CCR-Assert-RRCoverage-fromVehicle");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldReferenceFrom3rdLevelToRoot() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            policySummary.setPolicyCurrency(a);
            final BillingInfo billingInfo = new BillingInfo();
            final CreditCardInfo creditCardInfo = new CreditCardInfo();
            final BillingAddress billingAddress = new BillingAddress();
            billingAddress.setCountryCd(b);
            creditCardInfo.setBillingAddress(billingAddress);
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            return policySummary;
        };

        final EntryPointResult result = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-BillingAddress");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) result.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultF = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-BillingAddress");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultF.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));

    }

    @Test
    public void shouldReferenceFrom3rdLevelTo2ndLevel() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final BillingInfo billingInfo = new BillingInfo();
            final CreditCardInfo creditCardInfo = new CreditCardInfo();
            final BillingAddress billingAddress = new BillingAddress();
            billingAddress.setCountryCd(a);
            creditCardInfo.setBillingAddress(billingAddress);
            creditCardInfo.setCardType(b);
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            return policySummary;
        };

        final EntryPointResult result = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-BillingAddress-toCreditCard");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) result.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultF = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-BillingAddress-toCreditCard");
        assertThat(resultF, hasRuleResults(1));
        assertThat(resultF, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultF.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));

    }

    @Test
    public void shouldReferenceFrom2ndLevelCollectionToRoot() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            policySummary.getRiskItems().get(0).setModel(a);
            policySummary.setPolicyCurrency(b);
            return policySummary;
        };

        final EntryPointResult result = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-Vehicle-toAutoPolicy");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) result.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));

        final EntryPointResult resultT = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-Vehicle-toAutoPolicy");
        assertThat(resultT, hasRuleResults(1));
        assertThat(resultT, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultT.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

    }

    @Test
    public void shouldReferenceFromRootTo2ndLevel() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final BillingInfo billingInfo = new BillingInfo();
            final CreditCardInfo creditCardInfo = new CreditCardInfo();
            creditCardInfo.setCardType(a);
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            policySummary.setPolicyCurrency(b);
            return policySummary;
        };

        final EntryPointResult result = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-AutoPolicy-toCreditCard");
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) result.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-AutoPolicy-toCreditCard");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(resultFalse, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldReferenceFromRootTo3rdLevel() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final BillingInfo billingInfo = new BillingInfo();
            final CreditCardInfo creditCardInfo = new CreditCardInfo();
            creditCardInfo.setCardType(a);
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            policySummary.setPolicyCurrency(b);
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-AutoPolicy-to-CreditCardInfo");
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-AutoPolicy-to-CreditCardInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(resultFalse, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldReferenceFrom3rdTo3rdLevelSameBranch() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final Party party = new Party();
            final DriverInfo driverInfo = new DriverInfo();
            driverInfo.setDriverType(a);
            party.setDriverInfo(driverInfo);
            final PersonInfo personInfo = new PersonInfo();
            personInfo.setFirstName(b);
            party.setPersonInfo(personInfo);
            policySummary.setParties(singletonList(party));
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-DriverInfo-PersonInfo");
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-DriverInfo-PersonInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(resultFalse, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldReferenceFrom3rdTo3rdLevelSameBranchToParentAddress() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            Party party = new Party();
            DriverInfo driverInfo = new DriverInfo();
            driverInfo.setDriverType(a);
            PersonInfo personInfo = new PersonInfo();
            personInfo.setFirstName(b);
            party.setDriverInfo(driverInfo);
            party.setPersonInfo(personInfo);
            policySummary.setParties(singletonList(party));
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-PersonInfo-DriverInfo");
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-PersonInfo-DriverInfo");
        assertThat(resultFalse, hasNoIgnoredRules());
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldReferenceFrom3rdTo3rdLevelSameBranchSameParent() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            Party party = new Party();
            DriverInfo driverInfo = new DriverInfo();
            driverInfo.setDriverType(a);
            PersonInfo personInfo = new PersonInfo();
            personInfo.setFirstName(b);
            party.setDriverInfo(driverInfo);
            party.setPersonInfo(personInfo);
            policySummary.setParties(singletonList(party));
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-Party-PersonInfo-DriverInfo");
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-Party-PersonInfo-DriverInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
        assertThat(resultFalse, hasNoIgnoredRules());
    }

    @Test
    public void shouldReferenceFrom3rdTo2ndLevelDifferentBranchWithRestriction() {
        final Party party = new Party();
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final DriverInfo driverInfo = new DriverInfo();
            party.setDriverInfo(driverInfo);
            policySummary.setParties(singletonList(party));
            final CreditCardInfo creditCardInfo = new CreditCardInfo();
            final BillingInfo billingInfo = new BillingInfo();
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            driverInfo.setDriverType(a);
            creditCardInfo.setCardType(b);
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluateSubtree(model.apply("A", "A"), party, "R-CCR-assert-DriverInfo-CreditCardInfo");
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluateSubtree(model.apply("A", "B"), party, "R-CCR-assert-DriverInfo-CreditCardInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(resultFalse, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));

    }

    @Test
    public void shouldReferenceFrom3rdTo2ndLevelDifferentBranch() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final Party party = new Party();
            final DriverInfo driverInfo = new DriverInfo();
            driverInfo.setDriverType(a);
            party.setDriverInfo(driverInfo);
            policySummary.setParties(singletonList(party));
            final CreditCardInfo creditCardInfo = new CreditCardInfo();
            creditCardInfo.setCardType(b);
            final BillingInfo billingInfo = new BillingInfo();
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluate(model.apply("A", "A"), "R-CCR-assert-DriverInfo-CreditCardInfo");
        assertThat(resultTrue, hasRuleResults(1));
        assertThat(resultTrue, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultTrue.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-DriverInfo-CreditCardInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(resultFalse, hasNoIgnoredRules());
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));

    }

    @Test
    public void shouldReferenceInheritedCreditCardInfo() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            final BillingInfo billingInfo = new BillingInfo();
            billingInfo.setAccountName(b);
            final CreditCardInfoExtended creditCardInfo = new CreditCardInfoExtended();
            creditCardInfo.setCardType(a);
            billingInfo.setCreditCardInfo(creditCardInfo);
            policySummary.setBillingInfo(billingInfo);
            return policySummary;
        };

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-CreditCardInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldIgnoreRuleOnNullReferenceInCondition() {
        final BiFunction<String, String, Policy> model = (a, b) -> {
            final Policy policySummary = (Policy) getDataObject();
            BillingInfo billingInfo = new BillingInfo();
            billingInfo.setCreditCardInfo(null);
            policySummary.setBillingInfo(billingInfo);
            return policySummary;
        };

        final EntryPointResult resultTrue = engine.evaluate(model.apply("A", "A"), "R-CCR-default-condition-Policy-CreditCardInfo");
        assertThat(resultTrue, hasRuleResults(1));
        final ConditionEvaluationResult firstResult = resultTrue.getAllRuleResults().get(0).getConditionEvaluationResult();
        assertThat(firstResult.isApplicable(), is(false));
        assertThat(firstResult.getError(), instanceOf(KrakenExpressionEvaluationException.class));
    }

    @Test
    public void shouldIgnoreRuleOnNullReferenceInAssertion() {
        final Policy policySummary = (Policy) getDataObject();
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(null);
        policySummary.setBillingInfo(billingInfo);

        final EntryPointResult result = engine.evaluate(policySummary, "R-CCR-assert-Policy-CreditCardInfo");
        assertThat(result.getAllRuleResults(), hasSize(1));
        final AssertionPayloadResult payloadResult = (AssertionPayloadResult) result.getAllRuleResults().get(0).getPayloadResult();
        assertThat(payloadResult.getSuccess(), equalTo(null));
        assertTrue(payloadResult.getException().isPresent());
        assertThat(payloadResult.getException().get(), instanceOf(KrakenExpressionEvaluationException.class));
    }

    @Test
    public void shouldIgnoreRuleOnNullReferenceInDefaultPayloadHandler() {
        final Policy policySummary = (Policy) getDataObject();
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(null);
        policySummary.setBillingInfo(billingInfo);

        final EntryPointResult result = engine.evaluate(policySummary, "R-CCR-default-Policy-CreditCardInfo");

        assertThat(result.getAllRuleResults(), hasSize(1));
        final DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) result.getAllRuleResults().get(0).getPayloadResult();
        assertThat(payloadResult.getEvents(), hasSize(0));
        assertTrue(payloadResult.getException().isPresent());
        assertThat(payloadResult.getException().get(), instanceOf(KrakenExpressionEvaluationException.class));
    }

    @Test
    public void shouldReferenceInheritedThirdLevelCarCoverage() {
        BiFunction<String, String, Policy> model = (a, b) -> {
            Policy policySummary = (Policy) getDataObject();
            RRCoverageExtended rrCoverageExtended = new RRCoverageExtended();
            rrCoverageExtended.setCode(a);
            Vehicle vehicle = new Vehicle();
            vehicle.setModel(b);
            vehicle.setRentalCoverage(rrCoverageExtended);
            policySummary.setRiskItems(singletonList(vehicle));
            return policySummary;
        };

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-Policy-RentalCoverage");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldReferenceClosestInfo() {
        BiFunction<String, String, Policy> model = (a, b) -> {
            Policy policySummary = (Policy) getDataObject();
            Party party = new Party();
            party.setRelationToPrimaryInsured(a);
            PersonInfo personInfo = new PersonInfo();
            personInfo.setAdditionalInfo(b);
            party.setPersonInfo(personInfo);
            policySummary.setParties(singletonList(party));
            return policySummary;
        };

        final EntryPointResult resultFalse = engine.evaluate(model.apply("A", "B"), "R-CCR-assert-Party-Info");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }

    @Test
    public void shouldReferenceFromOneBranchToAnother() {
        BiFunction<String, String, Policy> model = (a, b) -> {
            Policy policySummary = (Policy) getDataObject();
            Party party = new Party();
            PersonInfo personInfo = new PersonInfo();
            AddressInfo addressInfo = new AddressInfo();
            addressInfo.setStreet(a);
            addressInfo.setAddressLine1(new AddressLine1(b));
            personInfo.setAddressInfo(addressInfo);
            party.setPersonInfo(personInfo);
            party.setRoles(Collections.singletonList(new PartyRole()));
            policySummary.setParties(singletonList(party));
            return policySummary;
        };

        final EntryPointResult result = engine.evaluate(model.apply("AnotherStreet", "street"), "R-CCR-assert-PartyRole-AddressLine1-AddressInfo");
        assertThat(result.getAllRuleResults(), hasSize(1));
        assertThat(((ValidationPayloadResult) result.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(true));

        final EntryPointResult resultFalse = engine.evaluate(model.apply("street", "street"), "R-CCR-assert-PartyRole-AddressLine1-AddressInfo");
        assertThat(resultFalse, hasRuleResults(1));
        assertThat(((ValidationPayloadResult) resultFalse.getAllRuleResults().get(0).getPayloadResult()).getSuccess(), is(false));
    }


    @Test
    public void shouldReferenceFromBottomLevelToRoot() {
        BiFunction<String, String, Policy> model = (a, b) -> {
            Policy policySummary = (Policy) getDataObject();
            policySummary.setState(a);
            policySummary.setId("policyId");
            Party party = new Party();
            party.setId("partyId");
            PersonInfo personInfo = new PersonInfo();
            personInfo.setId("personId");
            AddressInfo addressInfo = new AddressInfo();
            addressInfo.setId("addressInfo");
            addressInfo.setAddressLine1(new AddressLine1(b));
            addressInfo.setAddressLine2(new AddressLine2(b));
            personInfo.setAddressInfo(addressInfo);
            party.setPersonInfo(personInfo);
            party.setRoles(Collections.singletonList(new PartyRole()));
            policySummary.setParties(singletonList(party));
            return policySummary;
        };

        final EntryPointResult results = engine.evaluate(model.apply("CA", "street"), "R-CCR-default-AddressLine-Policy");
        assertThat(results, hasRuleResults(2));

        final EntryPointResult resultsNotApplicable = engine.evaluate(model.apply("NY", "street"), "R-CCR-default-AddressLine-Policy");
        assertThat(resultsNotApplicable.getAllRuleResults(), hasSize(2));
        Long notApplicableResults = resultsNotApplicable.getAllRuleResults().stream().filter(result -> !result.getConditionEvaluationResult().isApplicable()).count();
        assertThat(notApplicableResults, is(2L));
    }

    @Test
    public void shouldReferenceToManyItems() {
        final Policy policy = new Policy();
        policy.setParties(
                List.of(
                        new Party(
                                List.of(
                                        new PartyRole("Captain"),
                                        new PartyRole("Pirate")
                                ),
                                "Brother",
                                "c01"
                        ), new Party(
                                List.of(
                                        new PartyRole("Fish"),
                                        new PartyRole("Hero")
                                ),
                                "Father",
                                "c02"
                        )
                )
        );
        // rule to assert count of party roles is 4
        final EntryPointResult result = engine.evaluate(policy, "R-CCR-Policy-PartyRole");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result.getApplicableRuleResults(), hasSize(1));
        assertThat(result, hasValidationFailures(0));
    }
}
