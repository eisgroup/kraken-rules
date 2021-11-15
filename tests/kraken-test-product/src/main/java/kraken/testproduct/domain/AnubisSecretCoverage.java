/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author mulevicius
 */
public class AnubisSecretCoverage extends AnubisCoverage {

    private BigDecimal secretLimitAmount;

    public AnubisSecretCoverage(BigDecimal secretLimitAmount) {
        this.secretLimitAmount = secretLimitAmount;
    }

    public AnubisSecretCoverage(String code, BigDecimal limitAmount, String cultName, BigDecimal secretLimitAmount) {
        super(code, limitAmount, cultName);
        this.secretLimitAmount = secretLimitAmount;
    }

    public AnubisSecretCoverage(String code, BigDecimal limitAmount, BigDecimal secretLimitAmount) {
        super(code, limitAmount);
        this.secretLimitAmount = secretLimitAmount;
    }

    public AnubisSecretCoverage(String code, BigDecimal limitAmount, BigDecimal deductibleAmount, BigDecimal secretLimitAmount) {
        super(code, limitAmount, deductibleAmount);
        this.secretLimitAmount = secretLimitAmount;
    }

    public AnubisSecretCoverage(String code, BigDecimal limitAmount, BigDecimal deductibleAmount, String cultName, LocalDate date, BigDecimal secretLimitAmount) {
        super(code, limitAmount, deductibleAmount, cultName, date);
        this.secretLimitAmount = secretLimitAmount;
    }

    public void setSecretLimitAmount(BigDecimal secretLimitAmount) {
        this.secretLimitAmount = secretLimitAmount;
    }

    public BigDecimal getSecretLimitAmount() {
        return secretLimitAmount;
    }
}
