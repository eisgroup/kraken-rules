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

import { Payloads } from "kraken-model";
import ValidationSeverity = Payloads.Validation.ValidationSeverity;

import { Reducer, optional } from "declarative-js";
import flat = Reducer.flat;
import groupBy = Reducer.groupBy;
import Map = Reducer.Map;

import { Localization } from "../src/engine/results/Localization";
import { ContextInstanceInfo } from "../src/engine/contexts/info/ContextInstanceInfo";
import { EntryPointReducer } from "../src/engine/results/Reducer";

import { RuleEvaluationResults } from "../src/dto/RuleEvaluationResults";
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult;
import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult;
import isNotApplicable = RuleEvaluationResults.isNotApplicable;
import isValidation = RuleEvaluationResults.isValidation;

import { EntryPointResult } from "../src/dto/EntryPointResult";
import { FieldEvaluationResult } from "../src/dto/FieldEvaluationResult";
import { ErrorMessage, ErrorAwarePayloadResult } from "../src/engine/results/PayloadResult";

import defaultMessages = Localization.defaultMessages;
import errorMessage = Localization.errorMessage;
import { ConditionEvaluationResult } from "../src/dto/ConditionEvaluationResult";
import { ContextFieldInfo } from "../src/dto/ContextFieldInfo";

/**
 * Validation status grouped by validation error severity
 */
export interface ValidationStatus {
    critical: ResultErrorMessage[];
    warning: ResultErrorMessage[];
    info: ResultErrorMessage[];
}

export class ResultErrorMessage {
    constructor(
        /**
         * Name of the rule which generated error message
         */
        public readonly ruleName: string,
        /**
         * Error message text
         */
        public readonly message: string,
        /**
         * Error message code
         */
        public readonly messageCode: string,
        /**
         * A list of formatted values resolved by evaluating template expressions,
         * Can be used when constructing localized validation message from template
         */
        public readonly templateVariables: string[],

        /**
         * Information about field, instance containing this field,
         * is constructed by {@link ContextInstanceInfoResolver}
         */
        public readonly info: ContextInstanceInfo,
        /**
         * Error message severity
         */
        public readonly severity: ValidationSeverity
    ) { }
}

/**
 * Processes rule evaluation information from {@link EntryPointResult}, reducing it to list of
 * metadata entries for each distinct field
 */
export class ValidationStatusReducer
    implements EntryPointReducer<ValidationStatus> {
    private readonly errorMessageFrom: (
        rer: ValidationRuleEvaluationResult,
        contextFieldInfo: ContextFieldInfo
    ) => ErrorMessage;
    constructor() {
        this.errorMessageFrom = errorMessage(defaultMessages);
    }

    /**
     * @override
     */
    reduce = (results: EntryPointResult): ValidationStatus => {
        const getValidationResults = (fieldResults: FieldEvaluationResult) =>
            fieldResults.ruleResults
                .filter(ValidationStatusReducer.isApplicable)
                .filter(ValidationStatusReducer.isNotEvaluatedWithError)
                .filter(ValidationStatusReducer.isInValid);
        const validationStatusMap = Object.values(results.getFieldResults())
            .map(fieldResult => ({
                contextFieldInfo: fieldResult.contextFieldInfo,
                validationResults: getValidationResults(fieldResult)
            }))
            .map(t =>
                t.validationResults.map(it => {
                    const error = this.errorMessageFrom(it as ValidationRuleEvaluationResult, t.contextFieldInfo);
                    return new ResultErrorMessage(
                        it.ruleInfo.ruleName,
                        error.errorMessage!,
                        error.errorCode,
                        error.templateVariables,
                        {
                            getContextInstanceId: () => t.contextFieldInfo.contextId,
                            getContextName: () => t.contextFieldInfo.contextName
                        },
                        RuleEvaluationResults.isValidation(it)
                            ? it.payloadResult.validationSeverity!
                            : ValidationSeverity.critical
                    );
                })
            )
            .reduce(flat, [])
            .reduce(groupBy("severity"), Map());
        return {
            critical: optional(
                validationStatusMap.get(ValidationSeverity.critical)
            ).toArray(),
            info: optional(
                validationStatusMap.get(ValidationSeverity.info)
            ).toArray(),
            warning: optional(
                validationStatusMap.get(ValidationSeverity.warning)
            ).toArray()
        };
    }

    private static isNotEvaluatedWithError(rule: RuleEvaluationResult): boolean {
        return !isNotApplicable(rule) && (rule.payloadResult as ErrorAwarePayloadResult).error === undefined;
    }

    private static isInValid(rule: RuleEvaluationResult): boolean {
        return isValidation(rule) && rule.payloadResult.success === false;
    }

    private static isApplicable(rule: RuleEvaluationResult): boolean {
        return ConditionEvaluationResult.isApplicable(
            rule.conditionEvaluationResult
        );
    }
}
