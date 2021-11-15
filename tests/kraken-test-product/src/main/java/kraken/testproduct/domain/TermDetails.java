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

import java.time.LocalDate;

public class TermDetails extends Identifiable {

    private String contractTermTypeCd;

    private Integer termNo;

    private LocalDate termEffectiveDate;

    private LocalDate termExpirationDate;

    private String termCd;

    public TermDetails() {
    }

    public TermDetails(Integer termNo) {
        this.termNo = termNo;
    }

    public TermDetails(String contractTermTypeCd, Integer termNo, LocalDate termEffectiveDate,
            LocalDate termExpirationDate, String termCd) {
        this.contractTermTypeCd = contractTermTypeCd;
        this.termNo = termNo;
        this.termEffectiveDate = termEffectiveDate;
        this.termExpirationDate = termExpirationDate;
        this.termCd = termCd;
    }

    public String getContractTermTypeCd() {
        return contractTermTypeCd;
    }

    public void setContractTermTypeCd(String contractTermTypeCd) {
        this.contractTermTypeCd = contractTermTypeCd;
    }

    public Integer getTermNo() {
        return termNo;
    }

    public void setTermNo(Integer termNo) {
        this.termNo = termNo;
    }

    public LocalDate getTermEffectiveDate() {
        return termEffectiveDate;
    }

    public void setTermEffectiveDate(LocalDate termEffectiveDate) {
        this.termEffectiveDate = termEffectiveDate;
    }

    public LocalDate getTermExpirationDate() {
        return termExpirationDate;
    }

    public void setTermExpirationDate(LocalDate termExpirationDate) {
        this.termExpirationDate = termExpirationDate;
    }

    public String getTermCd() {
        return termCd;
    }

    public void setTermCd(String termCd) {
        this.termCd = termCd;
    }
}
