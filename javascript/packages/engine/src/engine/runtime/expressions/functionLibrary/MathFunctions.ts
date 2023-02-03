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
import { Numbers } from './../math/Numbers'
import { message } from '../functionLibrary/function.utils'
import { Moneys } from '../math/Moneys'

export const mathFunctions = {
    Sum,
    Avg,
    Min,
    Max,
    Round,
    RoundEven,
    Floor,
    Ceil,
    Abs,
    Sign,
    Sqrt,
    NumberSequence,
}

/**
 * Rounds to scale using IEEE 754 Round Half Up strategy.
 * <p/>
 * A Half Up strategy:
 * if the number falls midway,
 * then it is rounded to the nearest value above (for positive numbers) or below (for negative numbers)
 *
 * @param n
 * @param scale optional parameter, if not provided then will round to Integer
 * @return rounded number
 */
function Round(n: unknown, scale?: number): number {
    return Numbers.round(toNumber(n), scale)
}

/**
 * Rounds to scale using IEEE 754 Round Half Even strategy.
 * <p/>
 * A Half Even strategy:
 * if the number falls midway, then it is rounded to the nearest value with an even least significant digit
 *
 * @param n
 * @param scale optional parameter, if not provided then will round to Integer
 * @return rounded number
 */
function RoundEven(n: unknown, scale?: number): number {
    return Numbers.roundEven(toNumber(n), scale)
}

/**
 * @param n
 * @return  result of a mathematical floor function which returns
 *          greatest integer that is less than or equal to provided number
 */
function Floor(n: unknown): number {
    return Numbers.floor(toNumber(n))
}

/**
 * @param n
 * @return  result of a mathematical ceiling function which returns
 *          the least integer that is greater than or equal to provided number
 */
function Ceil(n: unknown): number {
    return Numbers.ceil(toNumber(n))
}
/**
 *
 * @param n
 * @return absolute number
 */
function Abs(n: unknown): number {
    return Numbers.abs(toNumber(n))
}

/**
 *
 * @param n
 * @return  a result of a mathematical signum function, which returns:
 *          <ul>
 *              <li>-1 - if number is less than zero</li>
 *              <li> 0 - if number is equal to zero</li>
 *              <li>+1 - if number is greater than zero</li>
 *          </ul>
 */
function Sign(n: unknown): number {
    return Numbers.sign(toNumber(n))
}

/**
 *
 * @param n
 * @return square root
 */
function Sqrt(n: unknown): number {
    return Numbers.sqrt(toNumber(n))
}

/**
 *
 * @param array that contains numbers
 * @return  average over all numbers in collection.
 *          If collection contains `null` value then rule will be ignored due to missing data.
 *          If collection is null then null is returned.
 */
function Avg(array: unknown): number | undefined {
    if (isUndefined(array) || isEmpty(array)) {
        return undefined
    }
    if (Array.isArray(array)) {
        return Numbers.avgOfArray(array.map(toNumber))
    }
    throw new Error('Avg function accepts only ArrayLike objects')
}

/**
 *
 * @param array that contains numbers
 * @return  sum over all numbers in collection.
 *          If collection contains `null` value then rule will be ignored due to missing data.
 *          If collection is null then null is returned.
 */
function Sum(array: unknown): number | undefined {
    if (isUndefined(array) || isEmpty(array)) {
        return undefined
    }
    if (Array.isArray(array)) {
        return Numbers.sumOfArray(array.map(toNumber))
    }
    throw new Error('Sum function accepts only ArrayLike objects')
}

/**
 *
 * Returns smallest number or date.
 * Returns smallest number or date from array of numbers or dates when array is passed as a first parameter
 * and second parameter is not passed.
 * Returns smallest number or date out of two numbers or dates when both parameters are passed.
 *
 * @param first is an array of numbers or array of dates or a single number or date
 * @param second number or date
 * @return smallest number or date
 */
function Min(first: unknown, second?: unknown): number | Date | undefined {
    if (arguments.length === 1) {
        return MinArray(first)
    }
    if (isNumber(first) && isNumber(second)) {
        return Numbers.min(toNumber(first), toNumber(second))
    }
    if (isDate(first) && isDate(second)) {
        return first < second ? first : second
    }
    throw new Error(
        `Function 'Min' parameters are invalid: ${first}, ${second}. Parameters must be only numbers or only dates.`,
    )
}

