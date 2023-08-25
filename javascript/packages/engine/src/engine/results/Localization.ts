/* eslint-disable no-inner-declarations */
/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
    ErrorMessage,
    LengthPayloadResult,
    RegExpPayloadResult,
    SizePayloadResult,
    SizeRangePayloadResult,
    UsagePayloadResult,
    ValidationPayloadResult,
    ValueListPayloadResult,
    NumberSetPayloadResult,
} from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import UsageType = Payloads.Validation.UsageType
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { payloadResultTypeChecker } from './PayloadResultTypeChecker'

export namespace Localization {
    /**
     * Provides default messages for validation payload results in case validation rules do not have code
     * and/or message specified
     * @author mulevicius
     * @since 1.24.0
     */
    export interface ValidationMessageProvider {
        /**
         * @param payloadResult Result of usage payload, can be used to parametrize validation message.
         * @return Default error message to set for usage validation result.
         * Can be further customized based on {@link UsagePayloadResult#usageType()}.
         * This is invoked only when validation severity is ERROR.
         */
        usageErrorMessage: (payloadResult: UsagePayloadResult) => ValidationMessage
        /**
         * @param payloadResult Result of regular expression payload, can be used to parametrize validation message.
         * @return Default error message to set for regular expression validation result.
         * This is invoked only when validation severity is ERROR.
         */
        regExpErrorMessage: (payloadResult: RegExpPayloadResult) => ValidationMessage
        /**
         * @param payloadResult Result of collection size payload, can be used to parametrize validation message.
         * @return Default error message to set for collection size validation result.
         * This is invoked only when validation severity is ERROR.
         */
        sizeErrorMessage: (payloadResult: SizePayloadResult) => ValidationMessage
        /**
         * @param payloadResult Result of assertion payload, can be used to parametrize validation message.
         * @return Default error message to set for assertion validation result.
         * This is invoked only when validation severity is ERROR.
         */
        assertionErrorMessage: (payloadResult: AssertionPayloadResult) => ValidationMessage
        /**
         * @param payloadResult Result of string length payload, can be used to parametrize validation message.
         * @return Default error message to set for string length validation result.
         * This is invoked only when validation severity is ERROR.
         */
        lengthErrorMessage: (payloadResult: LengthPayloadResult) => ValidationMessage
        /**
         * @param payloadResult Result of collection size range payload, can be used to parametrize validation message.
         * @return Default error message to set for collection size range validation result.
         * This is invoked only when validation severity is ERROR.
         */
        sizeRangeErrorMessage: (payloadResult: SizeRangePayloadResult) => ValidationMessage

        /**
         * @param payloadResult Result of number set payload, can be used to parametrize validation message.
         * @return Default error message to set for number set validation result.
         * This is invoked only when validation severity is ERROR.
         */
        numberSetErrorMessage: (payloadResult: NumberSetPayloadResult) => ValidationMessage
        /**
         * @param payloadResult Result of value list payload, can be used to parametrize validation message.
         * @return Default error message to set for value list validation result.
         * This is invoked only when validation severity is ERROR.
         */
        valueListErrorMessage: (payloadResult: ValueListPayloadResult) => ValidationMessage
    }

    /**
     * Represents default message that will be set in rule validation result when rule itself do not have specific
     * message specified.
     *
     * @author mulevicius
     * @since 1.24.0
     */
    export interface ValidationMessage {
        code: string
        message: string
        parameters?: unknown[]
    }

