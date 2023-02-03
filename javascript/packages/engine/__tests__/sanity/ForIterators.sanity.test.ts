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

describe('Engine Sanity For Iterators Test', () => {
    const { empty } = sanityMocks
    it('should evaluate rules with forEach on anubisCoverages', () => {
        const policy = empty()
        policy.riskItems = [
            {
                cd: 'Vehicle',
                id: '999',
                anubisCoverages: [
                    { id: '001', cd: 'AnubisCoverage', limitAmount: 10 },
                    { id: '002', cd: 'AnubisCoverage', limitAmount: 10 },
                ],
            },
            {
                cd: 'Vehicle',
                id: '888',
                anubisCoverages: [{ id: '003', cd: 'AnubisCoverage', limitAmount: 10 }],
            },
        ]

        const results = sanityEngine.evaluate(policy, 'ForEach_EntryPoint')

        expect(policy.transactionDetails!.changePremium).toEqual(300)
        expect(results).k_toMatchResultsStats({ total: 3, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    }),
        it('should evaluate rules with forEach on anubisCoverages and fail', () => {
            const policy = empty()
            policy.riskItems = [
                {
                    cd: 'Vehicle',
                    id: '999',
                    anubisCoverages: [
                        { id: '001', cd: 'AnubisCoverage', limitAmount: 10 },
                        { id: '002', cd: 'AnubisCoverage', limitAmount: 20 },
                    ],
                },
                {
                    cd: 'Vehicle',
                    id: '888',
                    anubisCoverages: [{ id: '003', cd: 'AnubisCoverage', limitAmount: 10 }],
                },
            ]

            const results = sanityEngine.evaluate(policy, 'ForEach_EntryPoint')

            expect(policy.transactionDetails!.changePremium).toEqual(400)
            expect(results).k_toMatchResultsStats({ critical: 2 })
            expect(results).k_toHaveExpressionsFailures(0)
        }),
        it('should evaluate rules with forSome and forEvery on anubisCoverages', () => {
            const policy = empty()
            policy.policyNumber = 'Q0001'
            policy.riskItems = [
                {
                    cd: 'Vehicle',
                    id: '99',
                    anubisCoverages: [
                        {
                            id: '01',
                            cd: 'AnubisCoverage',
                            code: 'code1',
                            limitAmount: 10,
                            cult: { id: '11', name: 'CULT' },
                        },
                        {
                            id: '02',
                            cd: 'AnubisCoverage',
                            code: 'code2',
                            limitAmount: 20,
                            cult: { id: '12', name: 'CULT' },
                        },
                    ],
                },
                {
                    cd: 'Vehicle',
                    id: '89',
                    anubisCoverages: [
                        {
                            id: '03',
                            cd: 'AnubisCoverage',
                            code: 'code3',
                            limitAmount: 30,
                            cult: { id: '13', name: 'CULT' },
                        },
                    ],
                },
            ]

            const results = sanityEngine.evaluate(policy, 'ForSome_ForEvery_EntryPoint')

            expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
            expect(results).k_toHaveExpressionsFailures(0)
        }),
        it('should evaluate rules with forSome and forEvery on anubisCoverages and fail', () => {
            const policy = empty()
            policy.policyNumber = 'Q0001'
            policy.riskItems = [
                {
                    cd: 'Vehicle',
                    id: '99',
                    anubisCoverages: [
                        {
                            id: '01',
                            cd: 'AnubisCoverage',
                            code: 'code1',
                            limitAmount: 20,
                            cult: { id: '11', name: 'CULT' },
                        },
                        {
                            id: '02',
                            cd: 'AnubisCoverage',
                            code: 'code2',
                            limitAmount: 20,
                            cult: { id: '12', name: 'CULT' },
                        },
                    ],
                },
                {
                    cd: 'Vehicle',
                    id: '89',
                    anubisCoverages: [
                        {
                            id: '03',
                            cd: 'AnubisCoverage',
                            code: 'code3',
                            limitAmount: 20,
                            cult: { id: '13', name: 'CULT' },
                        },
                    ],
                },
            ]

            const results = sanityEngine.evaluate(policy, 'ForSome_ForEvery_EntryPoint')

            expect(results).k_toMatchResultsStats({ total: 1, critical: 1 })
            expect(results).k_toHaveExpressionsFailures(0)
        })
})
