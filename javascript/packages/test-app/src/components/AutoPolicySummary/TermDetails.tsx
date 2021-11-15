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

import { InnerInputsComponentProps, SingleField } from "../core/field/SingleField";
import { renderers } from "../core/RenderInputFunctions";
import { trans } from "../core/DateUtils";

import { TestProduct } from "kraken-test-product";
import domain = TestProduct.kraken.testproduct.domain;
import { withMetadata } from "../core/ContextHOC";

class Component extends React.Component<InnerInputsComponentProps<domain.TermDetails>> {
    onContractTermTypeCdChange = (e: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { contractTermTypeCd: e.currentTarget.value }));
    }

    onTermCdChange = (e: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { termCd: e.currentTarget.value }));
    }

    onTermNoChange = (e: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { termNo: e }));
    }

    onEffectiveDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { termEffectiveDate: trans.toDate(e) }));
    }

    onExpirationDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { termExpirationDate: trans.toDate(e) }));
    }

    render(): JSX.Element {
        const { metadata, id, value } = this.props;
        return (
            <Row>
                <SingleField
                    id={id}
                    value={value.contractTermTypeCd}
                    contextName="Policy"
                    modelFieldName="contractTermTypeCd"
                    onChange={this.onContractTermTypeCdChange}

                    renderInput={renderers.input}
                />
                <SingleField
                    id={id}
                    value={value.termNo}
                    contextName="Policy"
                    modelFieldName="termNo"
                    onChange={this.onTermNoChange}

                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={id}
                    value={trans.toMoment(value.termEffectiveDate)}
                    contextName="Policy"
                    modelFieldName="effectiveDate"
                    onChange={this.onEffectiveDateChange}

                    renderInput={renderers.date}
                />
                <SingleField
                    id={id}
                    value={trans.toMoment(value.termExpirationDate)}
                    contextName="Policy"
                    modelFieldName="expirationDate"
                    onChange={this.onExpirationDateChange}

                    renderInput={renderers.date}
                />
                <SingleField
                    id={id}
                    value={value.termCd}
                    contextName="Policy"
                    modelFieldName="termCd"
                    onChange={this.onTermCdChange}

                    renderInput={renderers.input}
                />
            </Row>
        );
    }
}
export const TermDetails = withMetadata(Component);
