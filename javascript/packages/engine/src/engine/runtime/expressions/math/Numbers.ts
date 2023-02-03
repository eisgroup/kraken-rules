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

import { BigNumber } from 'bignumber.js'

export const Numbers = {
    add,
    sub,
    mult,
    div,
    mod,
    pow,
    sqrt,
    min,
    max,
    round,
    roundEven,
    floor,
    ceil,
    abs,
    sign,
    toString,
    avgOfArray,
    sumOfArray,
    minInArray,
    maxInArray,
    sequence,
    normalized,
}

function add(first: number, second: number): number {
    const result = normalized(first).plus(normalized(second))
    return correctIEE754Decimal64Precision(result).toNumber()
}

function sub(first: number, second: number): number {
    const result = normalized(first).minus(normalized(second))
    return correctIEE754Decimal64Precision(result).toNumber()
}

function mult(first: number, second: number): number {
    const result = normalized(first).multipliedBy(normalized(second))
    return correctIEE754Decimal64Precision(result).toNumber()
}

function div(first: number, second: number): number {
    const result = normalized(first).dividedBy(normalized(second))
    return correctIEE754Decimal64Precision(result).toNumber()
}

function mod(first: number, second: number): number {
    const result = normalized(first).modulo(normalized(second))
    return correctIEE754Decimal64Precision(result).toNumber()
}

function pow(first: number, second: number): number {
    const result = normalized(first).pow(normalized(Math.trunc(second)))
    return correctIEE754Decimal64Precision(result).toNumber()
}

function sqrt(n: number): number {
    const result = normalized(n).sqrt()
    return correctIEE754Decimal64Precision(result).toNumber()
}

function max(first: number, second: number): number {
    return Math.max(first, second)
}

function min(first: number, second: number): number {
    return Math.min(first, second)
}

function maxInArray(array: number[]): number {
    return Math.max(...array)
}

function minInArray(array: number[]): number {
    return Math.min(...array)
}

function avgOfArray(array: number[]): number {
    const sum = array.map(normalized).reduce((n1, n2) => n1.plus(n2))
    const avg = sum.dividedBy(normalized(array.length))
    return correctIEE754Decimal64Precision(avg).toNumber()
}

function sequence(fromNumber: number, toNumber: number, stepNumber: number): number[] {
    const from = normalized(fromNumber)
    const to = normalized(toNumber)
    const step = normalized(stepNumber)

    const numberSequence: number[] = []
    let element = from
    while (inRange(element, from, to)) {
        numberSequence.push(element.toNumber())
        element = element.plus(step)
    }
    return numberSequence
}

function inRange(n: BigNumber, from: BigNumber, to: BigNumber): boolean {
    return (n.comparedTo(from) >= 0 && n.comparedTo(to) <= 0) || (n.comparedTo(to) >= 0 && n.comparedTo(from) <= 0)
}

function sumOfArray(array: number[]): number {
    return array
        .map(normalized)
        .reduce((n1, n2) => n1.plus(n2))
        .toNumber()
}

function round(n: number, scale?: number): number {
    if (scale == undefined) {
        return normalized(n).integerValue(BigNumber.ROUND_HALF_UP).toNumber()
    }
    return normalized(n).decimalPlaces(scale, BigNumber.ROUND_HALF_UP).toNumber()
}

function roundEven(n: number, scale?: number): number {
    if (scale == undefined) {
        return normalized(n).integerValue(BigNumber.ROUND_HALF_EVEN).toNumber()
    }
    return normalized(n).decimalPlaces(scale, BigNumber.ROUND_HALF_EVEN).toNumber()
}

function floor(n: number): number {
    return normalized(n).integerValue(BigNumber.ROUND_FLOOR).toNumber()
}

function ceil(n: number): number {
    return normalized(n).integerValue(BigNumber.ROUND_CEIL).toNumber()
}

function abs(n: number): number {
    return normalized(n).abs().toNumber()
}

function sign(n: number): number {
    const bn = normalized(n)
    if (bn.isZero()) {
        return 0
    } else if (bn.isPositive()) {
        return 1
    } else {
        return -1
    }
}

function toString(n: number): string {
    return normalized(n).toFixed()
}

function normalized(n: number): BigNumber {
    return correctIEE754Decimal64Precision(new BigNumber(n))
}

function correctIEE754Decimal64Precision(n: BigNumber): BigNumber {
    // IEEE 754R Decimal64 (decimalPlaces: 16, RoundingMode: HALF_EVEN)
    // cannot use Decimal128 because javascript supports only 64 bit decimals
    return n.precision(16, BigNumber.ROUND_HALF_EVEN)
}
