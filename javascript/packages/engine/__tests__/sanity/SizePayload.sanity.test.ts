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
import { sanityMocks } from './_AutoPolicyObject.mocks'
import { FieldMetadataReducer } from '../../src'

describe('Size payload sanity test', () => {
    it('should success with array of 2', () => {
        const policy = sanityMocks.empty()
        policy.policies = ['1', '2']
        const result = sanityEngine.evaluate(policy, 'SizePayload')
        const fieldResults = new FieldMetadataReducer().reduce(result)

        expect(result).k_toMatchResultsStats({ total: 3, critical: 0 })
        expect(result).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: result, fieldResults }).k_toMatchResultsSnapshots()
    })
    it('should fail with array of 3', () => {
        const policy = sanityMocks.empty()
        policy.policies = ['1', '2', '3']
        const result = sanityEngine.evaluate(policy, 'SizePayload')
        const fieldResults = new FieldMetadataReducer().reduce(result)

        expect(result).k_toMatchResultsStats({ total: 3, critical: 2 })
        expect(result).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: result, fieldResults }).k_toMatchResultsSnapshots()
    })
    it('should handle undefined collection', () => {
        const policy = sanityMocks.empty()
        policy.policies = undefined
        const result = sanityEngine.evaluate(policy, 'SizePayload')
        const fieldResults = new FieldMetadataReducer().reduce(result)

        expect(result).k_toMatchResultsStats({ total: 3, critical: 2 })
        expect(result).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: result, fieldResults }).k_toMatchResultsSnapshots()
    })
})
