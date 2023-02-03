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

import { pathAccessor } from '../../../../src/engine/runtime/expressions/PathAccessor'

const data = {
    one: {
        two: '2',
    },
}

describe('PathAccessor', () => {
    it('should access by path and return value', () => {
        expect(pathAccessor.access(data, 'one.two')).toBe(data.one.two)
    })
    it('should access by path without dot and return value', () => {
        expect(pathAccessor.access(data, 'one')).toBe(data.one)
    })
    it('should access by not existing path and return undefined', () => {
        expect(pathAccessor.access(data, 'one.none')).toBeUndefined()
    })
    it('should access by number string path and return undefined', () => {
        expect(pathAccessor.access(data, '0')).toBeUndefined()
    })
    it('should access by empty string path and return undefined', () => {
        expect(pathAccessor.access(data, '')).toBeUndefined()
    })
    it('should access by string symbol path and return undefined', () => {
        expect(pathAccessor.access(data, '(()')).toBeUndefined()
    })
    it('should access and set by path from one element', () => {
        const obj = { a: 1 }
        expect(pathAccessor.accessAndSet(obj, 'a', 2)).toBe(2)
        expect(obj.a).toBe(2)
    })
    it('should access and set by path from two elements', () => {
        const obj = { a: { b: 1 } }
        expect(pathAccessor.accessAndSet(obj, 'a.b', 2)).toBe(2)
        expect(obj.a.b).toBe(2)
    })
    it('should access and set by path non existing element', () => {
        const obj = { a: { b: 1 } }
        expect(pathAccessor.accessAndSet(obj, 'a.c', 2)).toBe(2)
        expect(obj.a.b).toBe(1)
    })
    it('should return undefined on deeply nested property', () => {
        const obj = { a: { b: 1 } }
        expect(pathAccessor.accessAndSet(obj, 'a.c.d', 2)).toBe(undefined)
        expect(obj.a.b).toBe(1)
    })
})
