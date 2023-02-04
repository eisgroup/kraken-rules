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
    ConditionEvaluation,
    DefaultValuePayloadResult,
    PayloadResultType,
    RuleEvaluationResults,
} from 'kraken-engine-api'
import { k_toBeApplied, k_toBeIgnored, k_toBeSkipped } from '../toMatchRuleStatus'
import Kind = RuleEvaluationResults.Kind
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult

expect.extend({ k_toBeSkipped, k_toBeIgnored, k_toBeApplied })

describe('toMatchRuleStatus', () => {
    it('should match when rule is applied', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.APPLICABLE,
            },
            payloadResult: {
                type: PayloadResultType.DEFAULT,
            } as DefaultValuePayloadResult,
        } as RuleEvaluationResult

        expect(ruleResult).not.k_toBeIgnored()
        expect(ruleResult).not.k_toBeSkipped()
        expect(ruleResult).k_toBeApplied()
    })
    it('should match when rule is skipped', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.NOT_APPLICABLE,
            },
        } as RuleEvaluationResult

        expect(ruleResult).not.k_toBeIgnored()
        expect(ruleResult).k_toBeSkipped()
        expect(ruleResult).not.k_toBeApplied()
    })
    it('should match when rule is ignored by due to condition error', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.ERROR,
            },
        } as RuleEvaluationResult

        expect(ruleResult).k_toBeIgnored()
        expect(ruleResult).not.k_toBeSkipped()
        expect(ruleResult).not.k_toBeApplied()
    })
    it('should match when rule is ignored doe to payload error', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.APPLICABLE,
            },
            payloadResult: {
                type: PayloadResultType.DEFAULT,
                error: {
                    kind: 2,
                    error: {
                        severity: 'critical',
                        message: 'error',
                    },
                },
            } as DefaultValuePayloadResult,
        } as RuleEvaluationResult

        expect(ruleResult).k_toBeIgnored()
        expect(ruleResult).not.k_toBeSkipped()
        expect(ruleResult).not.k_toBeApplied()
    })
})
