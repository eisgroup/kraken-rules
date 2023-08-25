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

import { sanityEngine } from './_SanityEngine'
import { RuleEvaluationResults } from 'kraken-engine-api'
import isValidation = RuleEvaluationResults.isValidation
import { TestProduct } from 'kraken-test-product'
import { Moneys } from '../../src/engine/runtime/expressions/math/Moneys'

describe('EngineSanityRawMessageParameterTest', () => {
    it('shouldReturnRawMessageParameters', () => {
        const policy: TestProduct.kraken.testproduct.domain.Policy = {
            id: 'Policy-1',
            cd: 'Policy',
            policyValue: {
                amount: 10.01,
                currency: 'USD',
            },
        }

        const result = sanityEngine
            .evaluate(policy, 'RawTemplates')
            .getAllRuleResults()
            .find(r => r.ruleInfo.ruleName === 'RawTemplates_R01_Policy.state')
        if (!isValidation(result)) {
            throw 'Should be validation result'
        }

        const rawTemplateVariables = result.payloadResult.message.rawTemplateVariables

        const asserts = [
            value => typeof value === 'boolean',
            value => typeof value === 'boolean',
            value => value === undefined,
            value => typeof value === 'string',
            value => typeof value === 'string',
            value => typeof value === 'number',
            value => value instanceof Date,
            value => value instanceof Date,
            value => typeof value === 'string',
            value => Moneys.isMoney(value),
        ]
        for (let index = 0; index < asserts.length; index++) {
            const variable = rawTemplateVariables[index]
            const assert = asserts[index]
            expect(assert(variable)).toBe(true)
        }
    })
})
