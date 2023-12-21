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

import { nativeFunctions } from '../../../../../src/engine/runtime/expressions/functionLibrary/NativeFunctions'
import { Numbers } from '../../../../../src/engine/runtime/expressions/math/Numbers'
import { InternalFunctionScope } from '../../../../../src/engine/runtime/expressions/functionLibrary/Registry'

import {
    DefaultDateCalculator,
    resolveBrowserTimezoneId,
} from '../../../../../src/engine/runtime/expressions/math/Temporal'

describe('Native Functions Test', () => {
    const scope: InternalFunctionScope = {
        normalize: Numbers.normalized,
        functionContext: {
            zoneId: resolveBrowserTimezoneId(),
        },
        dateCalculator: new DefaultDateCalculator(),
    }

    describe('_n function', () => {
        it('should check if value is number', () => {
            expect(() => nativeFunctions._n(undefined)).toThrow()
            expect(() => nativeFunctions._n(null)).toThrow()
            expect(() => nativeFunctions._n('string')).toThrow()
            expect(() => nativeFunctions._n(true)).toThrow()

            expect(nativeFunctions._n(10)).toBe(10)
        })
        it('should check if value is string', () => {
            expect(() => nativeFunctions._s(undefined)).toThrow()
            expect(() => nativeFunctions._s(null)).toThrow()
            expect(() => nativeFunctions._s(10)).toThrow()
            expect(() => nativeFunctions._s(true)).toThrow()

            expect(nativeFunctions._s('string')).toBe('string')
        })
        it('should check if value is boolean', () => {
            expect(() => nativeFunctions._b(undefined)).toThrow()
            expect(() => nativeFunctions._b(null)).toThrow()
            expect(() => nativeFunctions._b('string')).toThrow()
            expect(() => nativeFunctions._b(10)).toThrow()

            expect(nativeFunctions._b(true)).toBe(true)
        })
        it('should check if value is eq', () => {
            expect(nativeFunctions._eq.bind(scope)(undefined, null)).toBe(true)
            expect(nativeFunctions._eq.bind(scope)(null, '')).toBe(false)
            expect(nativeFunctions._eq.bind(scope)(10, 10)).toBe(true)
            expect(nativeFunctions._eq.bind(scope)(0, false)).toBe(false)
            expect(nativeFunctions._eq.bind(scope)(0, null)).toBe(false)
            expect(nativeFunctions._eq.bind(scope)(new Date(2000, 1, 1), new Date(2000, 1, 1))).toBe(true)
        })
        it('should check if value is mt', () => {
            expect(() => nativeFunctions._mt.bind(scope)(undefined, 10)).toThrow()
            expect(nativeFunctions._mt.bind(scope)(10, 9)).toBe(true)
            expect(nativeFunctions._mt.bind(scope)(10, 10)).toBe(false)
            expect(nativeFunctions._mt.bind(scope)(10, 11)).toBe(false)
            expect(nativeFunctions._mt.bind(scope)(new Date('2020-01-02'), new Date('2020-01-01'))).toBe(true)
            expect(nativeFunctions._mt.bind(scope)(new Date('2020-01-01'), new Date('2020-01-02'))).toBe(false)
            expect(nativeFunctions._mt.bind(scope)(new Date('2020-01-02'), new Date('2020-01-02'))).toBe(false)
        })
        it('should check if value is mte', () => {
            expect(() => nativeFunctions._mte.bind(scope)(undefined, 10)).toThrow()
            expect(nativeFunctions._mte.bind(scope)(10, 9)).toBe(true)
            expect(nativeFunctions._mte.bind(scope)(10, 10)).toBe(true)
            expect(nativeFunctions._mte.bind(scope)(10, 11)).toBe(false)
            expect(nativeFunctions._mte.bind(scope)(new Date('2020-01-02'), new Date('2020-01-01'))).toBe(true)
            expect(nativeFunctions._mte.bind(scope)(new Date('2020-01-01'), new Date('2020-01-02'))).toBe(false)
            expect(nativeFunctions._mte.bind(scope)(new Date('2020-01-02'), new Date('2020-01-02'))).toBe(true)
        })
        it('should check if value is lt', () => {
            expect(() => nativeFunctions._lt.bind(scope)(undefined, 10)).toThrow()
            expect(nativeFunctions._lt.bind(scope)(10, 9)).toBe(false)
            expect(nativeFunctions._lt.bind(scope)(10, 10)).toBe(false)
            expect(nativeFunctions._lt.bind(scope)(10, 11)).toBe(true)
            expect(nativeFunctions._lt.bind(scope)(new Date('2020-01-02'), new Date('2020-01-01'))).toBe(false)
            expect(nativeFunctions._lt.bind(scope)(new Date('2020-01-01'), new Date('2020-01-02'))).toBe(true)
            expect(nativeFunctions._lt.bind(scope)(new Date('2020-01-02'), new Date('2020-01-02'))).toBe(false)
        })
        it('should check if value is lte', () => {
            expect(() => nativeFunctions._lte.bind(scope)(undefined, 10)).toThrow()
            expect(nativeFunctions._lte.bind(scope)(10, 9)).toBe(false)
            expect(nativeFunctions._lte.bind(scope)(10, 10)).toBe(true)
            expect(nativeFunctions._lte.bind(scope)(10, 11)).toBe(true)
            expect(nativeFunctions._lte.bind(scope)(new Date('2020-01-02'), new Date('2020-01-01'))).toBe(false)
            expect(nativeFunctions._lte.bind(scope)(new Date('2020-01-01'), new Date('2020-01-02'))).toBe(true)
            expect(nativeFunctions._lte.bind(scope)(new Date('2020-01-02'), new Date('2020-01-02'))).toBe(true)
        })
        it('should check if value is eq', () => {
            expect(nativeFunctions._neq.bind(scope)(undefined, null)).toBe(false)
            expect(nativeFunctions._neq.bind(scope)(null, '')).toBe(true)
            expect(nativeFunctions._neq.bind(scope)(10, 10)).toBe(false)
            expect(nativeFunctions._neq.bind(scope)(0, false)).toBe(true)
            expect(nativeFunctions._neq.bind(scope)(0, null)).toBe(true)
        })
        it('should check if value is in array', () => {
            expect(nativeFunctions._in.bind(scope)(undefined, undefined)).toBe(false)
            expect(nativeFunctions._in.bind(scope)(null, null)).toBe(false)
            expect(nativeFunctions._in.bind(scope)([], null)).toBe(false)
            expect(nativeFunctions._in.bind(scope)([undefined], null)).toBe(true)
            expect(nativeFunctions._in.bind(scope)([null], undefined)).toBe(true)
            expect(nativeFunctions._in.bind(scope)([10], 10)).toBe(true)
            expect(nativeFunctions._in.bind(scope)(['string'], 'string')).toBe(true)
            expect(nativeFunctions._in.bind(scope)([true], true)).toBe(true)

            expect(nativeFunctions._in.bind(scope)([false], null)).toBe(false)
            expect(nativeFunctions._in.bind(scope)([false], 0)).toBe(false)
            expect(nativeFunctions._in.bind(scope)([false], undefined)).toBe(false)
            expect(() => nativeFunctions._in.bind(scope)({ a: 'a' }, undefined)).toThrow()
        })
        it('should do modulus', () => {
            expect(nativeFunctions._mod(10.1, 3)).toBe(1.1)
            expect(nativeFunctions._mod(840.7, 0.1)).toBe(0)
            expect(nativeFunctions._mod(-1.0, 0.3)).toBe(-0.1)
            expect(() => nativeFunctions._mod(1, undefined)).toThrow()
        })
        it('should do subtraction', () => {
            expect(nativeFunctions._sub(840.7, 0.11)).toBe(840.59)
            expect(() => nativeFunctions._sub(1, undefined)).toThrow()
        })
        it('should do addition', () => {
            expect(nativeFunctions._add(840.7, 0.11)).toBe(840.81)
            expect(() => nativeFunctions._add(1, undefined)).toThrow()
        })
        it('should do multiplication', () => {
            expect(nativeFunctions._mult(840.7, 0.1)).toBe(84.07)
            expect(() => nativeFunctions._mult(1, undefined)).toThrow()
        })
        it('should do division', () => {
            expect(nativeFunctions._div(840.7, 0.1)).toBe(8407)
            expect(nativeFunctions._div(1, 3).toString()).toBe('0.3333333333333333')
            expect(nativeFunctions._div(2, 3).toString()).toBe('0.6666666666666667')
            expect(nativeFunctions._div(100, 3).toString()).toBe('33.33333333333333')
            expect(nativeFunctions._div(200, 3).toString()).toBe('66.66666666666667')
            expect(() => nativeFunctions._div(1, undefined)).toThrow()
            expect(() => nativeFunctions._div(1, 0)).toThrow()
        })
        it('should do power', () => {
            expect(nativeFunctions._pow(2.22, 2)).toBe(4.9284)
            expect(nativeFunctions._pow(4, -2)).toBe(0.0625)
            expect(nativeFunctions._pow(4, -2.1)).toBe(0.0625)
            expect(nativeFunctions._pow(2.0, 3)).toBe(8)
            expect(() => nativeFunctions._div(1, undefined)).toThrow()
        })
        it('should resolve first object with property', () => {
            const coverage = {
                limit: 10,
            }
            const policy = {
                policyCd: 'cd',
            }
            expect(nativeFunctions._o('limit', [policy, coverage])).toBe(coverage)
            expect(nativeFunctions._o('limit', [policy])).toBe(undefined)
            expect(nativeFunctions._o('limit', [])).toBe(undefined)
            expect(nativeFunctions._o('limit', [undefined, coverage])).toBe(coverage)
        })
        it('should evaluate property', () => {
            const policy = {
                policyCd: 'cd',
                arrayOfPolicyCd: ['cd'],
            }
            const arrayOfPolicy = [policy]
            expect(nativeFunctions._flatMap(policy, o => (o as typeof policy).policyCd)).toStrictEqual('cd')
            expect(nativeFunctions._flatMap(arrayOfPolicy, o => (o as typeof policy).policyCd)).toStrictEqual(['cd'])
            expect(nativeFunctions._flatMap(policy, o => (o as typeof policy).arrayOfPolicyCd[0])).toStrictEqual('cd')
            expect(nativeFunctions._flatMap(arrayOfPolicy, o => (o as typeof policy).arrayOfPolicyCd[0])).toStrictEqual(
                ['cd'],
            )
        })
    })
})
