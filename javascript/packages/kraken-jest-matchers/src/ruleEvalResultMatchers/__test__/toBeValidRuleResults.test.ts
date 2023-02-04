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

import { k_toBeValidRuleResult } from '../toBeValidRuleResult'
import { ErrorAwarePayloadResult, RuleEvaluationResults } from 'kraken-engine-api'

import ApplicableRuleEvaluationResult = RuleEvaluationResults.ApplicableRuleEvaluationResult

expect.extend({ k_toBeValidRuleResult })

describe('.k_toBeValidRuleResult', () => {
    it('Should fail when error is present', () => {
        const ruleResult = {
            kind: 2,
            payloadResult: {
                error: {
                    error: {
                        severity: 'critical',
                    },
                    kind: 2,
                },
            } as ErrorAwarePayloadResult,
        } as ApplicableRuleEvaluationResult

        expect(ruleResult).not.k_toBeValidRuleResult()
    })

    it('Should fail when evaluating to false', () => {
        const ruleResult = {
            kind: 2,
            payloadResult: {
                success: false,
            },
        } as ApplicableRuleEvaluationResult

        expect(ruleResult).not.k_toBeValidRuleResult()
    })

    it('Should not fail when evaluating to false', () => {
        const ruleResult = {
            kind: 2,
            payloadResult: {
                success: true,
            },
        } as ApplicableRuleEvaluationResult

        expect(ruleResult).k_toBeValidRuleResult()
    })
})
