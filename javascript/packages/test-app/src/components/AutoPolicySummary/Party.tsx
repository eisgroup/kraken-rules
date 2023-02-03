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

import { renderers } from '../core/RenderInputFunctions'

import { InnerInputsComponentProps, SingleField } from '../core/field/SingleField'
import { PartyRole } from './PartyRole'
import { PersonInfo } from './PersonInfo'
import { AddressInfo } from './AddressInfo'
import { DriverInfo } from './DriverInfo'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../core/ContextHOC'
import { ContextDefinitionInfo } from '../core/field/ContextDefinitionInfo'
import { EntityBox } from '../core/EntityBox'

class Component extends React.Component<InnerInputsComponentProps<domain.Party>> {
    onRelationToPrimaryInsuredChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(
            Object.assign({}, this.props.value, { relationToPrimaryInsured: event.currentTarget.value }),
        )
    }

    onPartyRoleChange = (partyRole: domain.PartyRole) => {
        this.props.onChange(Object.assign({}, this.props.value, { roles: [{ ...partyRole }] }))
    }

    onPersonInfoChange = (personInfo: domain.PersonInfo) => {
        this.props.onChange(Object.assign({}, this.props.value, { personInfo }))
    }

    onAddressInfoChange = (addressInfo: domain.AddressInfo) => {
        this.props.onChange(
            Object.assign({}, this.props.value, {
                personInfo: {
                    ...this.props.value.personInfo,
                    addressInfo: { ...addressInfo },
                },
            }),
        )
    }

    onDriverInfoChange = (driverInfo: domain.DriverInfo) => {
        this.props.onChange(Object.assign({}, this.props.value, { driverInfo }))
    }

    render(): JSX.Element {
        return (
            <div>
                <ContextDefinitionInfo contextName='Party' />
                <Row>
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.relationToPrimaryInsured}
                        contextName='Party'
                        modelFieldName='relationToPrimaryInsured'
                        onChange={this.onRelationToPrimaryInsuredChange}
                        renderInput={renderers.input}
                    />
                </Row>

                <ContextDefinitionInfo contextName='PartyRole' />
                <PartyRole
                    id={this.props.value.roles[0].id}
                    value={this.props.value.roles[0]}
                    onChange={this.onPartyRoleChange}
                />

                <ContextDefinitionInfo contextName='DriverInfo' />
                <DriverInfo
                    id={this.props.value.driverInfo.id}
                    value={this.props.value.driverInfo}
                    onChange={this.onDriverInfoChange}
                />

                <EntityBox title='PersonInfo'>
                    <ContextDefinitionInfo contextName='PersonInfo' />
                    <PersonInfo
                        id={this.props.value.personInfo.id}
                        value={this.props.value.personInfo}
                        onChange={this.onPersonInfoChange}
                    />

                    <ContextDefinitionInfo contextName='AddressInfo' />
                    <AddressInfo
                        id={this.props.value.personInfo.addressInfo.id}
                        value={this.props.value.personInfo.addressInfo}
                        onChange={this.onAddressInfoChange}
                    />
                </EntityBox>
            </div>
        )
    }
}

export const Party = withMetadata(Component)
