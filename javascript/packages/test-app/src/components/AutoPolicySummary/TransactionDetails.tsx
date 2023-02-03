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

import * as React from 'react'
import { Row } from 'antd'
import { Moment } from 'moment'

import { InnerInputsComponentProps, SingleField } from '../core/field/SingleField'
import { renderers } from '../core/RenderInputFunctions'
import { trans } from '../core/DateUtils'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../core/ContextHOC'

class Component extends React.Component<InnerInputsComponentProps<domain.TransactionDetails>> {
    onTxTypeChange = (e: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { txType: e.currentTarget.value }))
    }

    onTxReasonChange = (e: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { txReason: e.currentTarget.value }))
    }

    onTxEffectiveDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { txEffectiveDate: trans.toDateTime(e) }))
    }

    onTxCreateDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { txCreateDate: trans.toDate(e) }))
    }

    onTotalChangePremiumChange = (e: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { changePremium: e }))
    }

    onTotalPremiumChange = (e: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { totalPremium: e }))
    }

    render(): JSX.Element {
        const { id, value } = this.props
        return (
            <Row>
                <SingleField
                    id={id}
                    value={value.txType}
                    contextName='Policy'
                    modelFieldName='txType'
                    onChange={this.onTxTypeChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={id}
                    value={value.txReason}
                    contextName='Policy'
                    modelFieldName='txReason'
                    onChange={this.onTxReasonChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={id}
                    value={trans.toMoment(value.txEffectiveDate)}
                    contextName='Policy'
                    modelFieldName='txEffectiveDate'
                    onChange={this.onTxEffectiveDateChange}
                    renderInput={renderers.dateTime}
                />
                <SingleField
                    id={id}
                    value={trans.toMoment(value.txCreateDate)}
                    contextName='Policy'
                    modelFieldName='txCreateDate'
                    onChange={this.onTxCreateDateChange}
                    renderInput={renderers.date}
                />
                <SingleField
                    id={id}
                    value={value.changePremium}
                    contextName='Policy'
                    modelFieldName='changePremium'
                    onChange={this.onTotalChangePremiumChange}
                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={id}
                    value={value.totalPremium}
                    contextName='Policy'
                    modelFieldName='totalPremium'
                    onChange={this.onTotalPremiumChange}
                    renderInput={renderers.inputNumber}
                />
            </Row>
        )
    }
}
export const TransactionDetails = withMetadata(Component)
