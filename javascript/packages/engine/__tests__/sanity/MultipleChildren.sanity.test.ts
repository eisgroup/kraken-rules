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

import { sanityMocks } from './_AutoPolicyObject.mocks'
import { sanityEngine } from './_SanityEngine'
import { mock } from '../mock'
import { FieldMetadataReducer } from '../../src'

// navigation expression to children SecondaryInsured:
// [this.oneInsured,this.multipleInsureds,[this.multiInsureds1,this.multiInsureds2]]
describe('Engine Sanity multiple nested array test', () => {
    const { valid } = sanityMocks
    const insured = (id: string) => ({
        cd: mock.modelTreeJson.contexts.SecondaryInsured.name,
        id,
        name: 'mock',
    })
    it('should apply default on nested structure', () => {
        const policy = valid()
        policy.oneInsured = insured('1')
        policy.multiInsureds1 = [insured('2'), insured('3')]
        policy.multiInsureds2 = [insured('4'), insured('5')]
        policy.multipleInsureds = [insured('6'), insured('7')]

        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        const fieldResults = new FieldMetadataReducer().reduce(results)

        expect(policy.oneInsured!.name).toBe('new')
        expect(policy.multiInsureds1![0].name).toBe('new')
        expect(policy.multiInsureds1![1].name).toBe('new')
        expect(policy.multiInsureds2![0].name).toBe('new')
        expect(policy.multiInsureds2![1].name).toBe('new')
        expect(policy.multipleInsureds![0].name).toBe('new')
        expect(policy.multipleInsureds![1].name).toBe('new')
        expect(results).k_toMatchResultsStats({ total: 7 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect({ entryPointResults: results, fieldResults }).k_toMatchResultsSnapshots()
    })
    it('should not fail with undefined in nested arrays (undefined oneInsured)', () => {
        const policy = valid()
        policy.oneInsured = undefined
        policy.multiInsureds1 = [insured('2'), insured('3')]
        policy.multiInsureds2 = [insured('4'), insured('5')]
        policy.multipleInsureds = [insured('6'), insured('7')]
        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        expect(results).k_toMatchResultsStats({ total: 6 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should not fail with undefined in nested arrays (undefined in array)', () => {
        const policy = valid()
        policy.oneInsured = insured('1')
        // @ts-expect-error testing negative case
        policy.multiInsureds1 = [undefined, insured('3')]
        policy.multiInsureds2 = [insured('4'), insured('5')]
        policy.multipleInsureds = [insured('6'), insured('7')]
        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        expect(results).k_toMatchResultsStats({ total: 6 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should not fail with undefined in nested arrays (undefined nested array)', () => {
        const policy = valid()
        policy.oneInsured = insured('1')
        policy.multiInsureds1 = undefined
        policy.multiInsureds2 = [insured('4'), insured('5')]
        policy.multipleInsureds = [insured('6'), insured('7')]
        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        expect(results).k_toMatchResultsStats({ total: 5 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should not fail with undefined in nested arrays (undefined in second level array)', () => {
        const policy = valid()
        policy.oneInsured = insured('1')
        policy.multiInsureds1 = [insured('2'), insured('3')]
        policy.multiInsureds2 = [insured('4'), insured('5')]
        // @ts-expect-error testing negative case
        policy.multipleInsureds = [undefined, insured('7')]
        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        expect(results).k_toMatchResultsStats({ total: 6 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should not fail with undefined in nested arrays (undefined second level array)', () => {
        const policy = valid()
        policy.oneInsured = insured('1')
        policy.multiInsureds1 = [insured('2'), insured('3')]
        policy.multiInsureds2 = [insured('4'), insured('5')]
        policy.multipleInsureds = undefined
        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        expect(results).k_toMatchResultsStats({ total: 5 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should not fail with undefined in nested arrays (undefined all)', () => {
        const policy = valid()
        const results = sanityEngine.evaluate(policy, 'MultipleInsureds-default')
        expect(results).k_toMatchResultsStats({ total: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
    })
})
