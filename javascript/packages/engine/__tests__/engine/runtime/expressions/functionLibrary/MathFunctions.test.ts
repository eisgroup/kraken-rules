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

import { mathFunctions } from '../../../../../src/engine/runtime/expressions/functionLibrary/MathFunctions'
const { NumberSequence } = mathFunctions
import { mock } from '../../../../mock'

describe('Array Functions Test', () => {
    describe('Sum function', () => {
        it('should find sum of values from collection', () => {
            const result = mathFunctions.Sum([mock.toMoney(15.21), 6, 8, 9.000001, 0.685])
            expect(result).toBe(38.895001)
        })
        it('should return undefined when collection is empty', () => {
            const result = mathFunctions.Sum([])
            expect(result).toBe(undefined)
        })
        it('should return undefined when collection is undefined', () => {
            const result = mathFunctions.Sum(undefined)
            expect(result).toBe(undefined)
        })
        it('should throw on non array', () => {
            expect(() => mathFunctions.Sum(1)).toThrow()
        })
        it('should throw if has undefined', () => {
            expect(() => mathFunctions.Sum([undefined])).toThrow()
        })
        it('should throw if has wrong type', () => {
            expect(() => mathFunctions.Sum(['1'])).toThrow()
        })
    })
    describe('Avg function', () => {
        it('should find avg of values from collection', () => {
            const result = mathFunctions.Avg([mock.toMoney(12.99), 6, 18.5, 0.12, 0.6])
            expect(result).toBe(7.642)
        })
        it('should return undefined when collection is empty', () => {
            const result = mathFunctions.Min([])
            expect(result).toBe(undefined)
        })
        it('should return undefined when avg collection is undefined', () => {
            const result = mathFunctions.Avg(undefined)
            expect(result).toBe(undefined)
        })
        it('should throw on non array', () => {
            expect(() => mathFunctions.Avg(1)).toThrow()
        })
        it('should throw if has undefined', () => {
            expect(() => mathFunctions.Avg([undefined])).toThrow()
        })
        it('should throw if has wrong type', () => {
            expect(() => mathFunctions.Avg(['1'])).toThrow()
        })
    })
    describe('Round function', () => {
        it('should round to integer', () => {
            expect(mathFunctions.Round(1)).toBe(1)
            expect(mathFunctions.Round(1.5)).toBe(2)
            expect(mathFunctions.Round(2.5)).toBe(3)
            expect(mathFunctions.Round(-1)).toBe(-1)
            expect(mathFunctions.Round(-1.5)).toBe(-2)
            expect(mathFunctions.Round(-2.5)).toBe(-3)
        })
        it('should round to even integer', () => {
            expect(mathFunctions.RoundEven(1)).toBe(1)
            expect(mathFunctions.RoundEven(1.5)).toBe(2)
            expect(mathFunctions.RoundEven(2.5)).toBe(2)
            expect(mathFunctions.RoundEven(-1)).toBe(-1)
            expect(mathFunctions.RoundEven(-1.5)).toBe(-2)
            expect(mathFunctions.RoundEven(-2.5)).toBe(-2)
        })
        it('should round to scale', () => {
            expect(mathFunctions.Round(1, 2)).toBe(1)
            expect(mathFunctions.Round(1.555, 2)).toBe(1.56)
            expect(mathFunctions.Round(100.565, 2)).toBe(100.57)
            expect(mathFunctions.Round(-1, 2)).toBe(-1)
            expect(mathFunctions.Round(-1.555, 2)).toBe(-1.56)
            expect(mathFunctions.Round(-100.565, 2)).toBe(-100.57)
        })
        it('should round even to scale', () => {
            expect(mathFunctions.RoundEven(1, 2)).toBe(1)
            expect(mathFunctions.RoundEven(1.555, 2)).toBe(1.56)
            expect(mathFunctions.RoundEven(100.565, 2)).toBe(100.56)
            expect(mathFunctions.RoundEven(-1, 2)).toBe(-1)
            expect(mathFunctions.RoundEven(-1.555, 2)).toBe(-1.56)
            expect(mathFunctions.RoundEven(-100.565, 2)).toBe(-100.56)
        })
    })
    describe('Floor function', () => {
        it('should floor', () => {
            expect(mathFunctions.Floor(1)).toBe(1)
            expect(mathFunctions.Floor(1.9)).toBe(1)
            expect(mathFunctions.Floor(-1)).toBe(-1)
            expect(mathFunctions.Floor(-1.1)).toBe(-2)
        })
    })
    describe('Ceil function', () => {
        it('should ceil', () => {
            expect(mathFunctions.Ceil(1)).toBe(1)
            expect(mathFunctions.Ceil(1.1)).toBe(2)
            expect(mathFunctions.Ceil(-1)).toBe(-1)
            expect(mathFunctions.Ceil(-1.9)).toBe(-1)
        })
    })
    describe('Abs function', () => {
        it('should abs', () => {
            expect(mathFunctions.Abs(1)).toBe(1)
            expect(mathFunctions.Abs(-1.1)).toBe(1.1)
        })
    })
    describe('Sign function', () => {
        it('should sign', () => {
            expect(mathFunctions.Sign(1)).toBe(1)
            expect(mathFunctions.Sign(0)).toBe(0)
            expect(mathFunctions.Sign(-1.1)).toBe(-1)
        })
    })
    describe('Max function', () => {
        it('should return max of two numbers', () => {
            expect(mathFunctions.Max(0, 1)).toBe(1)
            expect(mathFunctions.Max(-1.1, -10)).toBe(-1.1)
            expect(() => mathFunctions.Max(0, undefined)).toThrow()
        })
        it('should return min of two dates', () => {
            expect(mathFunctions.Max(new Date('2020-01-01'), new Date('2020-01-02')) as Date).k_toBeDateEqualTo(
                new Date('2020-01-02'),
            )

            expect(() => mathFunctions.Max(0, new Date(2020, 1, 1))).toThrow()
            expect(() => mathFunctions.Max(0, undefined)).toThrow()
        })
        it('should return min of date collection', () => {
            expect(mathFunctions.Max([new Date('2020-01-01'), new Date('2020-01-02')]) as Date).k_toBeDateEqualTo(
                new Date('2020-01-02'),
            )
        })
        it('should find max of values from collection', () => {
            const result = mathFunctions.Max([mock.toMoney(19.99), 6, 13.5, 0.12, 0.6])
            expect(result).toBe(19.99)
        })
        it('should return undefined when collection is empty', () => {
            const result = mathFunctions.Max([])
            expect(result).toBe(undefined)
        })
        it('should return when collection is undefined', () => {
            const result = mathFunctions.Max(undefined)
            expect(result).toBe(undefined)
        })
        it('should throw on non array', () => {
            expect(() => mathFunctions.Max(1)).toThrow()
        })
        it('should throw if has undefined', () => {
            expect(() => mathFunctions.Max([undefined])).toThrow()
        })
        it('should throw if has wrong type', () => {
            expect(() => mathFunctions.Max(['1'])).toThrow()
        })
    })
    describe('Min function', () => {
        it('should return min of two numbers', () => {
            expect(mathFunctions.Min(0, 1)).toBe(0)
            expect(mathFunctions.Min(-1.1, -10)).toBe(-10)
            expect(() => mathFunctions.Min(0, undefined)).toThrow()
        })
        it('should return min of two dates', () => {
            expect(mathFunctions.Min(new Date('2020-01-01'), new Date('2020-01-02')) as Date).k_toBeDateEqualTo(
                new Date('2020-01-01'),
            )

            expect(() => mathFunctions.Min(0, new Date(2020, 1, 1))).toThrow()
            expect(() => mathFunctions.Min(0, undefined)).toThrow()
        })
        it('should return min of date collection', () => {
            expect(mathFunctions.Min([new Date('2020-01-01'), new Date('2020-01-02')]) as Date).k_toBeDateEqualTo(
                new Date('2020-01-01'),
            )
        })
        it('should find min of values from collection', () => {
            expect(mathFunctions.Min([mock.toMoney(15.99), 6, 18.5, 0.12, 0.6])).toBe(0.12)
            expect(mathFunctions.Min([-5, 6, 18.5, 0.12, 0.6])).toBe(-5)
        })
        it('should return undefined when collection is empty', () => {
            const result = mathFunctions.Min([])
            expect(result).toBe(undefined)
        })
        it('should return undefined when collection is undefined', () => {
            const result = mathFunctions.Min(undefined)
            expect(result).toBe(undefined)
        })
        it('should throw on non array', () => {
            expect(() => mathFunctions.Min(1)).toThrow()
        })
        it('should throw if has undefined', () => {
            expect(() => mathFunctions.Max([undefined])).toThrow()
        })
        it('should throw if has wrong type', () => {
            expect(() => mathFunctions.Max(['1'])).toThrow()
        })
    })
    describe('Sqrt function', () => {
        it('should sqrt', () => {
            expect(mathFunctions.Sqrt(1)).toBe(1)
            expect(mathFunctions.Sqrt(9)).toBe(3)
            expect(mathFunctions.Sqrt(15.29344444444444)).toBe(3.910683373074895)
            expect(() => mathFunctions.Sqrt(undefined)).toThrow()
        })
    })
    describe('Generator Functions', () => {
        describe('default steps', () => {
            describe('default steps', () => {
                it('should generate sequence', () => {
                    expect(NumberSequence(0, 3)).toMatchObject([0, 1, 2, 3])
                    expect(NumberSequence(3, 0)).toMatchObject([3, 2, 1, 0])
                    expect(NumberSequence(0, 0)).toMatchObject([0])
                    expect(NumberSequence(30, 32, 1)).toMatchObject([30, 31, 32])
                    expect(NumberSequence(0.3, 0.39, 0.01)).toMatchObject([
                        0.3, 0.31, 0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39,
                    ])
                    expect(NumberSequence(0.33, 0.339, 0.001)).toMatchObject([
                        0.33, 0.331, 0.332, 0.333, 0.334, 0.335, 0.336, 0.337, 0.338, 0.339,
                    ])
                    expect(NumberSequence(0.333, 0.3339, 0.0001)).toMatchObject([
                        0.333, 0.3331, 0.3332, 0.3333, 0.3334, 0.3335, 0.3336, 0.3337, 0.3338, 0.3339,
                    ])
                    expect(NumberSequence(10002.00001, 10002.000051, 0.000001)).toMatchSnapshot(
                        '10002.00001, 10002.000051, 0.000001',
                    )
                    expect(NumberSequence(1.00001, 1.00002, 1.000001)).toMatchSnapshot('1.00001, 1.00002, 1.000001')
                    expect(NumberSequence(1000.0001, 1000.0002, 0.00001)).toMatchSnapshot(
                        '1000.0001, 1000.0002, 0.00001 - 5 digits',
                    )
                    expect(NumberSequence(1000000.00001, 1000000.00002, 0.000001)).toMatchSnapshot(
                        '1000000.00001, 1000000.00002, 0.000001 - 6 digits',
                    )
                })
                it('should throw on null parameters', () => {
                    const undefinedParameter = undefined as unknown as number
                    expect(() => NumberSequence(undefinedParameter, 3)).toThrowError(NumberSequence.name)
                    expect(() => NumberSequence(1, undefinedParameter)).toThrowError(NumberSequence.name)
                })
            })
        })
        describe('explicit steps', () => {
            it('should generate sequence', () => {
                expect(NumberSequence(0, 3, 2)).toMatchObject([0, 2])
                expect(NumberSequence(4, 0, -2)).toMatchObject([4, 2, 0])
                expect(NumberSequence(10, 11, 12)).toMatchObject([10])
            })
            it('should throw on null parameters', () => {
                const undefinedParameter = undefined as unknown as number
                expect(() => NumberSequence(undefinedParameter, 3)).toThrowError(NumberSequence.name)
                expect(() => NumberSequence(1, undefinedParameter)).toThrowError(NumberSequence.name)
            })
            it('should throw on infinity generation', () => {
                expect(() => NumberSequence(10, 5, 1)).toThrowError(NumberSequence.name)
                expect(() => NumberSequence(5, 10, -1)).toThrowError(NumberSequence.name)
                expect(() => NumberSequence(1, 2, 0)).toThrowError(NumberSequence.name)
            })
        })
    })
})
