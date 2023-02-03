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
import ValidationSeverity = Payloads.Validation.ValidationSeverity

import { Reducer, optional } from 'declarative-js'
import flat = Reducer.flat
import groupBy = Reducer.groupBy
import Map = Reducer.Map

import {
    RuleEvaluationResults,
    EntryPointResult,
    FieldEvaluationResult,
    ErrorAwarePayloadResult,
    ContextInstanceInfo,
    ConditionEvaluation,
} from 'kraken-engine-api'

import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult
import isNotApplicable = RuleEvaluationResults.isNotApplicable
import isValidation = RuleEvaluationResults.isValidation

export const EntryPointResultUtils = {
    reduce,
}

/**
 * Validation status grouped by validation error severity
 */
export interface ValidationStatus {
    critical: ResultErrorMessage[]
    warning: ResultErrorMessage[]
    info: ResultErrorMessage[]
}

export class ResultErrorMessage {
    constructor(
        /**
         * Name of the rule which generated error message
         */
        public readonly ruleName: string,
        /**
         * Information about field, instance containing this field,
         * is constructed by {@link ContextInstanceInfoResolver}
         */
        public readonly info: ContextInstanceInfo,
        /**
         * Error message severity
         */
        public readonly severity: ValidationSeverity,
        /**
         * Error message code
         */
        public readonly messageCode?: string,
        /**
         * Error message text
         */
        public readonly message?: string,
    ) {}
}

export function reduce(results: EntryPointResult): ValidationStatus {
    const getValidationResults = (fieldResults: FieldEvaluationResult) =>
        fieldResults.ruleResults
            .filter(value => isApplicable(value))
            .filter(value => isNotEvaluatedWithError(value))
            .filter(value => isInValid(value))

    const validationStatusMap = Object.values(results.getFieldResults())
        .map(fieldResult => ({
            contextFieldInfo: fieldResult.contextFieldInfo,
            validationResults: getValidationResults(fieldResult),
        }))
        .map(t =>
            t.validationResults.map(result => {
                if (!RuleEvaluationResults.isValidation(result)) {
                    throw new Error('expected result to be validation result')
                }
                const { payloadResult } = result
                return new ResultErrorMessage(
                    result.ruleInfo.ruleName,

                    {
                        getContextInstanceId: () => t.contextFieldInfo.contextId,
                        getContextName: () => t.contextFieldInfo.contextName,
                    },
                    payloadResult.validationSeverity ?? ValidationSeverity.critical,
                    payloadResult.message?.errorCode,
                    payloadResult.message?.errorMessage,
                )
            }),
        )
        .reduce(flat, [])
        .reduce(groupBy('severity'), Map())
    return {
        critical: optional(validationStatusMap.get(ValidationSeverity.critical)).toArray(),
        info: optional(validationStatusMap.get(ValidationSeverity.info)).toArray(),
        warning: optional(validationStatusMap.get(ValidationSeverity.warning)).toArray(),
    }
}

function isNotEvaluatedWithError(rule: RuleEvaluationResult): boolean {
    return !isNotApplicable(rule) && (rule.payloadResult as ErrorAwarePayloadResult).error === undefined
}

function isInValid(rule: RuleEvaluationResult): boolean {
    return isValidation(rule) && rule.payloadResult.success === false
}

function isApplicable(rule: RuleEvaluationResult): boolean {
    return rule.conditionEvaluationResult.conditionEvaluation === ConditionEvaluation.APPLICABLE
}
