/* eslint-disable @typescript-eslint/no-non-null-assertion */
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
import { TestProduct } from 'kraken-test-product'
import Policy = TestProduct.kraken.testproduct.domain.Policy

describe('EngineSanityForbiddenFieldTest', () => {
    it('shouldApplyRuleOnFieldNotForbiddenAsTarget', () => {
        const policy: Policy = {
            id: 'Policy-1',
            cd: 'Policy',
            riskItems: [
                {
                    id: 'Vehicle-1',
                    cd: 'Vehicle',
                    addressInfo: {
                        id: 'AddressInfo-1',
                        cd: 'AddressInfo',
                    },
                },
            ],
        }

        const results = sanityEngine.evaluate(policy, 'ForbiddenField')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 })
    })
    it('shouldNotApplyRuleOnFieldForbiddenAsTarget', () => {
        const policy: Policy = {
            id: 'Policy-1',
            cd: 'Policy',
            riskItems: [
                {
                    id: 'Vehicle-1',
                    cd: 'Vehicle',
                    addressInfo: {
                        id: 'BillingAddress-1',
                        cd: 'BillingAddress',
                    },
                },
            ],
        }

        const results = sanityEngine.evaluate(policy, 'ForbiddenField')

        expect(results).k_toMatchResultsStats({ total: 0, critical: 0 })
    })
})
