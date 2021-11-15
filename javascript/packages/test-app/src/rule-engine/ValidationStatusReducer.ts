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
import {
  ContextInstanceInfo,
  EntryPointReducer,
  RuleEvaluationResults,
  EntryPointResult,
  FieldEvaluationResult,
  payloadResultTypeChecker,
  ErrorAwarePayloadResult,
  ValidationPayloadResult,
  ConditionEvaluationResult,
  Localization, ErrorMessage
} from "kraken-typescript-engine";
import { ContextFieldInfo } from "kraken-typescript-engine/src/dto/ContextFieldInfo";
import defaultMessages = Localization.defaultMessages;
import errorMessage = Localization.errorMessage;
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
    rer: RuleEvaluationResults.ValidationRuleEvaluationResult,
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
        .filter(ValidationStatusReducer.isValidationPayloadResult)
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
          const error = this.errorMessageFrom(
            it as RuleEvaluationResults.ValidationRuleEvaluationResult, t.contextFieldInfo
          );
          return new ResultErrorMessage(
            it.ruleInfo.ruleName,
            error.errorMessage,
            error.errorCode,
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

  private static isNotEvaluatedWithError(rule: RuleEvaluationResults.RegularRuleEvaluationResult): boolean {
    const isNotPresentError =
      (rule.payloadResult as ErrorAwarePayloadResult).error === undefined;
    return isNotPresentError;
  }

  private static isInValid(rule: RuleEvaluationResults.RegularRuleEvaluationResult): boolean {
    return !(rule.payloadResult as ValidationPayloadResult).success;
  }

  private static isApplicable(rule: RuleEvaluationResults.RegularRuleEvaluationResult): boolean {
    return ConditionEvaluationResult.isApplicable(
      rule.conditionEvaluationResult
    );
  }

  private static isValidationPayloadResult(
    rule: RuleEvaluationResults.RegularRuleEvaluationResult
  ): boolean {
    return rule && rule.payloadResult && payloadResultTypeChecker.isValidation(rule.payloadResult);
  }
}
