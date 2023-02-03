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
    AccessibilityPayloadResult,
    AssertionPayloadResult,
    ConditionEvaluation,
    FieldEvaluationResult,
    PayloadResultType,
    RuleEvaluationResults,
    VisibilityPayloadResult,
} from 'kraken-engine-api'

import { k_toMatchResultsStats } from '../toMatchResultsStats'
import { Payloads } from 'kraken-model'
import { MockEntryPointResult } from './MockEntryPointResult'
import PayloadType = Payloads.PayloadType
import ValidationSeverity = Payloads.Validation.ValidationSeverity

expect.extend({ k_toMatchResultsStats })

describe('.k_toMatchResultsStats', () => {
    it('Should pass when no results are present', () => {
        const results = {} as Record<string, FieldEvaluationResult>
        results['field'] = {
            contextFieldInfo: {
                contextId: 'id',
                contextName: 'context',
                fieldName: 'field',
                fieldType: 'boolean',
                fieldPath: 'field',
            },
            ruleResults: [],
        }

        const epResult = new MockEntryPointResult(results)
        expect(epResult).k_toMatchResultsStats({ total: 0, hidden: 0, disabled: 0, critical: 0, warning: 0, info: 0 })
    })
    it('Should pass when results are present', () => {
        const results = {} as Record<string, FieldEvaluationResult>
        results['field'] = {
            contextFieldInfo: {
                contextId: 'id',
                contextName: 'context',
                fieldName: 'field',
                fieldType: 'boolean',
                fieldPath: 'field',
            },
            ruleResults: [
                {
                    kind: RuleEvaluationResults.Kind.REGULAR,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.VISIBILITY,
                        visible: false,
                    } as VisibilityPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.VISIBILITY,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.REGULAR,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ACCESSIBILITY,
                        accessible: false,
                    } as AccessibilityPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ACCESSIBILITY,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: false,
                        validationSeverity: ValidationSeverity.critical,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: false,
                        validationSeverity: ValidationSeverity.info,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: false,
                        validationSeverity: ValidationSeverity.warning,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                },
            ],
        }

        const epResult = new MockEntryPointResult(results)
        expect(epResult).k_toMatchResultsStats({ total: 5, hidden: 1, disabled: 1, critical: 1, warning: 1, info: 1 })
    })
    it('Should pass when results are present, but not applicable', () => {
        const results = {} as Record<string, FieldEvaluationResult>
        results['field'] = {
            contextFieldInfo: {
                contextId: 'id',
                contextName: 'context',
                fieldName: 'field',
                fieldType: 'boolean',
                fieldPath: 'field',
            },
            ruleResults: [
                {
                    kind: RuleEvaluationResults.Kind.REGULAR,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.VISIBILITY,
                        visible: true,
                    } as VisibilityPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.VISIBILITY,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.REGULAR,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ACCESSIBILITY,
                        accessible: true,
                    } as AccessibilityPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ACCESSIBILITY,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: true,
                        validationSeverity: ValidationSeverity.critical,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: true,
                        validationSeverity: ValidationSeverity.info,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                },
                {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: {
                        conditionEvaluation: ConditionEvaluation.APPLICABLE,
                        error: undefined,
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: true,
                        validationSeverity: ValidationSeverity.warning,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                },
            ],
        }

        const epResult = new MockEntryPointResult(results)
        expect(epResult).k_toMatchResultsStats({ total: 5, hidden: 0, disabled: 0, critical: 0, warning: 0, info: 0 })
    })
})
