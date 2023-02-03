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

import { trans } from '../core/DateUtils'
import { renderers } from '../core/RenderInputFunctions'

import { InnerInputsComponentProps, SingleField, ManyFields } from '../core/field/SingleField'
import { AddressInfo } from './AddressInfo'
import { AnubisCoverage } from './coverages/AnubisCoverage'

import { FullCoverage } from './coverages/FullCoverage'
import { CollCoverage } from './coverages/CollCoverage'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../core/ContextHOC'
import { ContextDefinitionInfo } from '../core/field/ContextDefinitionInfo'
import { RRCoverage } from './coverages/RRCoverage'

class Component extends React.Component<InnerInputsComponentProps<domain.Vehicle>> {
    onIncludedChange = (included: boolean) => {
        this.props.onChange(Object.assign({}, this.props.value, { included }))
    }

    onModelChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { model: event.currentTarget.value }))
    }

    onModelYearChange = (modelYear: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { modelYear }))
    }

    onNewValueChange = (newValue: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { newValue }))
    }

    onCostNewChange = (costNew: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { costNew }))
    }

    onDeclaredAnnualMilesChange = (declaredAnnualMiles: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { declaredAnnualMiles }))
    }

    onOdometerReadingChange = (odometerReading: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { odometerReading }))
    }

    onNumDaysDrivenPerWeek = (numDaysDrivenPerWeek: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { numDaysDrivenPerWeek }))
    }

    onPurchaseDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { purchasedDate: trans.toDate(e) }))
    }

    onServiceHistoryChange = (histories: Date[]) => {
        this.props.onChange(Object.assign({}, { ...this.props.value }, { serviceHistory: histories }))
    }

    onAddressInfoChange = (addressInfo: domain.AddressInfo) => {
        this.props.onChange(Object.assign({}, this.props.value, { addressInfo }))
    }

    onCOLLCoverageChange = (collCoverage: domain.COLLCoverage) => {
        this.props.onChange(
            Object.assign({}, this.props.value, {
                collCoverages: this.props.value.collCoverages.map(coverage =>
                    Object.assign({}, coverage, collCoverage),
                ),
            }),
        )
    }

    onAnubisCoverageChange = (index: number) => (anubisCoverage: domain.AnubisCoverage) => {
        const anubisCoveragesCopy = this.props.value.anubisCoverages.slice()
        anubisCoveragesCopy[index] = anubisCoverage
        this.props.onChange(Object.assign({}, { ...this.props.value }, { anubisCoverages: anubisCoveragesCopy }))
    }

    onRRCoverageChange = (rentalCoverage: domain.RRCoverage) => {
        this.props.onChange(Object.assign({}, this.props.value, { rentalCoverage }))
    }

    onFullCoverageChange = (fullCoverage: domain.FullCoverage) => {
        this.props.onChange(
            Object.assign({}, this.props.value, {
                fullCoverages: this.props.value.fullCoverages.map(coverage =>
                    Object.assign({}, coverage, fullCoverage),
                ),
            }),
        )
    }

    render(): JSX.Element {
        return (
            <div>
                <Row>
                    <ContextDefinitionInfo contextName='Vehicle' />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.included}
                        contextName='Vehicle'
                        modelFieldName='included'
                        onChange={this.onIncludedChange}
                        renderInput={renderers.boolean}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.model}
                        contextName='Vehicle'
                        modelFieldName='model'
                        onChange={this.onModelChange}
                        renderInput={renderers.input}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.modelYear}
                        contextName='Vehicle'
                        modelFieldName='modelYear'
                        onChange={this.onModelYearChange}
                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.newValue}
                        contextName='Vehicle'
                        modelFieldName='newValue'
                        onChange={this.onNewValueChange}
                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.costNew}
                        contextName='Vehicle'
                        modelFieldName='costNew'
                        onChange={this.onCostNewChange}
                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.declaredAnnualMiles}
                        contextName='Vehicle'
                        modelFieldName='declaredAnnualMiles'
                        onChange={this.onDeclaredAnnualMilesChange}
                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.odometerReading}
                        contextName='Vehicle'
                        modelFieldName='odometerReading'
                        onChange={this.onOdometerReadingChange}
                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={this.props.value.numDaysDrivenPerWeek}
                        contextName='Vehicle'
                        modelFieldName='numDaysDrivenPerWeek'
                        onChange={this.onNumDaysDrivenPerWeek}
                        renderInput={renderers.inputNumber}
                    />
                    <SingleField
                        id={this.props.id}
                        value={trans.toMoment(this.props.value.purchasedDate)}
                        contextName='Vehicle'
                        modelFieldName='purchasedDate'
                        onChange={this.onPurchaseDateChange}
                        renderInput={renderers.date}
                    />
                    <ManyFields
                        id={this.props.id}
                        value={this.props.value.serviceHistory}
                        contextName='Vehicle'
                        modelFieldName='serviceHistory'
                        onChange={this.onServiceHistoryChange}
                        renderInput={renderers.date}
                    />
                </Row>
                <ContextDefinitionInfo contextName='AddressInfo' />
                <AddressInfo
                    id={this.props.value.addressInfo.id}
                    value={this.props.value.addressInfo}
                    onChange={this.onAddressInfoChange}
                />
                <ContextDefinitionInfo contextName='COLLCoverage' />
                <CollCoverage
                    id={this.props.value.collCoverages[0].id}
                    value={this.props.value.collCoverages[0]}
                    onChange={this.onCOLLCoverageChange}
                />
                <ContextDefinitionInfo contextName='AnubisCoverage' />
                <AnubisCoverage
                    id={this.props.value.anubisCoverages[0].id}
                    value={this.props.value.anubisCoverages[0]}
                    onChange={this.onAnubisCoverageChange(0)}
                />
                <AnubisCoverage
                    id={this.props.value.anubisCoverages[1].id}
                    value={this.props.value.anubisCoverages[1]}
                    onChange={this.onAnubisCoverageChange(1)}
                />
                <ContextDefinitionInfo contextName='RRCoverage' />
                <RRCoverage
                    id={this.props.value.rentalCoverage.id}
                    value={this.props.value.rentalCoverage}
                    onChange={this.onRRCoverageChange}
                />
                <h3>Full coverage</h3>
                <ContextDefinitionInfo contextName='FullCoverage' />
                <FullCoverage
                    id={this.props.value.fullCoverages[0].id}
                    value={this.props.value.fullCoverages[0]}
                    onChange={this.onFullCoverageChange}
                />
            </div>
        )
    }
}

export const Vehicle = withMetadata(Component)
