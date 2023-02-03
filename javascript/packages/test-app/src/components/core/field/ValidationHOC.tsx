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
import { FieldProps } from './FieldComponent'
import { FieldModelInfo } from './FieldModelInfo'

import { Alert } from 'antd'
import 'antd/lib/alert/style'
import 'antd/lib/col/style'
import 'antd/lib/icon/style'
import 'antd/lib/tag/style'
import { SingleFieldProps, InputValue } from './SingleField'
import { FieldStructure } from './FieldStructure'
import { startCase } from 'lodash'
import { KRAKEN_MODEL_TREE_POLICY } from 'kraken-test-product-model-tree'

interface State {
    field: Field
}

interface Field {
    fieldType: string
    cardinality: string
}

function resolveFieldId(contextName: string, id: string, modelFieldName: string): string {
    return `${contextName}:${id}:${modelFieldName}`
}

export function withValidation(Node: React.ComponentType<FieldProps>) {
    return class WithValidation extends React.PureComponent<SingleFieldProps, State> {
        constructor(props: SingleFieldProps) {
            super(props)
            this.state = {
                field: {
                    fieldType: 'not found',
                    cardinality: 'not found',
                },
            }
        }

        componentDidMount(): void {
            const field = KRAKEN_MODEL_TREE_POLICY.contexts[this.props.contextName].fields[this.props.modelFieldName]
            this.setState({ field: { cardinality: field.cardinality, fieldType: field.fieldType } })
        }

        isApplicable = (): boolean => {
            const { metadata, contextName, id, modelFieldName } = this.props
            const key = resolveFieldId(contextName, id, modelFieldName)
            if (metadata && metadata[key]) {
                return !metadata[key].isApplicable
            }
            return false
        }

        inputStyle = () => {
            const { metadata, contextName, id, modelFieldName } = this.props
            const key = resolveFieldId(contextName, id, modelFieldName)
            if (metadata && metadata[key]) {
                const border = metadata[key].errMessage ? '2px solid red' : ''
                return { border }
            }
            return { display: 'table' }
        }

        isVisible = (): boolean => {
            const { metadata, contextName, id, modelFieldName } = this.props
            const key = resolveFieldId(contextName, id, modelFieldName)
            if (metadata && metadata[key]) {
                return metadata[key].isVisible
            }
            return true
        }

        renderError = (): JSX.Element => {
            const { metadata, contextName, id, modelFieldName } = this.props
            const key = resolveFieldId(contextName, id, modelFieldName)
            if (metadata && metadata[key] && metadata[key].errMessage) {
                return <Alert className='input-width' message={metadata[key].errMessage} type='error' showIcon={true} />
            }

            return null
        }

        modelInfo = () => (
            <FieldModelInfo
                id={this.props.id}
                contextName={this.props.contextName}
                fieldInfo={this.state.field}
                fieldName={this.props.modelFieldName}
            />
        )

        curryInput = (props: { value: InputValue; onChange: (_e: unknown) => void }) =>
            this.props.renderInput({
                disabled: this.isApplicable(),
                style: this.inputStyle(),
                value: props.value,
                onChange: props.onChange,
                className: 'input-width',
                selections: this.props.selections,
            })

        render(): JSX.Element {
            const { info } = this.props
            const isVisible = this.isVisible()
            return (
                <FieldStructure isVisible={isVisible}>
                    <Node
                        onChange={this.props.onChange}
                        value={this.props.value}
                        renderInput={this.curryInput}
                        error={this.renderError}
                        fieldInfo={this.modelInfo}
                        info={info}
                        label={startCase(this.props.modelFieldName)}
                    />
                </FieldStructure>
            )
        }
    }
}
