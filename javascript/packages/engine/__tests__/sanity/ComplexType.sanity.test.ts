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

describe('Complex Field Type Sanity Test', () => {
    it('should evaluate rules on complex field types with no errors', () => {
        const policy = {
            id: '12',
            cd: 'Policy',
            billingInfo: {
                id: '1',
                cd: 'BillingInfo',
                creditCardInfo: {
                    id: '2',
                    cd: 'CreditCardInfo',
                },
            },
            riskItems: [
                {
                    id: '20',
                    cd: 'Vehicle',
                    addressInfo: {
                        id: '30',
                        cd: 'AddressInfo',
                        addressLines: [
                            {
                                id: '50',
                                cd: 'AddressLine1',
                            },
                            {
                                id: '51',
                                cd: 'AddressLine2',
                            },
                        ],
                    },
                    anubisCoverages: [
                        {
                            id: '40',
                            cd: 'AnubisCoverage',
                            limitAmount: 10,
                        },
                        {
                            id: '41',
                            cd: 'AnubisCoverage',
                            limitAmount: 10,
                        },
                    ],
                },
            ],
        }

        const results = sanityEngine.evaluate(policy, 'complex-field-type-test')

        expect(results).k_toMatchResultsStats({ total: 2, critical: 1 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
})
