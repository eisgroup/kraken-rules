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

import { InnerInputsComponentProps, SingleField } from '../../core/field/SingleField'
import { renderers } from '../../core/RenderInputFunctions'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../../core/ContextHOC'
import { Moment } from 'moment'
import { trans } from '../../core/DateUtils'

class Component extends React.Component<InnerInputsComponentProps<domain.AnubisCoverage>> {
    onCodeChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { code: event.currentTarget.value }))
    }

    onLimitAmountChange = (limitAmount: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { limitAmount }))
    }

    onDeductibleAmountChange = (deductibleAmount: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { deductibleAmount }))
    }

    onCultChange = (name: React.FormEvent<HTMLInputElement>) => {
        const cult = { ...this.props.value.cult }
        cult.name = name.currentTarget.value
        this.props.onChange(Object.assign({}, this.props.value, { cult }))
    }

    onCultDateChange = (e: Moment) => {
        const cult = { ...this.props.value.cult }
        cult.date = trans.toDate(e)
        this.props.onChange(Object.assign({}, this.props.value, { cult }))
    }

    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={this.props.value.code}
                    contextName='AnubisCoverage'
                    modelFieldName='code'
                    onChange={this.onCodeChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.limitAmount}
                    contextName='AnubisCoverage'
                    modelFieldName='limitAmount'
                    onChange={this.onLimitAmountChange}
                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.deductibleAmount}
                    contextName='AnubisCoverage'
                    modelFieldName='deductibleAmount'
                    onChange={this.onDeductibleAmountChange}
                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.cult.name}
                    contextName='AnubisCoverage'
                    modelFieldName='cultName'
                    onChange={this.onCultChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={trans.toMoment(this.props.value.cult.date)}
                    contextName='AnubisCoverage'
                    modelFieldName='cultDate'
                    onChange={this.onCultDateChange}
                    renderInput={renderers.date}
                />
            </Row>
        )
    }
}

export const AnubisCoverage = withMetadata(Component)
