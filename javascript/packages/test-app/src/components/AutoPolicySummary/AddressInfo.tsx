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

export class Component extends React.Component<InnerInputsComponentProps<domain.AddressInfo>> {
    onDoNotSolicitChange = (doNotSolicit: boolean) => {
        this.props.onChange(Object.assign({}, this.props.value, { doNotSolicit }))
    }

    onCountryCdChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { countryCd: event.currentTarget.value }))
    }

    onPostalCodeChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { postalCode: event.currentTarget.value }))
    }

    onCityChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { city: event.currentTarget.value }))
    }

    onStreetChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { street: event.currentTarget.value }))
    }

    onStateCdChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { stateCd: event.currentTarget.value }))
    }

    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={this.props.value.doNotSolicit}
                    contextName='AddressInfo'
                    modelFieldName='doNotSolicit'
                    onChange={this.onDoNotSolicitChange}
                    renderInput={renderers.boolean}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.countryCd}
                    contextName='AddressInfo'
                    modelFieldName='countryCd'
                    onChange={this.onCountryCdChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.postalCode}
                    contextName='AddressInfo'
                    modelFieldName='postalCode'
                    onChange={this.onPostalCodeChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.city}
                    contextName='AddressInfo'
                    modelFieldName='city'
                    onChange={this.onCityChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.street}
                    contextName='AddressInfo'
                    modelFieldName='street'
                    onChange={this.onStreetChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.street}
                    contextName='AddressInfo'
                    modelFieldName='stateCd'
                    onChange={this.onStateCdChange}
                    renderInput={renderers.input}
                />
            </Row>
        )
    }
}

export const AddressInfo = withMetadata(Component)
