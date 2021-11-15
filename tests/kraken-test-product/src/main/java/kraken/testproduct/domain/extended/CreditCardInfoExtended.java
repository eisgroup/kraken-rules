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

import kraken.testproduct.domain.CreditCardInfo;

import javax.money.MonetaryAmount;
import java.time.LocalDate;

public class CreditCardInfoExtended extends CreditCardInfo {

    private String cardType;

    private MonetaryAmount cardCreditLimitAmount;

    private String cardNumber;

    private Integer cvv;

    private LocalDate expirationDate;

    private String cardHolderName;

    private BillingAddressExtended billingAddress;

    public CreditCardInfoExtended() {
    }

    public CreditCardInfoExtended(String cardType, String cardNumber, Integer cvv, BillingAddressExtended billingAddress, MonetaryAmount cardCreditLimitAmount) {
        this.cardType = cardType;
        this.cardCreditLimitAmount = cardCreditLimitAmount;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.billingAddress = billingAddress;
    }

    public MonetaryAmount getCardCreditLimitAmount() {
        return cardCreditLimitAmount;
    }

    public void setCardCreditLimitAmount(MonetaryAmount cardCreditLimitAmount) {
        this.cardCreditLimitAmount = cardCreditLimitAmount;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getCvv() {
        return cvv;
    }

    public void setCvv(Integer cvv) {
        this.cvv = cvv;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public BillingAddressExtended getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddressExtended billingAddress) {
        this.billingAddress = billingAddress;
    }
}
