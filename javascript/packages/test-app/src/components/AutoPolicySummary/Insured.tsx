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

import { InnerInputsComponentProps, SingleField, ManyFields } from '../core/field/SingleField'
import { renderers } from '../core/RenderInputFunctions'

import { BillingAddress } from './BillingAddres'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../core/ContextHOC'
import { ContextDefinitionInfo } from '../core/field/ContextDefinitionInfo'

class Component extends React.Component<InnerInputsComponentProps<domain.Insured>> {
    onChildrenAgesChange = (childrenAges: string[]) => {
        this.props.onChange(Object.assign({}, this.props.value, { childrenAges }))
    }

    onHaveChildrenChange = (haveChildren: boolean) => {
        this.props.onChange(Object.assign({}, this.props.value, { haveChildren }))
    }

    onNameChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { name: event.currentTarget.value }))
    }

    onBillingAddressChange = (billingAddress: domain.BillingAddress) => {
        this.props.onChange(Object.assign({}, this.props.value, { addressInfo: billingAddress }))
    }

    render(): JSX.Element {
        return (
            <div>
                <Row>
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.haveChildren}
                        contextName='Insured'
                        modelFieldName='haveChildren'
                        onChange={this.onHaveChildrenChange}
                        renderInput={renderers.boolean}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.name}
                        contextName='Insured'
                        modelFieldName='name'
                        onChange={this.onNameChange}
                        renderInput={renderers.input}
                    />
                    <ManyFields
                        id={this.props.id}
                        value={this.props.value.childrenAges}
                        contextName='Insured'
                        modelFieldName='childrenAges'
                        onChange={this.onChildrenAgesChange}
                        renderInput={renderers.input}
                    />
                </Row>
                <ContextDefinitionInfo contextName='BillingAddress' />
                <BillingAddress
                    id={this.props.value.addressInfo.id}
                    value={this.props.value.addressInfo}
                    onChange={this.onBillingAddressChange}
                />
            </div>
        )
    }
}
export const Insured = withMetadata(Component)
