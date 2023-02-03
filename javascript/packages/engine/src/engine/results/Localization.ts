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
    RuleEvaluationResults,
    ContextFieldInfo,
} from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult
import UsageType = Payloads.Validation.UsageType
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { payloadResultTypeChecker } from './PayloadResultTypeChecker'

export namespace Localization {
    /**
     * @deprecated since 1.24.0 because does not support localization. Use {@link ValidationMessageProvider} instead.
     */
    export interface ProvidedErrorMessageContext {
        /**
         * Context definition name of entity with failed rule
         */
        contextName: string
        /**
         * Context definition id of entity with failed rule
         */
        contextId: string
        /**
         * Length of string, if rule assert it.
         */
        length?: number
        /**
         * regular expression of string, if rule assert it.
         */
        regexp?: string
    }

    /**
     * @deprecated since 1.24.0 because does not support localization. Use {@link ValidationMessageProvider} instead.
     */
    export interface ProvidedErrorMessage {
        errorCode: string
        errorMessage: (context: ProvidedErrorMessageContext) => string
    }

    /**
     * Provides default messages to validation payload results in case
     * validation rules do not have code and/or message specified
     *
     * @deprecated since 1.24.0 because does not support localization. Use {@link ValidationMessageProvider} instead.
     */
    export interface MessageProvider {
        /**
         * default message to set for reg exp validation result
         * In {@link ProvidedErrorMessageContext} length property will have
         * value from rule payload regular expression pattern
         */
        regexp: ProvidedErrorMessage
        /**
         * default message to set for mandatory validation result
         */
        mandatory: ProvidedErrorMessage
        /**
         * default message to set for empty field validation result
         */
        empty: ProvidedErrorMessage
        /**
         * default message to set for string length validation result
         * In {@link ProvidedErrorMessageContext} length property will have
         * value from rule payload length limit
         */
        length: ProvidedErrorMessage
        /**
         * default message to set for failing assertion rule
         */
        assert: ProvidedErrorMessage
        /**
         * default message to set for failing array length rule
         */
        size: ProvidedErrorMessage
        /**
         * default message to set for failing array length range rule
         */
        sizeRange: ProvidedErrorMessage
    }

    /**
     * These are default error messages and default error codes,
     * that are same as in java engine
     *
     * @deprecated since 1.24.0 because does not support localization. Use {@link defaultValidationMessages} instead.
     */
    export const defaultMessages: MessageProvider = {
        assert: {
            errorCode: 'rule-assertion-error',
            errorMessage: () => 'Assertion failed',
        },
        mandatory: {
            errorCode: 'rule-mandatory-error',
            errorMessage: () => 'Field is mandatory',
        },
        empty: {
            errorCode: 'rule-mandatory-empty-error',
            errorMessage: () => 'Field must be empty',
        },
        length: {
            errorCode: 'rule-length-error',
            errorMessage: c => 'Text must not be longer than ' + c.length,
        },
        regexp: {
            errorCode: 'rule-regexp-error',
            errorMessage: c => 'Field must match regular expression pattern: ' + c.regexp,
        },
        size: {
            errorCode: 'rule-size-error',
            errorMessage: () => 'Array length is invalid',
        },
        sizeRange: {
            errorCode: 'rule-size-range-error',
            errorMessage: () => 'Array length is invalid',
        },
    }

    /**
     * Resolves {@link ErrorMessage} from {@link RuleEvaluationResult}
     * @param messages error codes and messages
     * @deprecated since 1.24.0 because does not support localization. Use {@link message} instead.
     */
    export const errorMessage =
        (messages: MessageProvider) =>
        (rer: ValidationRuleEvaluationResult, contextInfo: ContextFieldInfo): ErrorMessage => {
            const { payloadResult } = rer
            if (
                payloadResultTypeChecker.isValidation(payloadResult) &&
                payloadResult.message &&
                payloadResult.message.errorCode &&
                payloadResult.message.errorMessage
            ) {
                return payloadResult.message
            }

            function defaultCodeIfAbsent(code: string): string {
                return payloadResult.message?.errorCode ?? code
            }

            if (payloadResultTypeChecker.isAssertion(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.assert.errorCode),
                    errorMessage: messages.assert.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                    }),
                    templateVariables: [],
                }
            }
            if (payloadResultTypeChecker.isLength(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.length.errorCode),
                    errorMessage: messages.length.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                        length: payloadResult.length,
                    }),
                    templateVariables: [],
                }
            }
            if (payloadResultTypeChecker.isSize(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.size.errorCode),
                    errorMessage: messages.size.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                    }),
                    templateVariables: [],
                }
            }
            if (payloadResultTypeChecker.isSizeRange(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.sizeRange.errorCode),
                    errorMessage: messages.sizeRange.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                    }),
                    templateVariables: [],
                }
            }
            if (payloadResultTypeChecker.isRegExp(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.regexp.errorCode),
                    errorMessage: messages.regexp.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                        regexp: payloadResult.regExp,
                    }),
                    templateVariables: [],
                }
            }
            if (payloadResultTypeChecker.isMandatory(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.mandatory.errorCode),
                    errorMessage: messages.mandatory.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                    }),
                    templateVariables: [],
                }
            }
            if (payloadResultTypeChecker.isEmpty(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.empty.errorCode),
                    errorMessage: messages.empty.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                    }),
                    templateVariables: [],
                }
            }
            throw new Error('Unknown validation payload type encountered')
        }

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
            templateVariables: payloadResult.message?.errorMessage
                ? payloadResult.message?.templateVariables
                : renderTemplateParameters(defaultMessage.parameters ?? []),
        }
    }

    function renderTemplateParameters(parameters: unknown[]): string[] {
        return parameters.map(ExpressionEvaluator.render)
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
        throw new Error('Unknown validation payload type encountered: ' + p.type)
    }
}
