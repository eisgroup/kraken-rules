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

import {
    AssertionPayloadResult,
    DefaultValuePayloadResult,
    ErrorMessage,
    ExpressionEvaluationResult,
    LengthPayloadResult,
    PayloadResultType,
    RegExpPayloadResult,
    RuleEvent,
    SizePayloadResult,
    SizeRangePayloadResult,
    UsagePayloadResult,
    NumberSetPayloadResult,
    ValueListPayloadResult,
} from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'

export const payloadResultCreator = {
    assertionFail(error: ExpressionEvaluationResult.ErrorResult): AssertionPayloadResult {
        return { type: PayloadResultType.ASSERTION, error }
    },
    assertion(
        payload: Payloads.Validation.AssertionPayload,
        success: boolean,
        templateVariables: unknown[],
    ): AssertionPayloadResult {
        return {
            type: PayloadResultType.ASSERTION,
            success,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    length(
        payload: Payloads.Validation.LengthPayload,
        success: boolean,
        templateVariables: unknown[],
    ): LengthPayloadResult {
        return {
            type: PayloadResultType.LENGTH,
            success,
            length: payload.length,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    regexp(
        payload: Payloads.Validation.RegExpPayload,
        success: boolean,
        templateVariables: unknown[],
    ): RegExpPayloadResult {
        return {
            type: PayloadResultType.REGEXP,
            success,
            regExp: payload.regExp,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    size(payload: Payloads.Validation.SizePayload, success: boolean, templateVariables: unknown[]): SizePayloadResult {
        return {
            type: PayloadResultType.SIZE,
            success,
            min: payload.size,
            sizeOrientation: payload.orientation,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    sizeRange(
        payload: Payloads.Validation.SizeRangePayload,
        success: boolean,
        templateVariables: unknown[],
    ): SizeRangePayloadResult {
        return {
            type: PayloadResultType.SIZE_RANGE,
            success,
            min: payload.min,
            max: payload.max,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    usage(
        payload: Payloads.Validation.UsagePayload,
        success: boolean,
        templateVariables: unknown[],
    ): UsagePayloadResult {
        return {
            type: PayloadResultType.USAGE,
            usageType: payload.usageType,
            success,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    valueList(
        payload: Payloads.Validation.ValueListPayload,
        success: boolean,
        templateVariables: unknown[],
    ): ValueListPayloadResult {
        return {
            type: PayloadResultType.VALUE_LIST,
            valueList: payload.valueList,
            success,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
    default(events: RuleEvent[]): DefaultValuePayloadResult {
        return { type: PayloadResultType.DEFAULT, events }
    },
    defaultFail(error: ExpressionEvaluationResult.ErrorResult): DefaultValuePayloadResult {
        return { type: PayloadResultType.DEFAULT, error }
    },
    defaultNoEvents(): DefaultValuePayloadResult {
        return { type: PayloadResultType.DEFAULT, events: [] }
    },
    numberSet(
        payload: Payloads.Validation.NumberSetPayload,
        success: boolean,
        templateVariables: unknown[],
    ): NumberSetPayloadResult {
        return {
            type: PayloadResultType.NUMBER_SET,
            success,
            min: payload.min,
            max: payload.max,
            step: payload.step,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity,
        }
    },
}

function format(
    message: Payloads.Validation.ErrorMessage | undefined,
    rawTemplateVariables: unknown[],
): ErrorMessage | undefined {
    if (message === undefined) {
        return undefined
    }

    const templateVariables = rawTemplateVariables.map(ExpressionEvaluator.renderTemplateParameter)

    return {
        rawTemplateVariables,
        templateVariables,
        errorCode: message.errorCode,
        errorMessage:
            message.templateParts.length > 0 // inserts variables between array items in templateParts
                ? // the assumption is that 'templateVariables.length - 1 === templateParts.length'
                  message.templateParts.reduce((v1, v2, i) => v1 + templateVariables[i - 1] + v2)
                : undefined,
    }
}
