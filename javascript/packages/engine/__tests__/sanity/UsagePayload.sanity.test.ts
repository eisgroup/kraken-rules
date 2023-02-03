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
import { FieldMetadataReducer } from '../../src'

describe('Engine Sanity Usage Payload Test', () => {
    const { valid, empty } = sanityMocks
    it("should execute 'UsageAutoPolicy' entrypoint with empty data", () => {
        const results = sanityEngine.evaluate(empty(), 'UsagePayloadAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 21, critical: 21 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'UsageAutoPolicy' entrypoint with valid data", () => {
        const results = sanityEngine.evaluate(valid(), 'UsagePayloadAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 26, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it('should execute rule on unknown field type', () => {
        const policy = (uri?: string) => {
            const data = empty()
            data.refToCustomer = uri
            return data
        }
        const results = sanityEngine.evaluate(policy(), 'Usage-UnknownField')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()

        const resultsValid = sanityEngine.evaluate(policy('uri'), 'Usage-UnknownField')
        const fieldResultsValid = new FieldMetadataReducer().reduce(results)

        expect(resultsValid).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(resultsValid).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults: fieldResultsValid }).k_toMatchResultsSnapshots()
    })
    it('should execute must be empty payload', () => {
        const data = empty()
        data.policyNumber = 'P1'

        const results = sanityEngine.evaluate(data, 'policy number must be empty')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
})
