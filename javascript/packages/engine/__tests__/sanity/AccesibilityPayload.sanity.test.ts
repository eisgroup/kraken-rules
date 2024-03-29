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
import { FieldMetadataReducer } from '../../src/engine/results/field_metadata_reducer/FieldMetadataReducer'
import { MatchableResults } from 'kraken-jest-matchers'

describe('Engine Sanity Accessibility Payload Test', () => {
    const { empty } = sanityMocks
    it("should execute 'AccessibilityAutoPolicy' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'AccessibilityAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)
        const matchableResults = {
            entryPointResults: results,
            fieldResults,
        } as MatchableResults

        expect(results).k_toMatchResultsStats({ total: 4, disabled: 4 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect(matchableResults).k_toMatchResultsSnapshots()
    })
})
