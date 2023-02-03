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
package kraken.utils;

import kraken.el.functionregistry.functions.DateFunctions;
import kraken.testproduct.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.*;

/**
 * @author avasiliauskas
 */
public class MockAutoPolicyBuilder {

    private String policyNumber;

    private String state;

    private Integer createdFromPolicyRev;

    private TransactionDetails transactionDetails;

    private PolicyDetail policyDetail;

    private AccessTrackInfo accessTrackInfo;

    private TermDetails termDetails;

    private BillingInfo billingInfo;

    private CreditCardInfo creditCardInfo;

    private List<Vehicle> riskItems;

    private List<Party> parties;

    private List<PartyRole> partiesRoles;

    private Policy policy;

    private LocalDate todayDate;

    private LocalDate futureDate;

    private LocalDate pastDate;

    private PersonInfo personaInfo;

    public MockAutoPolicyBuilder() {
        todayDate = LocalDate.now();
        futureDate = LocalDate.of(3000, 12, 20);
        pastDate = LocalDate.of(2000, 01, 01);
    }

    public MockAutoPolicyBuilder addPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
        return this;
    }

    private MockAutoPolicyBuilder addValidPolicyNumber() {
        this.policyNumber = "Q0006";
        return this;
    }

    public MockAutoPolicyBuilder addState(String state) {
        this.state = state;
        return this;
    }

    private MockAutoPolicyBuilder addValidState() {
        this.state = "State";
        return this;
    }

    public MockAutoPolicyBuilder addCreatedFromPolicyRev(Integer createdFromPolicyRev) {
        this.createdFromPolicyRev = createdFromPolicyRev;
        return this;
    }

    private MockAutoPolicyBuilder addValidCreatedFromPolicyRev() {
        this.createdFromPolicyRev = 1;
        return this;
    }

    public MockAutoPolicyBuilder addTxDetails(TransactionDetails transactionDetails) {
        this.transactionDetails = transactionDetails;
        return this;
    }

    private MockAutoPolicyBuilder addValidTxDetails() {
        transactionDetails = new TransactionDetails();
        transactionDetails.setTxType("CashBack");
        transactionDetails.setTxReason("Loan");
        transactionDetails.setTxCreateDate(todayDate);
        transactionDetails.setTxEffectiveDate(DateFunctions.dateTime("2018-01-01T00:00:00Z"));
        return this;
    }

    public MockAutoPolicyBuilder addPolicyDetail(PolicyDetail policyDetail) {
        this.policyDetail = policyDetail;
        return this;
    }

    public MockAutoPolicyBuilder addValidPolicyDetail() {
        this.policyDetail = new PolicyDetail();
        return this;
    }

    public MockAutoPolicyBuilder addAccessTrackInfo(AccessTrackInfo accessTrackInfo) {
        this.accessTrackInfo = accessTrackInfo;
        return this;
    }

    private MockAutoPolicyBuilder addValidAccessTrackInfo() {
        this.accessTrackInfo = new AccessTrackInfo(pastDate, "Manager", todayDate, "qaqa");
        return this;
    }

    public MockAutoPolicyBuilder addTermDetails(TermDetails termDetails) {
        this.termDetails = termDetails;
        return this;
    }

    private MockAutoPolicyBuilder addValidTermDetails() {
        this.termDetails = new TermDetails(
                "ContractTermTypeCd", 11, pastDate, futureDate, "TemrCd");
        return this;
    }

    public MockAutoPolicyBuilder addBillingInfo(BillingInfo billingInfo) {
        this.billingInfo = billingInfo;
        return this;
    }

    private MockAutoPolicyBuilder addValidBillingInfoWithCreditCardInfo() {
        this.billingInfo = new BillingInfo();
        addValidCreditCardInfo();
        return this;
    }

    public MockAutoPolicyBuilder addCreditCardInfo(CreditCardInfo creditCardInfo) {
        if (billingInfo != null) {
            billingInfo.setCreditCardInfo(creditCardInfo);
        } else {
            billingInfo = new BillingInfo();
            billingInfo.setCreditCardInfo(creditCardInfo);
        }
        return this;
    }

    private MockAutoPolicyBuilder addValidCreditCardInfo() {
        creditCardInfo = new CreditCardInfo(
                "MasterCard", "5105105105105100",
                123,
                new BillingAddress(true, "CA", "51546", "San Francisco"),
                null);
        if (billingInfo != null) {
            billingInfo.setCreditCardInfo(creditCardInfo);
        } else {
            billingInfo = new BillingInfo();
            billingInfo.setCreditCardInfo(creditCardInfo);
        }
        return this;
    }

    public MockAutoPolicyBuilder addRiskItems(List<Vehicle> riskItems) {
        this.riskItems = riskItems;
        return this;
    }

    public MockAutoPolicyBuilder addValidRiskItems(int amountOfRiskItems) {
        riskItems = new ArrayList<>();
        for (int index = 0; amountOfRiskItems > index; index++) {
            final String idxString = String.valueOf(index);
            final Vehicle vehicle = new Vehicle(
                    true,
                    "BMW X-" + idxString,
                    pastDate,
                    new AddressInfo(true, "LA", "51542", "San Francisco")
            );
            vehicle.setRentalCoverage(new RRCoverage(
                    idxString,
                    new BigDecimal(800000),
                    new BigDecimal(2000),
                    "5200"
            ));
            vehicle.setAnubisCoverages(singletonList(new AnubisCoverage(
                    "CD" + idxString,
                    new BigDecimal(800000),
                    new BigDecimal(2000)
            )));
            vehicle.setFullCoverages(singletonList(new FullCoverage(
                    idxString,
                    new BigDecimal(800000),
                    new BigDecimal(1000),
                    LocalDate.MIN,
                    LocalDate.MAX,
                    "Spinal"
            )));
            vehicle.setCollCoverages(singletonList(new COLLCoverage(
                    idxString,
                    new BigDecimal(50001),
                    new BigDecimal(0),
                    LocalDate.MIN,
                    LocalDate.MAX
            )));
            riskItems.add(vehicle);
        }
        return this;
    }

    public MockAutoPolicyBuilder addParty(Party party) {
        if (parties == null) {
            parties = new ArrayList<>();
        }
        parties.add(party);
        return this;
    }

    public MockAutoPolicyBuilder addParties(List<Party> parties) {
        this.parties = parties;
        return this;
    }

    private MockAutoPolicyBuilder addValidPartyWithPartyRoles(int amountOfPartiesRoles) {
        partiesRoles = new ArrayList<>();

        for (int index = 0; amountOfPartiesRoles > index; index++) {
            partiesRoles.add(new PartyRole("Admin"));
        }

        if (parties == null) {
            parties = new ArrayList<>(singletonList(new Party(partiesRoles, "Related", "1")));
        } else {
            parties.add(new Party(partiesRoles, "Related", "1"));
        }
        return this;
    }

    private MockAutoPolicyBuilder addValidPersonalInfoWithAddress() {
        personaInfo = new PersonInfo();
        personaInfo.setFirstName("Jonas");
        personaInfo.setLastName("Jomantas");
        personaInfo.setAddressInfo(new AddressInfo(true, "CD", "00000", "CITY"));
        if (parties != null && parties.size() > 0) {
            parties.get(0).setPersonInfo(personaInfo);
        } else {
            Party party = new Party(partiesRoles, "Related", "3");
            party.setPersonInfo(personaInfo);
            parties = new ArrayList<>(singletonList(party));
        }
        return this;
    }

    public MockAutoPolicyBuilder addValidAutoPolicy() {
        this.addValidBillingInfoWithCreditCardInfo();
        this.addValidPersonalInfoWithAddress();
        this.addValidCreatedFromPolicyRev();
        this.addValidPartyWithPartyRoles(1);
        this.addValidAccessTrackInfo();
        this.addValidPolicyNumber();
        this.addValidTermDetails();
        this.addValidRiskItems(1);
        this.addValidTxDetails();
        this.addValidState();
        return this;
    }

    public MockAutoPolicyBuilder addValidAutoPolicyWithMockDateTime() {
        AddressInfo addressInfo = new AddressInfo(true, "LA", "12345", "San Francisco");
        addressInfo.setId("2");
        Vehicle vehicle = new Vehicle(true, "BMW", LocalDate.of(2000, 01, 01),
                addressInfo);
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setTxType("CashBack");
        transactionDetails.setTxReason("Loan");
        transactionDetails.setTxCreateDate(LocalDate.of(2018, 01, 01));
        transactionDetails.setTxEffectiveDate(DateFunctions.dateTime("2018-01-01T00:00:00Z"));
        this.addValidAutoPolicy()
                .addAccessTrackInfo(new AccessTrackInfo(
                        LocalDate.of(2000, 01, 01),
                        "Manager",
                        LocalDate.of(2018, 01, 01),
                        "qaqa"))
                .addValidPolicyNumber()
                .addTermDetails(new TermDetails(
                        "ContractTermTypeCd",
                        11,
                        LocalDate.of(2000, 01, 01),
                        LocalDate.of(3000, 01, 01),
                        "TemrCd"))
                .addRiskItems(singletonList(vehicle))
                .addTxDetails(transactionDetails);
        return this;
    }

    public MockAutoPolicyBuilder addEmptyAutoPolicy() {
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(new CreditCardInfo());
        this.addBillingInfo(billingInfo);
        Party party = new Party("1");
        party.setPersonInfo(new PersonInfo());
        party.setRoles(singletonList(new PartyRole()));
        party.setDriverInfo(new DriverInfo());
        this.addParty(party);
        this.addAccessTrackInfo(new AccessTrackInfo());
        this.addTermDetails(new TermDetails());
        Vehicle vehicle = new Vehicle();
        vehicle.setAddressInfo(new AddressInfo());
        this.addRiskItems(singletonList(vehicle));
        this.addTxDetails(new TransactionDetails());
        return this;
    }

    public Policy build() {
        policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        policy.setState(state);
        policy.setCreatedFromPolicyRev(createdFromPolicyRev);
        policy.setTransactionDetails(transactionDetails);
        policy.setPolicyDetail(policyDetail);
        policy.setAccessTrackInfo(accessTrackInfo);
        policy.setTermDetails(termDetails);
        policy.setBillingInfo(billingInfo);
        if (riskItems != null) {
            policy.setRiskItems(riskItems);
        }
        if (parties != null) {
            policy.setParties(parties);
        }
        return policy;
    }
}
