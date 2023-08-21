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

import { TestProduct } from 'kraken-test-product'
import Policy = TestProduct.kraken.testproduct.domain.Policy
import { sanityEngine } from './_SanityEngine'

describe('EngineSanityDateFunctionsTest', () => {
    it('shouldEvaluateDateFunctionsShouldHandleDST', () => {
        const policy: Policy = {
            cd: 'Policy',
            id: 'Policy-1',
            transactionDetails: {
                cd: 'TransactionDetails',
                id: 'TransactionDetails-1',
            },
        }

        sanityEngine.evaluate(policy, 'DateFunctions-DefaultPlusDays')

        expect(policy.transactionDetails.txEffectiveDate).toStrictEqual(new Date('2022-03-29T10:00:00'))
    })
})
