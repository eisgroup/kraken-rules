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
    RuleEvaluationResults,
    EntryPointResult,
    RuleOverride,
    ContextFieldInfo,
    ErrorMessage,
    FieldMetadata,
    FieldMetadataResult,
} from 'kraken-engine-api'

import { EntryPointReducer } from '../Reducer'
import { Localization } from '../Localization'
import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult
import { payloadResultTypeChecker } from '../PayloadResultTypeChecker'

/**
 * Processes rule evaluation information from {@link EntryPointResult}, reducing it to list of
 * metadata entries for each distinct field
 */
export class FieldMetadataReducer implements EntryPointReducer<Record<string, FieldMetadata>> {
    readonly #isRuleOverridden: RuleOverride.IsRuleOverridden
    readonly #errorMessageFrom: (rer: ValidationRuleEvaluationResult, contextInfo: ContextFieldInfo) => ErrorMessage

    constructor(
        isRuleOverridden?: RuleOverride.IsRuleOverridden,
        validationMessageProvider?: Localization.ValidationMessageProvider,
    ) {
        this.#isRuleOverridden = isRuleOverridden ? isRuleOverridden : () => false

        if (validationMessageProvider) {
            this.#errorMessageFrom = rer => Localization.resolveMessage(rer.payloadResult, validationMessageProvider)
        } else {
            this.#errorMessageFrom = rer =>
                Localization.resolveMessage(rer.payloadResult, Localization.defaultValidationMessages)
        }
    }

    /**
     * @override
     */
    reduce(results: EntryPointResult): Record<string, FieldMetadata> {
        const fieldMetadataResults: Record<string, FieldMetadata> = {}
        const fieldResults = results.getFieldResults()
        for (const id in fieldResults) {
            if (Object.prototype.hasOwnProperty.call(fieldResults, id)) {
                const fieldResult = fieldResults[id]
                let isDisabled
                let isHidden
                const validationResults: FieldMetadataResult[] = []
                for (const ruleResult of fieldResult.ruleResults) {
                    if (!RuleEvaluationResults.isNotApplicable(ruleResult)) {
                        const payloadResult = ruleResult.payloadResult
                        // resolve accessibility
                        if (!isDisabled && payloadResultTypeChecker.isAccessibility(payloadResult)) {
                            isDisabled = !payloadResult.accessible
                        }
                        // resolve visibility
                        if (!isHidden && payloadResultTypeChecker.isVisibility(payloadResult)) {
                            isHidden = !payloadResult.visible
                        }
                        // resolve validation
                        if (RuleEvaluationResults.isValidation(ruleResult)) {
                            const error = this.#errorMessageFrom(ruleResult, fieldResult.contextFieldInfo)
                            const isOverridden =
                                RuleEvaluationResults.overrideInfoTypeChecker.isOverrideApplicable(
                                    ruleResult.overrideInfo,
                                ) && this.#isRuleOverridden(ruleResult.overrideInfo.overrideContext)
                            validationResults.push({
                                rawTemplateVariables: ruleResult.payloadResult.message?.rawTemplateVariables ?? [],
                                ruleName: ruleResult.ruleInfo.ruleName,
                                errorCode: error.errorCode,
                                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                                errorMessage: error.errorMessage!,
                                templateVariables: error.templateVariables,
                                payloadResult,
                                severity: ruleResult.payloadResult.validationSeverity,
                                isFailed:
                                    ruleResult.payloadResult.success !== undefined
                                        ? !ruleResult.payloadResult.success
                                        : undefined,
                                isOverridable: ruleResult.overrideInfo.overridable,
                                isOverridden,
                            })
                        }
                    }
                }

                // register results
                fieldMetadataResults[id] = {
                    id,
                    info: {
                        getContextInstanceId: () => fieldResult.contextFieldInfo.contextId,
                        getContextName: () => fieldResult.contextFieldInfo.contextName,
                    },
                    resolvedTargetPath: fieldResult.contextFieldInfo.fieldPath,
                    isDisabled,
                    isHidden,
                    ruleResults: validationResults,
                    fieldType: fieldResult.contextFieldInfo.fieldType,
                }
            }
        }
        return fieldMetadataResults
    }
}
