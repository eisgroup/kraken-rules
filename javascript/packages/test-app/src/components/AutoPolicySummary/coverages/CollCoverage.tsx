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

import { trans } from "../../core/DateUtils";
import { renderers } from "../../core/RenderInputFunctions";
import { InnerInputsComponentProps, SingleField } from "../../core/field/SingleField";

import { TestProduct } from "kraken-test-product";
import domain = TestProduct.kraken.testproduct.domain;
import { withMetadata } from "../../core/ContextHOC";

class Component extends React.Component<InnerInputsComponentProps<domain.COLLCoverage>> {
    onCodeChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { code: event.currentTarget.value }));
    }

    onLimitAmountChange = (limitAmount: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { limitAmount }));
    }

    onDeductibleAmountChange = (deductibleAmount: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { deductibleAmount }));
    }

    onEffectiveDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { effectiveDate: trans.toDate(e) }));
    }

    onExpirationDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { expirationDate: trans.toDate(e) }));
    }

    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={this.props.value.code}
                    contextName="COLLCoverage"
                    modelFieldName="code"
                    onChange={this.onCodeChange}

                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.limitAmount}
                    contextName="COLLCoverage"
                    modelFieldName="limitAmount"
                    onChange={this.onLimitAmountChange}

                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.deductibleAmount}
                    contextName="COLLCoverage"
                    modelFieldName="deductibleAmount"
                    onChange={this.onDeductibleAmountChange}

                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={this.props.id}
                    value={trans.toMoment(this.props.value.effectiveDate)}
                    contextName="COLLCoverage"
                    modelFieldName="effectiveDate"
                    onChange={this.onEffectiveDateChange}

                    renderInput={renderers.date}
                />
                <SingleField
                    id={this.props.id}
                    value={trans.toMoment(this.props.value.expirationDate)}
                    contextName="COLLCoverage"
                    modelFieldName="expirationDate"
                    onChange={this.onExpirationDateChange}

                    renderInput={renderers.date}
                />
            </Row>
        );
    }
}

export const CollCoverage = withMetadata(Component);
