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

import { setFunctions } from '../../../../../src/engine/runtime/expressions/functionLibrary/SetFunctions'

describe('Set Functions Test', () => {
    it('should union two arrays', () => {
        expect(setFunctions.Union(null, null)).toStrictEqual([])
        expect(setFunctions.Union(undefined, null)).toStrictEqual([])
        expect(setFunctions.Union(null, undefined)).toStrictEqual([])
        expect(setFunctions.Union(undefined, undefined)).toStrictEqual([])
        expect(setFunctions.Union([], null)).toStrictEqual([])
        expect(setFunctions.Union([], [])).toStrictEqual([])
        expect(setFunctions.Union(['a'], undefined)).toStrictEqual(['a'])
        expect(setFunctions.Union(['a'], ['a', 'a'])).toStrictEqual(['a'])
        expect(setFunctions.Union(['a', 'a'], ['a'])).toStrictEqual(['a'])
        expect(setFunctions.Union(['a'], ['b'])).toStrictEqual(['a', 'b'])
        expect(setFunctions.Union(['a', 'b', 'c'], ['b', 'c', 'd'])).toStrictEqual(['a', 'b', 'c', 'd'])
        expect(setFunctions.Union(['a', null], ['b', null])).toStrictEqual(['a', undefined, 'b'])
        expect(setFunctions.Union(['a', null], ['b'])).toStrictEqual(['a', undefined, 'b'])
        expect(setFunctions.Union([null, undefined], [null, undefined])).toStrictEqual([undefined])
    })
    it('should intersect two arrays', () => {
        expect(setFunctions.Intersection(null, null)).toStrictEqual([])
        expect(setFunctions.Intersection(undefined, null)).toStrictEqual([])
        expect(setFunctions.Intersection(null, undefined)).toStrictEqual([])
        expect(setFunctions.Intersection(undefined, undefined)).toStrictEqual([])
        expect(setFunctions.Intersection([], null)).toStrictEqual([])
        expect(setFunctions.Intersection([], [])).toStrictEqual([])
        expect(setFunctions.Intersection(['a'], undefined)).toStrictEqual([])
        expect(setFunctions.Intersection(['a'], ['a', 'a'])).toStrictEqual(['a'])
        expect(setFunctions.Intersection(['a', 'a'], ['a'])).toStrictEqual(['a'])
        expect(setFunctions.Intersection(['a'], ['b'])).toStrictEqual([])
        expect(setFunctions.Intersection(['a', 'b', 'c'], ['b', 'c', 'd'])).toStrictEqual(['b', 'c'])
        expect(setFunctions.Intersection(['a', null], ['b', null])).toStrictEqual([undefined])
        expect(setFunctions.Intersection(['a', null], ['b'])).toStrictEqual([])
        expect(setFunctions.Intersection([null], [undefined])).toStrictEqual([undefined])
    })
    it('should calculate difference between two arrays', () => {
        expect(setFunctions.Difference(null, null)).toStrictEqual([])
        expect(setFunctions.Difference(undefined, null)).toStrictEqual([])
        expect(setFunctions.Difference(null, undefined)).toStrictEqual([])
        expect(setFunctions.Difference(undefined, undefined)).toStrictEqual([])
        expect(setFunctions.Difference([], null)).toStrictEqual([])
        expect(setFunctions.Difference([], [])).toStrictEqual([])
        expect(setFunctions.Difference(['a'], undefined)).toStrictEqual(['a'])
        expect(setFunctions.Difference(['a'], ['a', 'a'])).toStrictEqual([])
        expect(setFunctions.Difference(['a', 'a'], ['a'])).toStrictEqual([])
        expect(setFunctions.Difference(['a'], ['b'])).toStrictEqual(['a'])
        expect(setFunctions.Difference(['a', 'b', 'c'], ['b', 'c', 'd'])).toStrictEqual(['a'])
        expect(setFunctions.Difference(['a', null], ['b', null])).toStrictEqual(['a'])
        expect(setFunctions.Difference(['a', null], ['b'])).toStrictEqual(['a', undefined])
        expect(setFunctions.Difference([null], [undefined])).toStrictEqual([])
    })
    it('should calculate symmetric difference between two arrays', () => {
        expect(setFunctions.SymmetricDifference(null, null)).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(undefined, null)).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(null, undefined)).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(undefined, undefined)).toStrictEqual([])
        expect(setFunctions.SymmetricDifference([], null)).toStrictEqual([])
        expect(setFunctions.SymmetricDifference([], [])).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(['a'], undefined)).toStrictEqual(['a'])
        expect(setFunctions.SymmetricDifference(['a'], ['b'])).toStrictEqual(['a', 'b'])
        expect(setFunctions.SymmetricDifference(['a'], ['a'])).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(['a'], ['a', 'a'])).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(['a', 'a'], ['a'])).toStrictEqual([])
        expect(setFunctions.SymmetricDifference(['a', 'b', 'c'], ['b', 'c', 'd'])).toStrictEqual(['a', 'd'])
        expect(setFunctions.SymmetricDifference(['a', null], ['b', null])).toStrictEqual(['a', 'b'])
        expect(setFunctions.SymmetricDifference(['a', null], ['b'])).toStrictEqual(['a', undefined, 'b'])
        expect(setFunctions.SymmetricDifference([null, undefined], [undefined])).toStrictEqual([])
    })
    it('should return distinct values of array', () => {
        expect(setFunctions.Distinct(null)).toStrictEqual([])
        expect(setFunctions.Distinct(undefined)).toStrictEqual([])
        expect(setFunctions.Distinct([])).toStrictEqual([])
        expect(setFunctions.Distinct(['a', 'a', 'b'])).toStrictEqual(['a', 'b'])
        expect(setFunctions.Distinct(['a', 'a', 'b', null, null, undefined])).toStrictEqual(['a', 'b', undefined])
    })
})
