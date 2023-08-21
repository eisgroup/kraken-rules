/* eslint-disable @typescript-eslint/no-non-null-assertion */
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
import { EntryPointResult, RuleEvaluationResults, ValidationPayloadResult } from 'kraken-engine-api'
import { dateFunctions } from '../../src/engine/runtime/expressions/functionLibrary/DateFunctions'

function format(date: string): string {
    return dateFunctions.Format(new Date(date), 'YYYY-MM-DD hh:mm:ss')
}

function result(fieldId: string, entryPointResult: EntryPointResult): ValidationPayloadResult {
    return (
        entryPointResult.getFieldResults()[fieldId]
            .ruleResults[0] as RuleEvaluationResults.ValidationRuleEvaluationResult
    ).payloadResult
}

describe('Engine Message Template Sanity Test', () => {
    const { empty } = sanityMocks
    it('should evaluate message template', () => {
        const policy = empty()
        policy.policyNumber = 'P00'
        policy.transactionDetails!.txEffectiveDate = new Date('2021-01-01T11:00:00Z')
        policy.riskItems = [
            { cd: 'Vehicle', id: '1', model: 'P01' },
            { cd: 'Vehicle', id: '2', model: 'P02' },
        ]
        const results = sanityEngine.evaluate(policy, 'Templates')

        expect(results).k_toMatchResultsStats({ total: 3, critical: 2 })

        const d1 = format('2021-01-01T10:00:00Z')
        const policyNumberResult = result('Policy:0:policyNumber', results)
        const policyNumberMessage = "Policy number 'P00' must be in vehicle models, but vehicle models are: [P01, P02]"
        expect(policyNumberResult.message?.errorMessage).toStrictEqual(policyNumberMessage)

        const d2 = format('2021-01-01T11:00:00Z')
        const policyTxEffectiveResult = result('Policy:0:txEffectiveDate', results)
        const txEffectiveMessage = `Transaction effective date must be later than ${d1} but was ${d2}`
        expect(policyTxEffectiveResult.message?.errorMessage).toStrictEqual(txEffectiveMessage)

        const d3 = format('2020-01-01T00:00:00')
        const policyStateResult = result('Policy:0:state', results)
        const policyStateMessage = `\${nothingtoseehere}  true false  string string 10.123 ${d3} ${d1} ABC`
        expect(policyStateResult.message?.errorMessage).toStrictEqual(policyStateMessage)
    })
})