/**
 *
 * Returns largest number or date.
 * Returns largest number or date from array of numbers or dates when array is passed as a first parameter
 * and second parameter is not passed.
 * Returns largest number or date out of two numbers or dates when both parameters are passed.
 *
 * @param first is an array of numbers or array of dates or a single number or date
 * @param second number or date
 * @return largest number or date
 */
function Max(first: unknown, second?: unknown): number | Date | undefined {
    if (arguments.length === 1) {
        return MaxArray(first)
    }
    if (isNumber(first) && isNumber(second)) {
        return Numbers.max(toNumber(first), toNumber(second))
    }
    if (isDate(first) && isDate(second)) {
        return first > second ? first : second
    }
    throw new Error(
        `Function 'Max' parameters are invalid: ${first}, ${second}. Parameters must be only numbers or only dates.`,
    )
}

function MinArray(array: unknown): number | Date | undefined {
    if (isUndefined(array) || isEmpty(array)) {
        return undefined
    }
    if (Array.isArray(array)) {
        if (array.every(isNumber)) {
            return Numbers.minInArray(array.map(toNumber))
        }
        if (array.every(isDate)) {
            return minDateInArray(array)
        }
        throw new Error('Min function array parameter must contain only numbers or only dates')
    }
    throw new Error('Min function accepts only ArrayLike objects')
}

function MaxArray(array: unknown): number | Date | undefined {
    if (isUndefined(array) || isEmpty(array)) {
        return undefined
    }
    if (Array.isArray(array)) {
        if (array.every(isNumber)) {
            return Numbers.maxInArray(array.map(toNumber))
        }
        if (array.every(isDate)) {
            return maxDateInArray(array)
        }
        throw new Error('Max function array parameter must contain only numbers or only dates')
    }
    throw new Error('Max function accepts only ArrayLike objects')
}

function minDateInArray(array: Date[]): Date {
    let smallestDate = array[0]
    for (const date of array) {
        if (date < smallestDate) {
            smallestDate = date
        }
    }
    return smallestDate
}

function maxDateInArray(array: Date[]): Date {
    let largestDate = array[0]
    for (const date of array) {
        if (date > largestDate) {
            largestDate = date
        }
    }
    return largestDate
}

function isUndefined(array: unknown): boolean {
    return array == undefined
}

function isEmpty(array: unknown): boolean {
    return Array.isArray(array) && !array.length
}

function isDate(value: unknown): value is Date {
    return value instanceof Date
}

function isNumber(value: unknown): value is number {
    return Moneys.isMoney(value) || typeof value === 'number'
}

function toNumber(value: unknown): number {
    if (Moneys.isMoney(value)) {
        return value.amount as number
    } else if (typeof value === 'number') {
        return value as number
    }
    throw new Error(
        'Mathematical functions accepts only ' +
            "'Number' and/or 'Money' type values, but got value ('" +
            value +
            ' of type ' +
            typeof value +
            "') ",
    )
}

/**
 * Generates a sequence of numbers by adding 1 to starting number until it is less than
 * or equals to ending number. Returns a collection of numbers in order.
 * Parameters cannot be {@code null}.
 *
 * @param from sequence starting number
 * @param to   sequence end number
 * @param step   number to add each step
 * @returns ordered collection
 * @since 11.2
 */
function NumberSequence(from: number, to: number, step?: number): number[] {
    return Numbers.sequence(nonNull(from), nonNull(to), resolveStep(from, to, step))
}

function resolveStep(from: number, to: number, step?: number): number {
    if (step != undefined) {
        validateDirection(from, to, step)
        return step
    }
    return from > to ? -1 : 1
}

function validateDirection(from: number, to: number, step: number): void {
    const direction = to < from ? -1 : 1
    const directionOfStep = step > 0 ? 1 : -1
    if (direction !== directionOfStep) {
        throw new Error(
            message(NumberSequence.name, 'These parameters would generate an infinite sequence of numbers.'),
        )
    }
}

function nonNull(n?: number): number {
    if (n == undefined) {
        throw new Error(message(NumberSequence.name, message.reason.parameterNonNull))
    }
    return n
}
