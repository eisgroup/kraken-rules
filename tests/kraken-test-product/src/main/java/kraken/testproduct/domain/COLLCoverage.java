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

import java.math.BigDecimal;
import java.time.LocalDate;

public class COLLCoverage extends CarCoverage {

    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    public COLLCoverage() {
    }

    public COLLCoverage(BigDecimal limitAmount) {
        super(null, limitAmount, null);
    }

    public COLLCoverage(String code, BigDecimal limitAmount, BigDecimal deductibleAmount, LocalDate effectiveDate, LocalDate expirationDate) {
        super(code, limitAmount, deductibleAmount);
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
    }

    public COLLCoverage(LocalDate effectiveDate, LocalDate expirationDate) {
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
