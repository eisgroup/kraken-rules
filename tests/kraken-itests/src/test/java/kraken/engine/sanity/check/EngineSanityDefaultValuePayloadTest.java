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

import kraken.el.functionregistry.functions.DateFunctions;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.AccessTrackInfo;
import kraken.testproduct.domain.BillingInfo;
import kraken.testproduct.domain.CreditCardInfo;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.PolicyDetail;
import kraken.testproduct.domain.TermDetails;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static kraken.testing.matchers.KrakenMatchers.hasValueChangeEvents;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public final class EngineSanityDefaultValuePayloadTest extends SanityEngineBaseTest {

    @Test
    public void shouldExecuteInitAutoPolicyEntryPointWithEmptyData() {
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(new CreditCardInfo());

        AccessTrackInfo accessTrackInfo = new AccessTrackInfo();
        accessTrackInfo.setUpdatedOn(LocalDate.MIN);
        accessTrackInfo.setUpdatedBy("qa1");

        Policy policy = new MockAutoPolicyBuilder()
                .addBillingInfo(billingInfo)
                .addAccessTrackInfo(accessTrackInfo)
                .addTxDetails(new TransactionDetails())
                .addParties(new ArrayList<>())
                .addPolicyDetail(new PolicyDetail())
                .addRiskItems(Arrays.asList(new Vehicle()))
                .addTermDetails(new TermDetails())
                .build();

        final EntryPointResult result = engine.evaluate(policy, "InitAutoPolicy");
        final LocalDate today = LocalDate.now();

        assertThat(policy.getPolicyNumber(), is("Q0001"));

        assertThat(policy.getState(), is("Initialized"));
        assertThat(policy.getCreatedFromPolicyRev(), is(1));
        assertThat(policy.getTransactionDetails().getTxType(), is("NEW BUSINESS"));
        assertThat(
                policy.getTransactionDetails().getTxEffectiveDate(),
                is(DateFunctions.dateTime("2018-04-30T10:56:56Z"))
        );
        assertThat(policy.getTransactionDetails().getTxCreateDate(), is(today));
        assertThat(policy.getAccessTrackInfo().getCreatedOn(), is(today));
        assertThat(policy.getAccessTrackInfo().getUpdatedOn(), is(today));
        assertThat(policy.getAccessTrackInfo().getUpdatedBy(), is("qa2"));
        assertThat(policy.getTermDetails().getTermEffectiveDate(), is(today));
        assertThat(policy.getTermDetails().getTermNo(), is(0));
        assertThat(policy.getTermDetails().getTermCd(), is(equalTo("ANNUAL")));
        assertThat(result, hasValueChangeEvents(13));
    }

    @Test
    public void shouldFailWhenTwoRulesEvaluatedOnSameField() {
        // second default rule is activated when policyNumber is '666'
        Policy data = new Policy();
        data.setPolicyNumber("666");
        assertThrows(KrakenRuntimeException.class, () -> engine.evaluate(data, "InitAutoPolicy"));
    }

    @Test
    public void shouldExecuteInitAutoPolicyEntryPointWithValidData() {
        Policy policy = new MockAutoPolicyBuilder().addValidAutoPolicyWithMockDateTime().build();

        final EntryPointResult result = engine.evaluate(policy, "InitAutoPolicy");

        assertThat(policy.getPolicyNumber(), is("Q0006"));
        assertThat(policy.getState(), is("State"));
        assertThat(policy.getCreatedFromPolicyRev(), is(1));
        assertThat(policy.getTransactionDetails().getTxType(), is("CashBack"));
        assertThat(
                policy.getTransactionDetails().getTxEffectiveDate(),
                is(DateFunctions.dateTime("2018-01-01T00:00:00Z"))
        );
        assertThat(
                policy.getTransactionDetails().getTxCreateDate(),
                is(LocalDate.of(2018, 1, 1))
        );
        assertThat(
                policy.getAccessTrackInfo().getCreatedOn(),
                is(LocalDate.of(2000, 1, 1))
        );
        assertThat(
                policy.getTermDetails().getTermEffectiveDate(),
                is(LocalDate.of(2000, 1, 1))
        );
        assertThat(
                policy.getTermDetails().getTermExpirationDate(),
                is(LocalDate.of(3000, 1, 1))
        );
        assertThat(policy.getAccessTrackInfo().getUpdatedOn(), is(LocalDate.now()));
        assertThat(policy.getTermDetails().getTermNo(), is(11));
        assertThat(policy.getTermDetails().getTermCd(), is("TemrCd"));
        assertThat(policy.getAccessTrackInfo().getUpdatedBy(), is("qa2"));
        assertThat(result, hasValueChangeEvents(2));
    }
}
