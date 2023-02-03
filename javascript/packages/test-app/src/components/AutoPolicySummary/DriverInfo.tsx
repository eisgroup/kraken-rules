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
import { trans } from '../core/DateUtils'
import { renderers } from '../core/RenderInputFunctions'

import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { withMetadata } from '../core/ContextHOC'

class Component extends React.Component<InnerInputsComponentProps<domain.DriverInfo>> {
    onDriverTypeChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { driverType: event.currentTarget.value }))
    }

    onConvictedChange = (convicted: boolean) => {
        this.props.onChange(Object.assign({}, this.props.value, { convicted }))
    }

    onTrainingCompletionDateChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { trainingCompletionDate: trans.toDate(e) }))
    }

    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={this.props.value.driverType}
                    contextName='DriverInfo'
                    modelFieldName='driverType'
                    onChange={this.onDriverTypeChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={trans.toMoment(this.props.value.trainingCompletionDate)}
                    contextName='DriverInfo'
                    modelFieldName='trainingCompletionDate'
                    onChange={this.onTrainingCompletionDateChange}
                    renderInput={renderers.date}
                />
            </Row>
        )
    }
}

export const DriverInfo = withMetadata(Component)