    export const defaultValidationMessages: ValidationMessageProvider = {
        assertionErrorMessage(): Localization.ValidationMessage {
            return {
                code: 'rule-assertion-error',
                message: 'Assertion failed',
            }
        },
        lengthErrorMessage(payloadResult: LengthPayloadResult): Localization.ValidationMessage {
            return {
                code: 'rule-length-error',
                message: 'Text must not be longer than {{0}}',
                parameters: [payloadResult.length],
            }
        },
        regExpErrorMessage(payloadResult: RegExpPayloadResult): Localization.ValidationMessage {
            return {
                code: 'rule-regexp-error',
                message: 'Field must match regular expression pattern: {{0}}',
                parameters: [payloadResult.regExp],
            }
        },
        sizeErrorMessage(): Localization.ValidationMessage {
            return {
                code: 'rule-size-error',
                message: 'Array length is invalid',
            }
        },
        sizeRangeErrorMessage(): Localization.ValidationMessage {
            return {
                code: 'rule-size-range-error',
                message: 'Array length is invalid',
            }
        },
        usageErrorMessage(payloadResult: UsagePayloadResult): Localization.ValidationMessage {
            if (payloadResult.usageType === UsageType.mandatory) {
                return {
                    code: 'rule-mandatory-error',
                    message: 'Field is mandatory',
                }
            }
            if (payloadResult.usageType === UsageType.mustBeEmpty) {
                return {
                    code: 'rule-mandatory-empty-error',
                    message: 'Field must be empty',
                }
            }
            throw new Error('Unknown usage type encountered: ' + payloadResult.usageType)
        },
        numberSetErrorMessage(payloadResult: NumberSetPayloadResult): Localization.ValidationMessage {
            const min = payloadResult.min
            const max = payloadResult.max
            const step = payloadResult.step

            if (min != undefined && max != undefined && step != undefined) {
                return {
                    code: 'number-set-min-max-step-error',
                    message: 'Value must be in interval between {{0}} and {{1}} inclusively with increment {{2}}',
                    parameters: [min, max, step],
                }
            }
            if (min != undefined && max == undefined && step != undefined) {
                return {
                    code: 'number-set-min-step-error',
                    message: 'Value must be {{0}} or larger with increment {{1}}',
                    parameters: [min, step],
                }
            }
            if (min == undefined && max != undefined && step != undefined) {
                return {
                    code: 'number-set-max-step-error',
                    message: 'Value must be {{0}} or smaller with decrement {{1}}',
                    parameters: [max, step],
                }
            }
            if (min != undefined && max != undefined && step == undefined) {
                return {
                    code: 'number-set-min-max-error',
                    message: 'Value must be in interval between {{0}} and {{1}} inclusively',
                    parameters: [min, max],
                }
            }
            if (min != undefined && max == undefined && step == undefined) {
                return {
                    code: 'number-set-min-error',
                    message: 'Value must be {{0}} or larger',
                    parameters: [min],
                }
            }
            if (min == undefined && max != undefined && step == undefined) {
                return {
                    code: 'number-set-max-error',
                    message: 'Value must be {{0}} or smaller',
                    parameters: [max],
                }
            }
            throw new Error('Invalid number set payload encountered: min, max and step are undefined')
        },
        valueListErrorMessage(payloadResult: ValueListPayloadResult): Localization.ValidationMessage {
            return {
                code: 'value-list-error',
                message: 'Value must be one of: {{0}}',
                parameters: [payloadResult.valueList.values.map(value => value.toString()).join(', ')],
            }
        },
    }

    /**
     * Resolves {@link ErrorMessage} from {@link RuleEvaluationResult}
     * @param payloadResult result to resolve default error message for
     * @param defaultProvider provider of default error codes and messages, can use {@link defaultValidationMessages}.
     */
    export function resolveMessage(
        payloadResult: ValidationPayloadResult,
        defaultProvider: ValidationMessageProvider,
    ): ErrorMessage {
        const defaultMessage = defaultForPayloadType(payloadResult, defaultProvider)
        return {
            errorCode: payloadResult.message?.errorCode ?? defaultMessage.code,
            errorMessage: payloadResult.message?.errorMessage ?? defaultMessage.message,
            rawTemplateVariables: payloadResult.message?.rawTemplateVariables ?? [],
            templateVariables: payloadResult.message?.errorMessage
                ? payloadResult.message?.templateVariables
                : renderTemplateParameters(defaultMessage.parameters ?? []),
        }
    }

    function renderTemplateParameters(parameters: unknown[]): string[] {
        return parameters.map(ExpressionEvaluator.renderTemplateParameter)
    }

    function defaultForPayloadType(
        p: ValidationPayloadResult,
        defaultProvider: ValidationMessageProvider,
    ): ValidationMessage {
        if (payloadResultTypeChecker.isAssertion(p)) {
            return defaultProvider.assertionErrorMessage(p)
        }
        if (payloadResultTypeChecker.isLength(p)) {
            return defaultProvider.lengthErrorMessage(p)
        }
        if (payloadResultTypeChecker.isSize(p)) {
            return defaultProvider.sizeErrorMessage(p)
        }
        if (payloadResultTypeChecker.isSizeRange(p)) {
            return defaultProvider.sizeRangeErrorMessage(p)
        }
        if (payloadResultTypeChecker.isRegExp(p)) {
            return defaultProvider.regExpErrorMessage(p)
        }
        if (payloadResultTypeChecker.isUsage(p)) {
            return defaultProvider.usageErrorMessage(p)
        }
        if (payloadResultTypeChecker.isNumberSet(p)) {
            return defaultProvider.numberSetErrorMessage(p)
        }
        if (payloadResultTypeChecker.isValueList(p)) {
            return defaultProvider.valueListErrorMessage(p)
        }
        throw new Error('Unknown validation payload type encountered: ' + p.type)
    }
}
