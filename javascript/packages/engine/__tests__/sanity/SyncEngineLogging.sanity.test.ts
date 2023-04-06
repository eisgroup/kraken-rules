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
import { KrakenConfig } from '../../src/config'
import { sanityEngine } from './_SanityEngine'
import { TestProduct } from 'kraken-test-product'

type ThisWithKraken = typeof global & { Kraken: KrakenConfig }

let previousEnvironment

beforeEach(() => {
    ;(global as ThisWithKraken).Kraken.logger.clear()
    previousEnvironment = process.env.NODE_ENV
    jest.useFakeTimers('modern').setSystemTime(new Date('2023-01-01T00:00:00Z'))
})

afterEach(() => {
    ;(global as ThisWithKraken).Kraken.logger.clear()
    process.env.NODE_ENV = previousEnvironment

    jest.useRealTimers()
})

describe('SyncEngine', () => {
    it('Should debug log in development environment', () => {
        ;(global as ThisWithKraken).Kraken.logger.debug = true
        process.env.NODE_ENV = 'development'

        const policy: TestProduct.kraken.testproduct.domain.Policy = {
            id: 'Policy-1',
            cd: 'Policy',
            state: 'OT',
            termDetails: {
                id: 'TermDetails-1',
                cd: 'TermDetails',
                termEffectiveDate: new Date('1999-01-01'),
            },
            accessTrackInfo: {
                id: 'AccessTrackInfo-1',
                cd: 'AccessTrackInfo',
            },
            transactionDetails: {
                id: 'TransactionDetails-1',
                cd: 'TransactionDetails',
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
                    costNew: 30000,
                    collCoverages: [
                        {
                            id: 'COLLCoverage-1',
                            cd: 'COLLCoverage',
                            code: '123',
                            limitAmount: 15,
                            deductibleAmount: 25,
                        },
                    ],
                },
            ],
            coverage: {
                id: 'CarCoverage-1',
                cd: 'CarCoverage',
                limitAmount: 10000,
            },
        }

        sanityEngine.evaluate(policy, 'TracerSnapshotTest')

        const logs = (global as ThisWithKraken).Kraken.logger.logs

        expect(logs.join('\n')).toMatchSnapshot()
    })
})
