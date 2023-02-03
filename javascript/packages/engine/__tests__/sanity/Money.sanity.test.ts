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

describe('Engine Sanity Money', () => {
    const { empty } = sanityMocks
    it('should set default money amount 2500 USD', async () => {
        const data = empty()
        data.billingInfo!.creditCardInfo!.cardCreditLimitAmount = {
            amount: 0,
            currency: 'USD',
        }

        sanityEngine.evaluate(data, 'default-money')
        const money = data!.billingInfo!.creditCardInfo!.cardCreditLimitAmount!

        expect(money['amount']).toBe(2500)
        expect(money['currency']).toBe('USD')
    })
    it('should fail condition expression with empty data (limit > 2000)', async () => {
        const results = sanityEngine.evaluate(empty(), 'assert-money')
        expect(results.getApplicableResults()).toHaveLength(0)
        expect(results).k_toHaveExpressionsFailures(1)
    })
    it('should not execute rule due to failed condition (limit > 2000)', async () => {
        const data = empty()
        data.billingInfo!.creditCardInfo!.cardCreditLimitAmount = {
            amount: 1500,
            currency: 'USD',
        }
        const results = sanityEngine.evaluate(data, 'assert-money')
        expect(results.getApplicableResults()).toHaveLength(0)
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should execute money assertion rule (limit > 3000)', async () => {
        const data = empty()
        data.billingInfo!.creditCardInfo!.cardCreditLimitAmount = {
            amount: 2500,
            currency: 'USD',
        }
        const results = sanityEngine.evaluate(data, 'assert-money')
        expect(results).not.k_toHaveExpressionsFailures()
        expect(results.getApplicableResults()).toHaveLength(1)
        expect(results.getApplicableResults()[0]).not.k_toBeValidRuleResult()
    })
})
