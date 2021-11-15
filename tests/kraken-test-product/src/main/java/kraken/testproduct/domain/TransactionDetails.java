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
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionDetails extends Identifiable {

    private String txType;

    private String txReason;

    private LocalDateTime txEffectiveDate;

    private LocalDate txCreateDate;

    private BigDecimal totalPremium;

    private BigDecimal changePremium;

    private BigDecimal totalLimit;

    public TransactionDetails() {
    }

    public TransactionDetails(String txType, String txReason, LocalDateTime txEffectiveDate) {
        this.txType = txType;
        this.txReason = txReason;
        this.txEffectiveDate = txEffectiveDate;
    }

    public String getTxType() {
        return txType;
    }

    public void setTxType(String txType) {
        this.txType = txType;
    }

    public String getTxReason() {
        return txReason;
    }

    public void setTxReason(String txReason) {
        this.txReason = txReason;
    }

    public LocalDateTime getTxEffectiveDate() {
        return txEffectiveDate;
    }

    public void setTxEffectiveDate(LocalDateTime txEffectiveDate) {
        this.txEffectiveDate = txEffectiveDate;
    }

    public LocalDate getTxCreateDate() {
        return txCreateDate;
    }

    public void setTxCreateDate(LocalDate txCreateDate) {
        this.txCreateDate = txCreateDate;
    }

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public BigDecimal getChangePremium() {
        return changePremium;
    }

    public void setChangePremium(BigDecimal changePremium) {
        this.changePremium = changePremium;
    }

    public BigDecimal getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(BigDecimal totalLimit) {
        this.totalLimit = totalLimit;
    }

}
