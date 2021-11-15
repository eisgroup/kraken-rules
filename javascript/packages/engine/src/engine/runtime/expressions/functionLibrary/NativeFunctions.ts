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

// tslint:disable: triple-equals

import { message } from "./function.utils";
import { Numbers } from "./../math/Numbers";

export const nativeFunctions = {
    _s,
    _n,
    _nd,
    _b,
    _eq,
    _neq,
    _in,
    _mod,
    _sub,
    _mult,
    _pow,
    _div,
    _add
};

/**
 * Coerces value to string in consistent manner throughout different Kraken Engine implementations
 *
 * @param obj
 * @return a coerced string
 * @throws error if value type is not string
 */
function _s(obj?: any): string {
    if (typeof obj === "string") {
        return obj as string;
    }
    throw new Error(message("_s", `Parameter must be string, but was ${typeof obj} instead`));
}

/**
 * Coerces value to number in consistent manner throughout different Kraken Engine implementations;
 *
 * @param obj
 * @return a coerced number
 * @throws error if value type is not number
 */
function _n(obj?: any): number {
    if (typeof obj === "number") {
        return obj as number;
    }
    throw new Error(message("_n", `Parameter must be number, but was ${typeof obj} instead`));
}

/**
 * Coerces value to number or Date for comparison
 * in consistent manner throughout different Kraken Engine implementations
 *
 * @param obj
 * @return a coerced number or Date
 * @throws error if value type is not number or Date
 */
function _nd(obj?: any): number | Date {
    if (typeof obj === "number") {
        return obj as number;
    }
    if (obj instanceof Date) {
        return obj as Date;
    }
    throw new Error(message("_nd", `Parameter must be number or Date, but was ${typeof obj} instead`));
}

/**
 * Coerces value to boolean in consistent manner throughout different Kraken Engine implementations
 *
 * @param obj
 * @return a coerced boolean
 * @throws error if value type is not boolean
 */
function _b(obj?: any): boolean {
    if (typeof obj === "boolean") {
        return obj as boolean;
    }
    throw new Error(message("_b", `Parameter must be a boolean, but was ${typeof obj} instead`));
}

/**
 * Custom equals operator implementation
 * so that equality is consistent throughout different Kraken Engine implementations
 *
 * @param value1
 * @param value2
 * @return true if values are equal
 */
function _eq(value1?: any, value2?: any): boolean {
    if (value1 == undefined && value2 == undefined) {
        return true;
    }
    if (value1 instanceof Date && value2 instanceof Date) {
        return value1.getTime() === value2.getTime();
    }
    return value1 === value2;
}

/**
 * Custom not-equals operator implementation
 * so that equality is consistent throughout different Kraken Engine implementations
 *
 * @param value1
 * @param value2
 * @return true if values are not equal
 */
function _neq(value1?: any, value2?: any): boolean {
    return !_eq(value1, value2);
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
function _in(array?: any, value?: any): boolean {
    if (array == undefined) {
        return false;
    }
    if (Array.isArray(array)) {
        for (const item of array) {
            if (_eq(item, value)) {
                return true;
            }
        }
        return false;
    }
    throw new Error(message("_in", `object is not array, but was ${typeof array} instead`));
}

function _add(first? : number, second? : number) : number {
    return Numbers.add(_n(first), _n(second));
}

function _sub(first? : number, second? : number) : number {
    return Numbers.sub(_n(first), _n(second));
}

function _mult(first? : number, second? : number) : number {
    return Numbers.mult(_n(first), _n(second));
}

function _div(first? : number, second? : number) : number {
    return Numbers.div(_n(first), throwIfZero(_n(second)));
}

function _mod(first? : number, second? : number) : number {
    return Numbers.mod(_n(first), _n(second));
}

function _pow(first? : number, second? : number) : number {
    return Numbers.pow(_n(first), _n(second));
}

function throwIfZero(n : number) : number {
    if (n === 0) {
        throw new Error(message("_div", "Division by zero. Rule will be ignored due to missing data."));
    }
    return n;
}
