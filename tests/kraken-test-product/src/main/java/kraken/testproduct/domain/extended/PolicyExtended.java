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
package kraken.testproduct.domain.extended;

import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.Vehicle;

import java.util.List;

import static java.util.Collections.singletonList;

public class PolicyExtended extends Policy {

    public static PolicyExtended filled() {
        PolicyExtended policy = new PolicyExtended();
        BillingInfoExtended billingInfo = new BillingInfoExtended();
        CreditCardInfoExtended creditCardInfo = new CreditCardInfoExtended();
        creditCardInfo.setBillingAddress(new BillingAddressExtended(true, "LT", "AZ-21", "Vilnius"));
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);
        policy.setAccessTrackInfo(new AccessTrackInfoExtended());
        policy.setParties(singletonList(new PartyExtended(singletonList(new PartyRoleExtended("root")), "parent", "10", new DriverInfoExtended())));
        policy.setRiskItems(singletonList(new VehicleExtended(new AddressInfoExtended())));
        policy.setAccessTrackInfo(new AccessTrackInfoExtended());
        policy.setPolicyDetail(new PolicyDetailExtended());
        policy.setTermDetails(new TermDetailsExtended());
        policy.setTransactionDetails(new TransactionDetailsExtended());
        return policy;
    }
}
