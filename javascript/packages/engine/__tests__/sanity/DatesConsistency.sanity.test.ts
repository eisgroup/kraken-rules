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
import _ from 'kraken-jest-matchers'

describe('Engine Sanity date consistency test', () => {
    const { empty } = sanityMocks
    it("should execute 'Date creation time must be consistent' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date creation time must be consistent')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date time creation time must be consistent' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date time creation time must be consistent')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'DateTime conversion is consistent' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'DateTime conversion is consistent')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date conversion is consistent' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date conversion is consistent')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date conversion is consistent WithDay' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date conversion is consistent WithDay')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date conversion is consistent WithMonth' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date conversion is consistent WithMonth')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date conversion is consistent WithYear' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date conversion is consistent WithYear')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date conversion is consistent PlusYears_Months_Days' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date conversion is consistent PlusYears_Months_Days')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Now conversion' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Now conversion')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Date Getters' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Date Getters')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it("should execute 'Create date with string format' entrypoint", () => {
        const results = sanityEngine.evaluate(empty(), 'Create date with string format')

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
})
