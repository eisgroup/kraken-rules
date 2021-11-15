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

import java.math.BigDecimal;

public class RentalCoverage extends Identifiable implements Coverage {

    private String code;
    private BigDecimal limitAmount;
    private BigDecimal deductibleAmount;

    public RentalCoverage() {
    }

    public RentalCoverage(String code, BigDecimal limitAmount, BigDecimal deductibleAmount) {
        this.code = code;
        this.limitAmount = limitAmount;
        this.deductibleAmount = deductibleAmount;
    }

    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BigDecimal getLimitAmount() {
        return limitAmount;
    }
    
    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    @Override
    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }

    @Override
    public String getCd() {
        return "RentalCoverage";
    }

    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
    }
    
}
