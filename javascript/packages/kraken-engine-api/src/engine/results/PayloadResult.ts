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

import { Payloads, ValueList } from 'kraken-model'
import { ExpressionEvaluationResult } from '../runtime/expressions/ExpressionEvaluationResult'
import { RuleEvent } from './Events'

export enum PayloadResultType {
    ASSERTION = 1,
    SIZE = 2,
    SIZE_RANGE = 4,
    DEFAULT = 8,
    LENGTH = 16,
    REGEXP = 32,
    USAGE = 64,
    ACCESSIBILITY = 128,
    VISIBILITY = 256,
    NUMBER_SET = 512,
    NOT_APPLICABLE = 1024,
    VALUE_LIST = 2048,
}

export interface PayloadResult {
    readonly type: PayloadResultType
}

export interface ErrorAwarePayloadResult {
    readonly error?: ExpressionEvaluationResult.ErrorResult
}

export interface ValidationPayloadResult extends PayloadResult {
    type: PayloadResultType
    success?: boolean
    message?: ErrorMessage
    validationSeverity?: Payloads.Validation.ValidationSeverity
}

export interface AccessibilityPayloadResult extends PayloadResult {
    type: PayloadResultType.ACCESSIBILITY
    accessible: boolean
}

export interface ErrorMessage {
    errorMessage?: string
    errorCode: string

    /**
     * A list of variables to be used in localized message templates.
     * Variables are extracted from rule validation message template and formatted for being displayed
     * in localized message template.
     */
    templateVariables: string[]

    /**
     * A list of variables to be used in localized message templates.
     * Variables are extracted from rule validation message template.
     * Values are not converted to a string.
     * These values provided directly from the expressions in the message template.
     * These values can be usefull if you want to localize the message and the template variables according to the locale.
     */
    rawTemplateVariables: unknown[]
}

export interface VisibilityPayloadResult extends PayloadResult {
    type: PayloadResultType.VISIBILITY
    visible: boolean
}

export interface DefaultValuePayloadResult extends PayloadResult, ErrorAwarePayloadResult {
    type: PayloadResultType.DEFAULT
    events?: RuleEvent[]
}

export interface AssertionPayloadResult extends ErrorAwarePayloadResult, ValidationPayloadResult {
    type: PayloadResultType.ASSERTION
}

export interface SizePayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.SIZE
    min: number
    sizeOrientation: Payloads.Validation.SizeOrientation
}

export interface SizeRangePayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.SIZE_RANGE
    min: number
    max: number
}

export interface LengthPayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.LENGTH
    length: number
}

export interface RegExpPayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.REGEXP
    regExp: string
}

export interface UsagePayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.USAGE
    usageType: Payloads.Validation.UsageType
}

export interface NumberSetPayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.NUMBER_SET
    min?: number
    max?: number
    step?: number
}

export interface ValueListPayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.VALUE_LIST
    valueList: ValueList
}
