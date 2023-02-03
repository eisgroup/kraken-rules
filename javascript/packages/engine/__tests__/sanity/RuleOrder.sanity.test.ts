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
import { mock } from '../mock'
import { FieldMetadataReducer } from '../../src'

describe('Engine Sanity rule order test', () => {
    const { empty } = sanityMocks
    it('should execute rules in order', () => {
        const policy = empty()
        policy.riskItems = [
            {
                cd: mock.modelTreeJson.contexts.Vehicle.name,
                id: 'v1',
                collCoverages: [
                    {
                        cd: mock.modelTreeJson.contexts.COLLCoverage.name,
                        id: 'cc1',
                    },
                ],
            },
        ]
        policy.insured = {
            cd: mock.modelTreeJson.contexts.Insured.name,
            id: 'i1',
            haveChildren: true,
        }
        policy.billingInfo = {
            creditCardInfo: {
                cd: mock.modelTreeJson.contexts.CreditCardInfo.name,
                id: 'cci1',
                billingAddress: {
                    cd: mock.modelTreeJson.contexts.BillingAddress.name,
                    id: 'ba1',
                },
            },
        }
        policy.parties = [
            {
                cd: mock.modelTreeJson.contexts.Party.name,
                id: 'p1',
            },
        ]
        const results = sanityEngine.evaluate(policy, 'RuleOrder')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(policy.riskItems[0].collCoverages![0].code).toBe('PartyPartyAddress')
        expect(results).k_toMatchResultsStats({ total: 7, critical: 0, warning: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it('should execute rules with complex field expressions in order', () => {
        const policy = empty()
        policy.billingInfo = {
            creditCardInfo: {
                cd: mock.modelTreeJson.contexts.CreditCardInfo.name,
                id: 'cci1',
            },
        }
        policy.riskItems = [
            {
                cd: mock.modelTreeJson.contexts.Vehicle.name,
                id: 'v1',
                rentalCoverage: {
                    cd: mock.modelTreeJson.contexts.RRCoverage.name,
                    id: 'rr1',
                },
            },
        ]
        policy.insured = {
            cd: mock.modelTreeJson.contexts.Insured.name,
            id: 'i1',
            addressInfo: {
                cd: mock.modelTreeJson.contexts.BillingAddress.name,
                id: 'adi1',
            },
        }
        policy.parties = [
            {
                cd: mock.modelTreeJson.contexts.Party.name,
                id: 'p1',
                roles: [
                    {
                        cd: mock.modelTreeJson.contexts.PartyRole.name,
                        id: 'pr1',
                    },
                ],
            },
        ]
        const results = sanityEngine.evaluate(policy, 'RuleOrderWithComplexField')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(policy.policyNumber).toBe('doNotSolicit is true')
        expect(policy.insured.addressInfo!.doNotSolicit).toBeTruthy()
        expect(policy.riskItems[0].rentalCoverage!.limitAmount).toBe(10)
        expect(policy.insured.addressInfo!.city).toBe('San Diego')
        expect(policy.parties[0].roles![0].limit).toBe(100)
        expect(results).k_toMatchResultsStats({ total: 6 })
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it('should set default coverage and transaction detail limit amounts to 1000', () => {
        const policy = empty()
        policy.transactionDetails = { id: '2', cd: 'TransactionDetails' }
        policy.coverage = {
            id: '1',
            cd: 'CarCoverage',
        }

        const results = sanityEngine.evaluate(policy, 'FunctionCheck-RulesUsingFunctionOrderCheck')

        expect(policy.coverage.limitAmount).toBe(1000)
        expect(policy.transactionDetails.totalLimit).toBe(1000)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('shouldEvaluateRuleOrderWithNoCycleInFunction', async () => {
        const policy = empty()
        policy.riskItems = [
            {
                cd: mock.modelTreeJson.contexts.Vehicle.name,
                id: 'v1',
                rentalCoverage: {
                    cd: mock.modelTreeJson.contexts.RRCoverage.name,
                    id: 'rr1',
                },
                collCoverages: [
                    {
                        cd: mock.modelTreeJson.contexts.COLLCoverage.name,
                        id: 'coll1',
                        limitAmount: 50,
                    },
                    {
                        cd: mock.modelTreeJson.contexts.COLLCoverage.name,
                        id: 'coll2',
                        limitAmount: 75,
                    },
                ],
            },
        ]

        const results = sanityEngine.evaluate(policy, 'RuleOrderWithNoCycleInFunction')

        expect(policy.policyNumber).toBe('Large')
        expect(policy.riskItems[0].rentalCoverage!.limitAmount).toBe(125)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
})
