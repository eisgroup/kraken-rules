/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import { Payloads } from 'kraken-model'
import { PayloadResult } from '../PayloadResult'
import { ContextInstanceInfo } from '../../contexts/info/ContextInstanceInfo'

export interface FieldMetadataResult {
    ruleName: string
    errorCode: string
    errorMessage: string
    templateVariables: string[]
    payloadResult: PayloadResult
    isOverridable: boolean
    isOverridden: boolean
    severity?: Payloads.Validation.ValidationSeverity
    /**
     * if field is undefined, then rule evaluation was ignored
     */
    isFailed?: boolean
}

/**
 * Metadata that will be returned in map {field:metadata}. It contains all info after evaluating rules on field.
 */
export interface FieldMetadata {
    id: string
    resolvedTargetPath: string
    info: ContextInstanceInfo
    isHidden?: boolean
    isDisabled?: boolean
    ruleResults: FieldMetadataResult[]
    fieldType: string
}
