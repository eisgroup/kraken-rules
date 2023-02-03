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

import { InnerInputsComponentProps, SingleField } from '../core/field/SingleField'
import { renderers } from '../core/RenderInputFunctions'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../core/ContextHOC'

class Component extends React.Component<InnerInputsComponentProps<domain.PersonInfo>> {
    onFirstNameChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { firstName: event.currentTarget.value }))
    }

    onLastNameChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { lastName: event.currentTarget.value }))
    }

    onAgeChange = (age: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { age }))
    }

    onOccupationChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { occupation: event.currentTarget.value }))
    }

    onSameHomeAddressChange = (sameHomeAddress: boolean) => {
        this.props.onChange(Object.assign({}, this.props.value, { sameHomeAddress }))
    }

    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={this.props.value.firstName}
                    contextName='PersonInfo'
                    modelFieldName='firstName'
                    onChange={this.onFirstNameChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.lastName}
                    contextName='PersonInfo'
                    modelFieldName='lastName'
                    onChange={this.onLastNameChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.age}
                    contextName='PersonInfo'
                    modelFieldName='age'
                    onChange={this.onAgeChange}
                    renderInput={renderers.inputNumber}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.occupation}
                    contextName='PersonInfo'
                    modelFieldName='occupation'
                    onChange={this.onOccupationChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.sameHomeAddress}
                    contextName='PersonInfo'
                    modelFieldName='sameHomeAddress'
                    onChange={this.onSameHomeAddressChange}
                    renderInput={renderers.boolean}
                />
            </Row>
        )
    }
}

export const PersonInfo = withMetadata(Component)
