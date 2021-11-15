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

import * as React from "react";
import { Row } from "antd";
import { Moment } from "moment";
import { optional } from "declarative-js";

import { InnerInputsComponentProps, SingleField, ManyFields } from "../core/field/SingleField";
import { trans } from "../core/DateUtils";
import { renderers } from "../core/RenderInputFunctions";

import { BillingAddress } from "./BillingAddres";

import { TestProduct } from "kraken-test-product";
import domain = TestProduct.kraken.testproduct.domain;
import { withMetadata } from "../core/ContextHOC";
import { ContextDefinitionInfo } from "../core/field/ContextDefinitionInfo";

class Component extends React.Component<InnerInputsComponentProps<domain.CreditCardInfo>> {
    onCardTypeChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { cardType: event.currentTarget.value }));
    }

    onCardNumberChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { cardNumber: event.currentTarget.value }));
    }

    onCvvChange = (cvv: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { cvv }));
    }

    onExpirationDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { expirationDate: trans.toDate(e) }));
    }

    onCreditCardLimitAmountChange = (e: React.ReactText) => {
        this.props.onChange(Object.assign(
            {},
            this.props.value,
            { cardCreditLimitAmount: { amount: Number(e), currency: "USD" } }
        ));
    }

    onBillingAddressChange = (billingAddress: domain.BillingAddress) => {
        this.props.onChange(Object.assign({}, this.props.value, { billingAddress }));
    }

    onRefToBankChange = (refsToBank: string[]) => {
        this.props.onChange(Object.assign({}, this.props.value, { refsToBank }));
    }

    render(): JSX.Element {
        return (
            <div>
                <Row>
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.cardType}
                        contextName="CreditCardInfo"
                        modelFieldName="cardType"
                        onChange={this.onCardTypeChange}

                        renderInput={renderers.input}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.cardNumber}
                        contextName="CreditCardInfo"
                        modelFieldName="cardNumber"
                        onChange={this.onCardNumberChange}

                        renderInput={renderers.input}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.cvv}
                        contextName="CreditCardInfo"
                        modelFieldName="cvv"
                        onChange={this.onCvvChange}

                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={trans.toMoment(this.props.value.expirationDate)}
                        contextName="CreditCardInfo"
                        modelFieldName="expirationDate"
                        onChange={this.onExpirationDateChange}

                        renderInput={renderers.date}
                    />
                    <SingleField
                        id={this.props.id}
                        value={optional(this.props.value.cardCreditLimitAmount).map(x => x.amount).orElse(undefined)}
                        contextName="CreditCardInfo"
                        modelFieldName="cardCreditLimitAmount"
                        onChange={this.onCreditCardLimitAmountChange}

                        renderInput={renderers.inputCurrency}
                    />
                    <ManyFields
                        id={this.props.id}
                        value={optional(this.props.value.refsToBank).orElse([])}
                        contextName="CreditCardInfo"
                        modelFieldName="refsToBank"
                        onChange={this.onRefToBankChange}

                        renderInput={renderers.input}
                    />
                </Row>

                <ContextDefinitionInfo contextName="BillingAddress" />
                <BillingAddress
                    id={this.props.value.billingAddress.id}
                    value={this.props.value.billingAddress}
                    onChange={this.onBillingAddressChange}
                />
            </div>
        );
    }
}
export const CreditCardInfo = withMetadata(Component);
