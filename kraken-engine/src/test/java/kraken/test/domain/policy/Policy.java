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
package kraken.test.domain.policy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.money.MonetaryAmount;

/**
 * Created by rimas on 25/01/17.
 */
public class Policy {

    private String policyNo;

    private String policyCurrency;

    private BigDecimal policyLimit;

    private MonetaryAmount policyCost;

    private Integer policyTermNo;

    private Insured insured;

    private Integer termNo;

    private List<RiskItem> riskItems;

    private Map<String, RiskItem> riskItemMap;

    public Policy(String policyNo) {
        this.policyNo = policyNo;
        this.riskItems = new ArrayList<>();
        this.riskItemMap = new HashMap<>();
    }

    public Map<String, RiskItem> getRiskItemMap() {
        return riskItemMap;
    }

    public void setRiskItemMap(Map<String, RiskItem> riskItemMap) {
        this.riskItemMap = riskItemMap;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public String getPolicyCurrency() {
        return policyCurrency;
    }

    public void setPolicyCurrency(String policyCurrency) {
        this.policyCurrency = policyCurrency;
    }

    public BigDecimal getPolicyLimit() {
        return policyLimit;
    }

    public void setPolicyLimit(BigDecimal policyLimit) {
        this.policyLimit = policyLimit;
    }

    public Integer getPolicyTermNo() {
        return policyTermNo;
    }

    public void setPolicyTermNo(Integer policyTermNo) {
        this.policyTermNo = policyTermNo;
    }

    public MonetaryAmount getPolicyCost() {
        return policyCost;
    }

    public void setPolicyCost(MonetaryAmount policyCost) {
        this.policyCost = policyCost;
    }

    public void setRiskItems(List<RiskItem> riskItems) {
        this.riskItems = riskItems;
    }

    public List<RiskItem> getRiskItems() {
        return riskItems;
    }

    public Insured getInsured() {
        return insured;
    }

    public void setInsured(Insured insured) {
        this.insured = insured;
    }

    public Integer getTermNo() {
        return termNo;
    }

    public void setTermNo(Integer termNo) {
        this.termNo = termNo;
    }
}
