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

public class AnubisCoverage extends CarCoverage {

    private Cult cult;

    public AnubisCoverage() {
    }

    public AnubisCoverage(String code, BigDecimal limitAmount, String cultName) {
        super(code, limitAmount, null);

        this.setCult(new Cult(cultName, null));
    }

    public AnubisCoverage(String code, BigDecimal limitAmount) {
        super(code, limitAmount, null);
    }

    public AnubisCoverage(String code, BigDecimal limitAmount, BigDecimal deductibleAmount) {
        super(code, limitAmount, deductibleAmount);
    }

    public AnubisCoverage(String code, BigDecimal limitAmount, BigDecimal deductibleAmount, String cultName, LocalDate date) {
        super(code, limitAmount, deductibleAmount);
        this.setCult(new Cult(cultName, date));
    }

    public Cult getCult() {
        return cult;
    }

    public void setCult(Cult cult) {
        this.cult = cult;
    }


}
