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

import { withValidation } from './ValidationHOC'
import { FieldComponent } from './FieldComponent'
import { MultipleField } from './MultipleField'
import { InputRendererProps } from '../RenderInputFunctions'
import { Moment } from 'moment'
import { withMetadata } from '../ContextHOC'

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

export type InputValue = string | number | Date | boolean | Moment

export interface SingleFieldProps {
    id: string
    value: InputValue | InputValue[]
    contextName: string
    modelFieldName: string
    info?: string
    onChange: (e: unknown) => void
    metadata: { [key: string]: ValidationMetadata }
    renderInput: (config: InputRendererProps<InputValue | InputValue[]>) => JSX.Element
    /**
     * set this property, when using renderer.select
     */
    selections?: string[]
}

export const SingleField = withMetadata(withValidation(FieldComponent))
export const ManyFields = withMetadata(withValidation(MultipleField))
