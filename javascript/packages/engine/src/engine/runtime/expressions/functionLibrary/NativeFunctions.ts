/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { message } from './function.utils'
import { Numbers } from '../math/Numbers'
import { DateCalculator } from '../date/DateCalculator'
import { InternalFunctionScope } from './Registry'

export const nativeFunctions = {
    _s,
    _n,
    _b,
    _eq,
    _neq,
    _mt,
    _mte,
    _lt,
    _lte,
    _in,
    _mod,
    _sub,
    _mult,
    _pow,
    _div,
    _add,
    _o,
    _flatMap,
}

/**
 * Evaluates function on object and flat maps if object and/or property is a collection.
 * If object is a collection then function {@link getValue} is applied on each element of the collection.
 * If result of function {@link getValue} is a collection, then the result is flat mapped.
 * If object is not a collection, then function {@link getValue} is applied on object and result is returned.
 * Used to support automatic flat mapping of dynamic types.
 *
 * @param object
 * @param getValue
 */
function _flatMap(object: unknown, getValue: (o: unknown) => unknown | undefined): unknown | undefined {
    if (Array.isArray(object)) {
        return object.map(x => getValue(x)).reduce((array: unknown[], value) => array.concat(value), [])
    }
    return getValue(object)
}

/**
 * Returns first object that has requested property
 *
 * @param property
 * @param objects
 */
function _o(property: string, objects: unknown[]): unknown | undefined {
    for (const o of objects) {
        if (o && Object.prototype.hasOwnProperty.call(o, property)) {
            return o
        }
    }
    return undefined
}

/**
 * Coerces value to string in consistent manner throughout different Kraken Engine implementations
 *
 * @param obj
 * @return a coerced string
 * @throws error if value type is not string
 */
function _s(obj?: unknown): string {
    if (typeof obj === 'string') {
        return obj as string
    }
    throw new Error(message('_s', `Parameter must be string, but was ${typeof obj} instead`))
}

/**
 * Coerces value to number in consistent manner throughout different Kraken Engine implementations;
 *
 * @param obj
 * @return a coerced number
 * @throws error if value type is not number
 */
function _n(obj?: unknown): number {
    if (typeof obj === 'number') {
        return obj as number
    }
    throw new Error(message('_n', `Parameter must be number, but was ${typeof obj} instead`))
}

/**
 * Coerces value to boolean in consistent manner throughout different Kraken Engine implementations
 *
 * @param obj
 * @return a coerced boolean
 * @throws error if value type is not boolean
 */
function _b(obj?: unknown): boolean {
    if (typeof obj === 'boolean') {
        return obj as boolean
    }
    throw new Error(message('_b', `Parameter must be a boolean, but was ${typeof obj} instead`))
}

/**
 * Custom equals operator implementation
 * so that equality is consistent throughout different Kraken Engine implementations
 *
 * @param value1
 * @param value2
 * @return true if values are equal
 */
function _eq(this: InternalFunctionScope, value1?: unknown, value2?: unknown): boolean {
    return eq(value1, value2, this.dateCalculator)
}

function eq(value1: unknown, value2: unknown, dc: DateCalculator<unknown, unknown>): boolean {
    if (value1 == undefined && value2 == undefined) {
        return true
    }
    if ((dc.isDate(value1) && dc.isDate(value2)) || (dc.isDateTime(value1) && dc.isDateTime(value2))) {
        const value1Time = dc.convertDateToJavascriptDate(value1).getTime()
        const value2Time = dc.convertDateToJavascriptDate(value2).getTime()
        return value1Time === value2Time
    }
    return value1 === value2
}

/**
 * Custom not-equals operator implementation
 * so that equality is consistent throughout different Kraken Engine implementations
 *
 * @param value1
 * @param value2
 * @return true if values are not equal
 */
function _neq(this: InternalFunctionScope, value1?: unknown, value2?: unknown): boolean {
    return !eq(value1, value2, this.dateCalculator)
}

