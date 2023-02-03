/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

import { sanityMocks } from './_AutoPolicyObject.mocks'
import { sanityEngine } from './_SanityEngine'
import { EntryPointResult, RuleEvaluationResults } from 'kraken-engine-api'
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult

describe('EngineSanityDefaultRulePriorityTest', () => {
    const { empty } = sanityMocks
    function ruleResult(result: EntryPointResult, ruleName: string): RuleEvaluationResult {
        return result.getAllRuleResults().find(r => r.ruleInfo.ruleName === ruleName)!
    }
    it('shouldEvaluateDefaultRulesByPriority_ApplyWithPriorityMIN', () => {
        const policy = empty()
        policy.riskItems = undefined

        const results = sanityEngine.evaluate(policy, 'DefaultRuleByPriority')

        expect(policy.policyNumber).toBe('MIN')
        expect(results.getAllRuleResults()).toHaveLength(7)

        expect(ruleResult(results, 'DefaultPolicyNumber-PriorityMAX-defaultExpressionError')).k_toBeIgnored()
        expect(ruleResult(results, 'DefaultPolicyNumber-Priority999-conditionExpressionError')).k_toBeIgnored()
        expect(ruleResult(results, 'DefaultPolicyNumber-Priority10')).k_toBeSkipped()
        expect(ruleResult(results, 'DefaultPolicyNumber-Priority0')).k_toBeSkipped()
        expect(ruleResult(results, 'DefaultPolicyNumber-Priority-10')).k_toBeSkipped()
        expect(ruleResult(results, 'DefaultPolicyNumber-PriorityMIN')).k_toBeApplied()
        expect(ruleResult(results, 'DefaultPolicyNumber-PriorityMIN2')).k_toBeSkipped()
    })
    it('shouldEvaluateDefaultRulesByPriority_ApplyWithPriority10', () => {
        const policy = empty()
        policy.riskItems = undefined
        policy.billingInfo!.accountName = '10'

        const results = sanityEngine.evaluate(policy, 'DefaultRuleByPriority')

        expect(policy.policyNumber).toBe('10')
        expect(results.getAllRuleResults()).toHaveLength(3)

        expect(ruleResult(results, 'DefaultPolicyNumber-PriorityMAX-defaultExpressionError')).k_toBeIgnored()
        expect(ruleResult(results, 'DefaultPolicyNumber-Priority999-conditionExpressionError')).k_toBeIgnored()
        expect(ruleResult(results, 'DefaultPolicyNumber-Priority10')).k_toBeApplied()
    })
    it('shouldEvaluateDefaultRulesByPriority_FailTwoDefaultRulesAppliedWithSamePriority', () => {
        const policy = empty()
        policy.riskItems = undefined
        policy.billingInfo!.accountName = 'MIN'

        expect(() => sanityEngine.evaluate(policy, 'DefaultRuleByPriority')).toThrowError(
            `On field 'Policy:0:policyNumber' applied '2' default rules: 'DefaultPolicyNumber-PriorityMIN2, DefaultPolicyNumber-PriorityMIN'. Only one default rule can be applied on the same field.`,
        )
    })
})
