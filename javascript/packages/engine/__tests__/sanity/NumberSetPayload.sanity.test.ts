/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

describe('EngineSanityNumberSetPayloadTest', () => {
    it('shouldEvaluateNumberSetRulesOnPolicyWithValidValues', () => {
        const policy = {
            id: 'Policy-1',
            cd: 'Policy',
            termDetails: {
                id: 'TermDetails-1',
                cd: 'TermDetails',
                termNo: 1,
            },
            billingInfo: {
                id: 'BillingInfo-1',
                cd: 'BillingInfo',
                creditCardInfo: {
                    id: 'CreditCardInfo-1',
                    cd: 'CreditCardInfo',
                    cardCreditLimitAmount: {
                        amount: 505,
                        currency: 'USD',
                    },
                },
            },
            riskItems: [
                {
                    id: 'Vehicle-1',
                    cd: 'Vehicle',
                    newValue: 7999.99,
                },
            ],
        }
        const results = sanityEngine.evaluate(policy, 'NumberSet')

        expect(results).k_toMatchResultsStats({ total: 3, critical: 0 })
    })
    it('shouldEvaluateNumberSetRulesOnPolicyWithValidNotValidValues', () => {
        const policy = {
            id: 'Policy-1',
            cd: 'Policy',
            termDetails: {
                id: 'TermDetails-1',
                cd: 'TermDetails',
                termNo: 0,
            },
            billingInfo: {
                id: 'BillingInfo-1',
                cd: 'BillingInfo',
                creditCardInfo: {
                    id: 'CreditCardInfo-1',
                    cd: 'CreditCardInfo',
                    cardCreditLimitAmount: {
                        amount: 500,
                        currency: 'USD',
                    },
                },
            },
            riskItems: [
                {
                    id: 'Vehicle-1',
                    cd: 'Vehicle',
                    newValue: 7999.999,
                },
            ],
        }
        const results = sanityEngine.evaluate(policy, 'NumberSet')

        expect(results).k_toMatchResultsStats({ total: 3, critical: 3 })
    })
})
