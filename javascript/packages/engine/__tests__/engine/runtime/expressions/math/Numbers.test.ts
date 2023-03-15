/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
import { Numbers } from '../../../../../src/engine/runtime/expressions/math/Numbers'

describe('Numbers', () => {
    describe('isValueInNumberSet', () => {
        type NumberSet = {
            min?: number
            max?: number
            step?: number
        }
        function hasNumbers(numberSet: NumberSet, ...numbers: number[]) {
            numbers.forEach(n =>
                expect(Numbers.isValueInNumberSet(n, numberSet.min, numberSet.max, numberSet.step)).toBeTruthy(),
            )
        }
        function doesNotHaveNumbers(numberSet: NumberSet, ...numbers: number[]) {
            numbers.forEach(n =>
                expect(Numbers.isValueInNumberSet(n, numberSet.min, numberSet.max, numberSet.step)).toBeFalsy(),
            )
        }
        it('should be in number set - negative min, positive max, with step', () => {
            const numberSet = { min: -4, max: 4, step: 3 }
            hasNumbers(numberSet, -4, -1, 2)
            doesNotHaveNumbers(numberSet, -5, -3, -2, 0, 1, 3, 3.569999, 4, 5)
        })
        it('should be in number set - negative min, negative max, with step', () => {
            const numberSet = { min: -40, max: -4, step: 25 }
            hasNumbers(numberSet, -40, -15)
            doesNotHaveNumbers(numberSet, -10, 15, -4)
        })
        it('should be in number set - positive min, positive max, with step, floating point numbers', () => {
            const numberSet = { min: 1.515, max: 1.535, step: 0.01 }
            hasNumbers(numberSet, 1.515, 1.525, 1.535)
            doesNotHaveNumbers(numberSet, 1.5151, 1.53501)
        })
        it('should be in number set - positive min, positive max, with step', () => {
            const numberSet = { min: 0, max: 15, step: 7.5 }
            hasNumbers(numberSet, 0, 7.5, 15)
            doesNotHaveNumbers(numberSet, -7.5, 0.0001, 14.9998)
        })
        it('should be in number set - negative min, null max, with step', () => {
            const numberSet = { min: -40, max: undefined, step: 25 }
            hasNumbers(numberSet, -40, -15, 10, 35)
            doesNotHaveNumbers(numberSet, -35, 25, -25)
        })
        it('should be in number set - null min, positive max, with step', () => {
            const numberSet = { min: undefined, max: 15, step: 7.5 }
            hasNumbers(numberSet, -7.5, 0, 7.5, 15)
            doesNotHaveNumbers(numberSet, 14, 16, 22.5)
        })
        it('should be in number set - min, max, null step', () => {
            const numberSet = { min: -10, max: 10, step: undefined }
            hasNumbers(numberSet, -10, 2.113574, 10)
            doesNotHaveNumbers(numberSet, -10.0001, 10.0001)
        })
        it('should be in number set - min, null max, null step', () => {
            const numberSet = { min: -10, max: undefined, step: undefined }
            hasNumbers(numberSet, -10, 2.113574, 10, 10.0001)
            doesNotHaveNumbers(numberSet, -10.0001)
        })
        it('should be in number set - null min, max, null step', () => {
            const numberSet = { min: undefined, max: 10, step: undefined }
            hasNumbers(numberSet, -10.0001, -10, 2.113574, 10)
            doesNotHaveNumbers(numberSet, 10.0001)
        })
    })
    describe('normalization', () => {
        it('should not round if precision is decimal 64', () => {
            const n = 1234567890.123456
            const normalized = Numbers.normalized(n)
            expect(normalized.toString()).toBe('1234567890.123456')
        })
        it('should round if precision is larger than decimal 64', () => {
            // 1234567890.1234567 just barely fits in javascript number because it uses binary64 format,
            // but will not fit in java's decimal64
            const n = 1234567890.1234567
            expect(n.toString()).toBe('1234567890.1234567')

            const normalized = Numbers.normalized(n)
            expect(normalized.toString()).toBe('1234567890.123457')
        })
    })
})
