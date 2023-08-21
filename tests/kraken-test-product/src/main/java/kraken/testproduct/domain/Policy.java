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
package kraken.testproduct.domain;

import kraken.testproduct.domain.meta.Identifiable;

import java.util.List;

import javax.money.MonetaryAmount;

public class Policy extends Identifiable {

    private Referer referer;

    private CarCoverage coverage;

    private String policyCurrency;

    private BillingInfo billingInfo;
    
    private Insured insured;

    private AccessTrackInfo accessTrackInfo;

    private TransactionDetails transactionDetails;

    private List<? extends Party> parties;

    private PolicyDetail policyDetail;

    private List<? extends Vehicle> riskItems;

    private TermDetails termDetails;

    private String state;

    private String policyNumber;

    private MonetaryAmount policyValue;

    private List<String> policies;

    private Integer createdFromPolicyRev;

    private Object refToCustomer;

    private SysDate systemDate;
    private SysDate backupSystemDate;

    private SecondaryInsured oneInsured;
    private List<? extends SecondaryInsured> multipleInsureds;
    private List<? extends SecondaryInsured> multiInsureds1;
    private List<? extends SecondaryInsured> multiInsureds2;

    public void setMultipleInsureds(
            SecondaryInsured oneInsured,
            List<? extends SecondaryInsured> multipleInsureds,
            List<? extends SecondaryInsured> multiInsureds1,
            List<? extends SecondaryInsured> multiInsureds2
    ) {
        this.oneInsured = oneInsured;
        this.multiInsureds1 = multiInsureds1;
        this.multiInsureds2 = multiInsureds2;
        this.multipleInsureds = multipleInsureds;
    }

    public SecondaryInsured getOneInsured() {
        return oneInsured;
    }

    public List<? extends SecondaryInsured> getMultipleInsureds() {
        return multipleInsureds;
    }

    public List<? extends SecondaryInsured> getMultiInsureds1() {
        return multiInsureds1;
    }

    public List<? extends SecondaryInsured> getMultiInsureds2() {
        return multiInsureds2;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getPolicyCurrency() {
        return policyCurrency;
    }

    public void setPolicyCurrency(String policyCurrency) {
        this.policyCurrency = policyCurrency;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getCreatedFromPolicyRev() {
        return createdFromPolicyRev;
    }

    public void setCreatedFromPolicyRev(Integer createdFromPolicyRev) {
        this.createdFromPolicyRev = createdFromPolicyRev;
    }

    public TransactionDetails getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(TransactionDetails transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public PolicyDetail getPolicyDetail() {
        return policyDetail;
    }

    public void setPolicyDetail(PolicyDetail policyDetail) {
        this.policyDetail = policyDetail;
    }

    public AccessTrackInfo getAccessTrackInfo() {
        return accessTrackInfo;
    }

    public void setAccessTrackInfo(AccessTrackInfo accessTrackInfo) {
        this.accessTrackInfo = accessTrackInfo;
    }

    public TermDetails getTermDetails() {
        return termDetails;
    }

    public void setTermDetails(TermDetails termDetails) {
        this.termDetails = termDetails;
    }

    public BillingInfo getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(BillingInfo billingInfo) {
        this.billingInfo = billingInfo;
    }
    
    public Insured getInsured() {
        return insured;
    }
    
    public void setInsured(Insured insured) {
        this.insured = insured;
    }
    
    public List<? extends Vehicle> getRiskItems() {
        return riskItems;
    }

    public void setRiskItems(List<? extends Vehicle> riskItems) {
        this.riskItems = riskItems;
    }

    public List<? extends Party> getParties() {
        return parties;
    }

    public void setParties(List<? extends Party> parties) {
        this.parties = parties;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public Object getRefToCustomer() {
        return refToCustomer;
    }

    public void setRefToCustomer(Object refToCustomer) {
        this.refToCustomer = refToCustomer;
    }

    public CarCoverage getCoverage() {
        return coverage;
    }

    public void setCoverage(CarCoverage coverage) {
        this.coverage = coverage;
    }

    public Referer getReferer() {
        return referer;
    }

    public void setReferer(Referer referer) {
        this.referer = referer;
    }

    public MonetaryAmount getPolicyValue() {
        return policyValue;
    }

    public void setPolicyValue(MonetaryAmount policyValue) {
        this.policyValue = policyValue;
    }

    public SysDate getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(SysDate systemDate) {
        this.systemDate = systemDate;
    }

    public SysDate getBackupSystemDate() {
        return backupSystemDate;
    }

    public void setBackupSystemDate(SysDate backupSystemDate) {
        this.backupSystemDate = backupSystemDate;
    }
}
