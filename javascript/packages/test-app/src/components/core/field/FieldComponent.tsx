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

import { InputValue } from './SingleField'
import { Tooltip, Icon } from 'antd'

interface CurriedInputProps {
    value: InputValue | InputValue[]
    onChange: (e: InputValue | InputValue[] | object) => void
}

export interface FieldComponentProps {
    label: string
    info?: string
    renderInput: (props: CurriedInputProps) => JSX.Element
    fieldInfo?: () => void
    error?: () => void
    selections?: string[]
}

export type FieldProps = CurriedInputProps & FieldComponentProps

function label(props: FieldComponentProps): JSX.Element {
    return (
        <h4>
            {props.label}
            {props.info && (
                <Tooltip placement='right' title={props.info}>
                    <Icon type='question-circle-o' style={{ color: '#2f7dc5', marginLeft: '5px' }} />
                </Tooltip>
            )}
        </h4>
    )
}

export const FieldComponent = (props: FieldProps) => (
    <div>
        {label(props)}
        {props.renderInput({ ...props })}
        {props.fieldInfo()}
        {props.error && props.error()}
    </div>
)

export interface InnerInputsComponentProps<T> {
    metadata: { [key: string]: ValidationMetadata }
    id: string
    value: T
    onChange: (det: T) => void
}

export interface ValidationMetadata {
    isApplicable: boolean
    isVisible: boolean
    errMessage?: string
}