function _mt(this: InternalFunctionScope, value1?: unknown, value2?: unknown): boolean {
    if (typeof value1 === 'number' && typeof value2 === 'number') {
        return value1 > value2
    }
    const dc = this.dateCalculator
    if ((dc.isDate(value1) && dc.isDate(value2)) || (dc.isDateTime(value1) && dc.isDateTime(value2))) {
        const value1Time = dc.convertDateToJavascriptDate(value1).getTime()
        const value2Time = dc.convertDateToJavascriptDate(value2).getTime()
        return value1Time > value2Time
    }
    throw new Error(`Objects are not comparable: ${value1} ${value2}`)
}

function _mte(this: InternalFunctionScope, value1?: unknown, value2?: unknown): boolean {
    if (typeof value1 === 'number' && typeof value2 === 'number') {
        return value1 >= value2
    }
    const dc = this.dateCalculator
    if ((dc.isDate(value1) && dc.isDate(value2)) || (dc.isDateTime(value1) && dc.isDateTime(value2))) {
        const value1Time = dc.convertDateToJavascriptDate(value1).getTime()
        const value2Time = dc.convertDateToJavascriptDate(value2).getTime()
        return value1Time >= value2Time
    }
    throw new Error(`Objects are not comparable: ${value1} ${value2}`)
}

function _lt(this: InternalFunctionScope, value1?: unknown, value2?: unknown): boolean {
    if (typeof value1 === 'number' && typeof value2 === 'number') {
        return value1 < value2
    }
    const dc = this.dateCalculator
    if ((dc.isDate(value1) && dc.isDate(value2)) || (dc.isDateTime(value1) && dc.isDateTime(value2))) {
        const value1Time = dc.convertDateToJavascriptDate(value1).getTime()
        const value2Time = dc.convertDateToJavascriptDate(value2).getTime()
        return value1Time < value2Time
    }
    throw new Error(`Objects are not comparable: ${value1} ${value2}`)
}

function _lte(this: InternalFunctionScope, value1?: unknown, value2?: unknown): boolean {
    if (typeof value1 === 'number' && typeof value2 === 'number') {
        return value1 <= value2
    }
    const dc = this.dateCalculator
    if ((dc.isDate(value1) && dc.isDate(value2)) || (dc.isDateTime(value1) && dc.isDateTime(value2))) {
        const value1Time = dc.convertDateToJavascriptDate(value1).getTime()
        const value2Time = dc.convertDateToJavascriptDate(value2).getTime()
        return value1Time <= value2Time
    }
    throw new Error(`Objects are not comparable: ${value1} ${value2}`)
}

/**
 * Custom in-array operator implementation
 * so that it is consistent throughout different Kraken Engine implementations
 *
 * @param array
 * @param value
 * @return true if values are equal
 * @throws error if array parameter type is not Array
 */
function _in(this: InternalFunctionScope, array?: unknown, value?: unknown): boolean {
    if (array == undefined) {
        return false
    }
    if (Array.isArray(array)) {
        for (const item of array) {
            if (eq(item, value, this.dateCalculator)) {
                return true
            }
        }
        return false
    }
    throw new Error(message('_in', `object is not array, but was ${typeof array} instead`))
}

function _add(first?: number, second?: number): number {
    return Numbers.add(_n(first), _n(second))
}

function _sub(first?: number, second?: number): number {
    return Numbers.sub(_n(first), _n(second))
}

function _mult(first?: number, second?: number): number {
    return Numbers.mult(_n(first), _n(second))
}

function _div(first?: number, second?: number): number {
    return Numbers.div(_n(first), throwIfZero(_n(second)))
}

function _mod(first?: number, second?: number): number {
    return Numbers.mod(_n(first), _n(second))
}

function _pow(first?: number, second?: number): number {
    return Numbers.pow(_n(first), _n(second))
}

function throwIfZero(n: number): number {
    if (n === 0) {
        throw new Error(message('_div', 'Division by zero. Rule will be ignored due to missing data.'))
    }
    return n
}
