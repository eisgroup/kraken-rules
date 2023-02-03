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

describe('Engine Sanity Assertion Payload Test', () => {
    const { empty, valid, inValid } = sanityMocks
    it("should execute 'RegExpAutoPolicy' entrypoint with empty data", () => {
        const results = sanityEngine.evaluate(empty(), 'RegExpAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 5, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'RegExpAutoPolicy' entrypoint with valid data", () => {
        const results = sanityEngine.evaluate(valid(), 'RegExpAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 11, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'RegExpAutoPolicy' entrypoint with not valid data", () => {
        const results = sanityEngine.evaluate(inValid(), 'RegExpAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toHaveExpressionsFailures(0)
        expect(results).k_toMatchResultsStats({ total: 7, critical: 6, warning: 0 })
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'RegExpAutoPolicy' entrypoint and reduce results", () => {
        const results = sanityEngine.evaluate(valid(), 'RegExpAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 11, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
})
