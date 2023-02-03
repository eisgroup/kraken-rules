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
    ConditionEvaluation,
    FieldEvaluationResult,
    PayloadResultType,
    RuleEvaluationResults,
} from 'kraken-engine-api'

import { k_toHaveExpressionsFailures } from '../toHaveExpressionsFailures'
import { Payloads } from 'kraken-model'
import PayloadType = Payloads.PayloadType
import { MockEntryPointResult } from './MockEntryPointResult'

expect.extend({ k_toHaveExpressionsFailures })

describe('.k_toHaveExpressionsFailures', () => {
    it('Should pass when no expression errors are present', () => {
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
                        type: PayloadResultType.ASSERTION,
                        success: true,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                },
            ],
        }

        const epResult = new MockEntryPointResult(results)
        expect(epResult).k_toHaveExpressionsFailures(0)
    })
    it('Should fail when condition expression errors are present', () => {
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
                        error: {
                            kind: 2,
                            error: {
                                message: 'error occured',
                                severity: 'critical',
                            },
                        },
                    },
                    payloadResult: {
                        type: PayloadResultType.ASSERTION,
                        success: true,
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                },
            ],
        }

        const epResult = new MockEntryPointResult(results)
        expect(epResult).k_toHaveExpressionsFailures(1)
    })
    it('Should fail when payload expression errors are present', () => {
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
                        type: PayloadResultType.ASSERTION,
                        success: false,
                        error: {
                            kind: 2,
                            error: {
                                message: 'error occured',
                                severity: 'critical',
                            },
                        },
                    } as AssertionPayloadResult,
                    ruleInfo: {
                        context: 'context',
                        ruleName: 'rname',
                        targetPath: 'field',
                        payloadtype: PayloadType.ASSERTION,
                    },
                },
            ],
        }

        const epResult = new MockEntryPointResult(results)
        expect(epResult).k_toHaveExpressionsFailures(1)
    })
})
