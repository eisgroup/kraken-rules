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

import { stringFunctions } from '../../../../../src/engine/runtime/expressions/functionLibrary/StringFunctions'

describe('String Functions Test', () => {
    describe('Substring function', () => {
        it('should substring by parameters', () => {
            expect(stringFunctions.Substring('fullString', 4, undefined)).toBe('String')
            expect(stringFunctions.Substring('fullString', 4, null)).toBe('String')
            expect(stringFunctions.Substring('fullStringLeft', 4, 10)).toBe('String')
            expect(stringFunctions.Substring('fullStringLeft', 0, 0)).toBe('')
            expect(() => stringFunctions.Substring('String', 8, undefined)).toThrowError()
            expect(() => stringFunctions.Substring(undefined, 8, undefined)).toThrowError()
            expect(() => stringFunctions.Substring('string', 8, undefined)).toThrowError()
            expect(() => stringFunctions.Substring('string', undefined, 8)).toThrowError()
            expect(() => stringFunctions.Substring(null, 8, undefined)).toThrowError()
            expect(() => stringFunctions.Substring('String', -1, undefined)).toThrowError()
            expect(() => stringFunctions.Substring('String', 3, 8)).toThrowError()
            expect(() => stringFunctions.Substring('String', 3, 2)).toThrowError()
        })
        it('should concat strings', () => {
            expect(stringFunctions.Concat(['a', 'b', 'c'])).toBe('abc')
            expect(stringFunctions.Concat()).toBe('')
            expect(stringFunctions.Concat(['a', undefined, 'c'])).toBe('ac')
            expect(stringFunctions.Concat([1, '2', '3'])).toBe('123')
        })
        it('should return string length', () => {
            expect(stringFunctions.StringLength('333')).toBe(3)
            expect(stringFunctions.StringLength(undefined)).toBe(0)
        })
        it('should search in string array or string', () => {
            expect(stringFunctions.Includes(['a', 'b', 'c'], 'a')).toBe(true)
            expect(stringFunctions.Includes(['a', 'b', 'c'], 'd')).toBe(false)
            expect(stringFunctions.Includes(['a', 'b', 'c'], undefined)).toBe(false)
            expect(stringFunctions.Includes([1, 2, 3], 3)).toBe(false)
            expect(stringFunctions.Includes([], 'a')).toBe(false)
            expect(stringFunctions.Includes(undefined, 'a')).toBe(false)
            expect(stringFunctions.Includes('abc', 'a')).toBe(true)
            expect(stringFunctions.Includes('abc', 'd')).toBe(false)
            expect(() => stringFunctions.Includes(1, 'd')).toThrow()
        })
        it('should convert number to string', () => {
            expect(stringFunctions.NumberToString(1)).toBe('1')
            expect(stringFunctions.NumberToString(1.0)).toBe('1')
            expect(stringFunctions.NumberToString(0.0000000003333333333333333)).toBe('0.0000000003333333333333333')
            expect(stringFunctions.NumberToString(undefined)).toBe('')
            expect(() => stringFunctions.NumberToString([])).toThrow()
        })
        it('should add string from left', () => {
            expect(stringFunctions.PadLeft('11', '0', 4)).toBe('0011')
            expect(stringFunctions.PadLeft('11', '0', 2)).toBe('11')
            expect(stringFunctions.PadLeft('11', '0', 1)).toBe('11')
            expect(stringFunctions.PadLeft('11', '0')).toBe('11')
            expect(stringFunctions.PadLeft('11', undefined, 4)).toBe('  11')
            expect(stringFunctions.PadLeft(undefined, '0', 4)).toBe('0000')
            expect(stringFunctions.PadLeft(undefined, '0')).toBe('')
            expect(() => stringFunctions.PadLeft('a', 'bb', 1)).toThrow()
        })
        it('should add string from right', () => {
            expect(stringFunctions.PadRight('11', '0', 4)).toBe('1100')
            expect(stringFunctions.PadRight('11', '0', 2)).toBe('11')
            expect(stringFunctions.PadRight('11', '0', 1)).toBe('11')
            expect(stringFunctions.PadRight('11', '0')).toBe('11')
            expect(stringFunctions.PadRight('11', undefined, 4)).toBe('11  ')
            expect(stringFunctions.PadRight(undefined, '0', 4)).toBe('0000')
            expect(stringFunctions.PadRight(undefined, '0')).toBe('')
            expect(() => stringFunctions.PadRight('a', 'bb', 1)).toThrow()
        })
        it('should trim string', () => {
            expect(stringFunctions.Trim('  a  ')).toBe('a')
            expect(stringFunctions.Trim('  start end  ')).toBe('start end')
            expect(stringFunctions.Trim('')).toBe('')
            expect(stringFunctions.Trim(undefined)).toBe(undefined)
        })
        it('should make string uppercase', () => {
            expect(stringFunctions.UpperCase('aaa')).toBe('AAA')
            expect(stringFunctions.UpperCase('')).toBe('')
            expect(stringFunctions.UpperCase(undefined)).toBe(undefined)
        })
        it('should make string lowercase', () => {
            expect(stringFunctions.LowerCase('AAA')).toBe('aaa')
            expect(stringFunctions.LowerCase('')).toBe('')
            expect(stringFunctions.LowerCase(undefined)).toBe(undefined)
        })
        it('should check is string starts with some text', () => {
            expect(stringFunctions.StartsWith('ABC', 'AB')).toBeTruthy()
            // @ts-expect-error testing negative case
            expect(stringFunctions.StartsWith('123', 12)).toBeTruthy()
            expect(stringFunctions.StartsWith('ABC', 'BC')).toBeFalsy()
            expect(stringFunctions.StartsWith('B', 'BC')).toBeFalsy()
            expect(stringFunctions.StartsWith('', 'BC')).toBeFalsy()
            expect(() => stringFunctions.StartsWith('ABC')).toThrowError()
            // @ts-expect-error testing negative case
            expect(() => stringFunctions.StartsWith(11, 'A')).toThrowError()
            expect(() => stringFunctions.StartsWith()).toThrowError()
        })
        it('should check is string ends with some text', () => {
            expect(stringFunctions.EndsWith('ABC', 'BC')).toBeTruthy()
            // @ts-expect-error testing negative case
            expect(stringFunctions.EndsWith('123', 23)).toBeTruthy()
            expect(stringFunctions.EndsWith('ABC', 'AB')).toBeFalsy()
            expect(stringFunctions.EndsWith('C', 'CD')).toBeFalsy()
            expect(stringFunctions.EndsWith('', 'BC')).toBeFalsy()
            expect(() => stringFunctions.EndsWith('ABC')).toThrowError()
            expect(() => stringFunctions.EndsWith('ABC')).toThrowError()
            // @ts-expect-error testing negative case
            expect(() => stringFunctions.EndsWith(11, 'A')).toThrowError()
            expect(() => stringFunctions.EndsWith()).toThrowError()
        })
    })
    describe('IsBlank function', () => {
        it('should check if string is blank', () => {
            expect(stringFunctions.IsBlank(null)).toBeTruthy()
            expect(stringFunctions.IsBlank(undefined)).toBeTruthy()
            expect(stringFunctions.IsBlank('')).toBeTruthy()
            expect(stringFunctions.IsBlank('   ')).toBeTruthy()
            expect(stringFunctions.IsBlank('a')).toBeFalsy()
            expect(stringFunctions.IsBlank(' a ')).toBeFalsy()
            // @ts-expect-error testing negative case
            expect(() => stringFunctions.IsBlank(11)).toThrowError()
        })
    })
})
