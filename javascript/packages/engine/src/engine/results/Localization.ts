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

import { RuleEvaluationResults } from "../../dto/RuleEvaluationResults";
import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult;
import { ErrorMessage, payloadResultTypeChecker } from "./PayloadResult";
import { ContextFieldInfo } from "../../dto/ContextFieldInfo";

export namespace Localization {

    export interface ProvidedErrorMessageContext {
        /**
         * Context definition name of entity with failed rule
         */
        contextName: string;
        /**
         * Context definition id of entity with failed rule
         */
        contextId: string;
        /**
         * Length of string, if rule assert it.
         */
        length?: number;
        /**
         * regular expression of string, if rule assert it.
         */
        regexp?: string;

    }

    export interface ProvidedErrorMessage {
        errorCode: string;
        errorMessage: (context: ProvidedErrorMessageContext) => string;
    }

    /**
     * Provides default messages to validation payload results in case
     * validation rules do not have code and/or message specified
     */
    export interface MessageProvider {
        /**
         * default message to set for reg exp validation result
         * In {@link ProvidedErrorMessageContext} length property will have
         * value from rule payload regular expression pattern
         */
        regexp: ProvidedErrorMessage;
        /**
         * default message to set for mandatory validation result
         */
        mandatory: ProvidedErrorMessage;
        /**
         * default message to set for empty field validation result
         */
        empty: ProvidedErrorMessage;
        /**
         * default message to set for string length validation result
         * In {@link ProvidedErrorMessageContext} length property will have
         * value from rule payload length limit
         */
        length: ProvidedErrorMessage;
        /**
         * default message to set for failing assertion rule
         */
        assert: ProvidedErrorMessage;
        /**
         * default message to set for failing array length rule
         */
        size: ProvidedErrorMessage;
        /**
         * default message to set for failing array length range rule
         */
        sizeRange: ProvidedErrorMessage;
    }

    /**
     * These are default error messages and default error codes,
     * that are same as in java engine
     */
    export const defaultMessages: MessageProvider = {
        assert: {
            errorCode: "rule-assertion-error",
            errorMessage: () => "Assertion failed"
        },
        mandatory: {
            errorCode: "rule-mandatory-error",
            errorMessage: () => "Field is mandatory"
        },
        empty: {
            errorCode: "rule-mandatory-empty-error",
            errorMessage: () => "Field must be empty"
        },
        length: {
            errorCode: "rule-length-error",
            errorMessage: c => "Text must not be longer than " + c.length
        },
        regexp: {
            errorCode: "rule-regexp-error",
            errorMessage: c => "Field must match regular expression pattern: " + c.regexp
        },
        size: {
            errorCode: "rule-size-error",
            errorMessage: () => "Array length is invalid"
        },
        sizeRange: {
            errorCode: "rule-size-range-error",
            errorMessage: () => "Array length is invalid"
        }
    };

    /**
     * Resolves {@link Localization.Message} from {@link RuleEvaluationResult}
     * @param messages error codes and messages
     */
    export const errorMessage = (
        messages: MessageProvider
    ) => (
        rer: ValidationRuleEvaluationResult,
        contextInfo: ContextFieldInfo
    ): ErrorMessage => {
            const { payloadResult } = rer;
            if (payloadResultTypeChecker.isValidation(payloadResult)
                && payloadResult.message
                && payloadResult.message.errorCode
                && payloadResult.message.errorMessage
            ) {
                return payloadResult.message;
            }

            function defaultCodeIfAbsent(code: string): string {
                return payloadResult.message?.errorCode ?? code;
            }

            if (payloadResultTypeChecker.isAssertion(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.assert.errorCode),
                    errorMessage: messages.assert.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName
                    }),
                    templateVariables: []
                };
            }
            if (payloadResultTypeChecker.isLength(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.length.errorCode),
                    errorMessage: messages.length.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                        length: payloadResult.length
                    }),
                    templateVariables: []
                };
            }
            if (payloadResultTypeChecker.isSize(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.size.errorCode),
                    errorMessage: messages.size.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName
                    }),
                    templateVariables: []
                };
            }
            if (payloadResultTypeChecker.isSizeRange(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.sizeRange.errorCode),
                    errorMessage: messages.sizeRange.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName
                    }),
                    templateVariables: []
                };
            }
            if (payloadResultTypeChecker.isRegExp(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.regexp.errorCode),
                    errorMessage: messages.regexp.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName,
                        regexp: payloadResult.regExp
                    }),
                    templateVariables: []
                };
            }
            if (payloadResultTypeChecker.isMandatory(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.mandatory.errorCode),
                    errorMessage: messages.mandatory.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName
                    }),
                    templateVariables: []
                };
            }
            if (payloadResultTypeChecker.isEmpty(payloadResult)) {
                return {
                    errorCode: defaultCodeIfAbsent(messages.empty.errorCode),
                    errorMessage: messages.empty.errorMessage({
                        contextId: contextInfo.contextId,
                        contextName: contextInfo.contextName
                    }),
                    templateVariables: []
                };
            }
            throw new Error("Unknown validation payload type encountered");
        };
}
