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

import { FieldMetadataReducer } from '../../src/engine/results/field_metadata_reducer/FieldMetadataReducer'
import { sanityMocks } from './_AutoPolicyObject.mocks'
import { sanityEngine } from './_SanityEngine'

describe('Engine Sanity Length Payload Test', () => {
    const { empty, inValid, valid } = sanityMocks
    it("should execute 'LengthAutoPolicy' entrypoint with valid data", () => {
        const results = sanityEngine.evaluate(valid(), 'LengthAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'LengthAutoPolicy' entrypoint with empty data", () => {
        const emptyData = empty()
        emptyData.parties!.push({
            id: '1',
            cd: 'Party',
        })
        const results = sanityEngine.evaluate(emptyData, 'LengthAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'LengthAutoPolicy' entrypoint with not valid data", () => {
        const notValidData = inValid()
        notValidData!.parties![0]['relationToPrimaryInsured'] = 'Related to Primary Insured'
        notValidData!.parties!.push({
            id: '1',
            cd: 'Party',
            relationToPrimaryInsured: 'Not Related to Primary Insured',
        })
        const results = sanityEngine.evaluate(notValidData, 'LengthAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 2 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'LengthAutoPolicy' entrypoint with equal length data", () => {
        const validData = valid()
        const results = sanityEngine.evaluate(validData, 'LengthAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it("should execute 'LengthAutoPolicy' entrypoint and reduce results", () => {
        const results = sanityEngine.evaluate(valid(), 'LengthAutoPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
})
