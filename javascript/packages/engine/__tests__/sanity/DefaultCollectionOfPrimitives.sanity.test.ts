/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { sanityEngine } from './_SanityEngine'
import { TestProduct } from 'kraken-test-product'
import Policy = TestProduct.kraken.testproduct.domain.Policy

describe('EngineSanityDefaultCollectionOfPrimitivesTest', () => {
    it('shouldDefaultCollectionOfPrimitivesAndCoerceCollectionAndNumberTypes', () => {
        const policy: Policy = {
            cd: 'Policy',
            id: 'Policy-1',
            state: 'US',
            policyValue: {
                amount: 5.5,
                currency: 'USD',
            },
            insured: {
                cd: 'Insured',
                id: 'Insured-1',
            },
        }
        sanityEngine.evaluate(policy, 'DefaultCollectionOfPrimitives')

        expect(policy.policies).toStrictEqual(['US', 'A', 'B'])
        expect(policy.insured.childrenAges).toStrictEqual([1, 10.5, 5.5])
    })
})
