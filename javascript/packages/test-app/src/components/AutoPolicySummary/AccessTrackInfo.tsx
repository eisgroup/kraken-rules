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

class Component extends React.Component<InnerInputsComponentProps<domain.AccessTrackInfo>> {
    onCreatedOnChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { createdOn: trans.toDate(e) }))
    }

    onCreatedByChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { createdBy: event.currentTarget.value }))
    }

    onUpdatedOnChange = (e: Moment) => {
        this.props.onChange(Object.assign({}, this.props.value, { updatedOn: trans.toDate(e) }))
    }

    onUpdatedByChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { updatedBy: event.currentTarget.value }))
    }

    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={trans.toMoment(this.props.value.createdOn)}
                    contextName='Policy'
                    modelFieldName='createdOn'
                    onChange={this.onCreatedOnChange}
                    renderInput={renderers.date}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.createdBy}
                    contextName='Policy'
                    modelFieldName='createdBy'
                    onChange={this.onCreatedByChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={trans.toMoment(this.props.value.updatedOn)}
                    contextName='Policy'
                    modelFieldName='updatedOn'
                    onChange={this.onUpdatedOnChange}
                    renderInput={renderers.date}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.updatedBy}
                    contextName='Policy'
                    modelFieldName='updatedBy'
                    onChange={this.onUpdatedByChange}
                    renderInput={renderers.input}
                />
            </Row>
        )
    }
}

export const AccessTrackInfo = withMetadata(Component)
