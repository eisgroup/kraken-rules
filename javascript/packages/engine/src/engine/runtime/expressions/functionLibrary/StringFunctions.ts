import { message } from "./function.utils";
import { Numbers } from "./../math/Numbers";

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
type Nullable<T> = T | null | undefined;
// tslint:disable: triple-equals

/**
 *
 * @param value
 * @return returns true is string is null, undefined, empty or consists of whitespace characters only
 * @throws error if value type is not string
 * @example
 * IsBlank("   ") // true
 * IsBlank(null) // true
 * IsBlank("a") // false
 */
function IsBlank(value: Nullable<string>): boolean {
    if (value == undefined) {
        return true;
    }
    if (typeof value !== "string") {
        throw new Error(message("IsBlank", "Parameter must be a string"));
    }
    return value.trim().length === 0;
}

/**
 * Extracting text by parameters from provided text.
 *
 * @param value      to substring
 * @param beginIndex
 * @return left overs of substring
 * @example
 * Substring("SubstringValue", 3) // "stringValue"
 * Substring("value", 3, 4) // "lu"
 */
function Substring(value: Nullable<string>, beginIndex: Nullable<number>, endIndex: Nullable<number>): string {
    if (value == undefined) {
        throw new Error(message("Substring", message.reason.firstParam));
    }
    if (beginIndex == undefined) {
        throw new Error(message("Substring", message.reason.secondParam));
    }
    if (endIndex == undefined) {
        if (beginIndex > value.length || beginIndex < 0) {
            throw new Error(
                `Cannot perform substring to value: " + ${value} + ", with begin index: " + beginIndex`
            );
        }
        return value.substring(beginIndex);
    } else {
        if (beginIndex > value.length || beginIndex < 0 || endIndex > value.length || beginIndex > endIndex) {
            throw new Error(
                "Cannot perform substring to value: " + value + ", " +
                "with begin index: " + beginIndex + ", and end index: " + endIndex
            );
        }
        return value.substring(beginIndex, endIndex);
    }
}

/**
 * Concatenates objects to one string. It will ignore {@code undefined} and {@code null}
 *
 * @param items to concatenate
 * @return concatenated string
 * @example
 * Concat(["a", undefined, "c"]) // "ac"
 */
function Concat(array?: Nullable<any[]>): string {
    if (!array) {
        return "";
    }
    return array.filter(t => t != undefined).join("");
}

/**
 * Calculates string length. If string is {@code null} or {@code undefined}, returns {@code 0}
 *
 * @param string to calculate length of
 * @return length of string
 * @example
 * StringLength("123") // 3
 */
function StringLength(text: Nullable<string>): number {
    if (!text) {
        return 0;
    }
    return text.length;
}

/**
 * Checks for element to be included in collection or string.
 * If {@code null} appears in as a collection or element to search {@code false}
 * will be returned.
 *
 * @param stringsOrString collection or string of elements to be checked
 * @param searchElement   element to search in string or collection
 * @return does collection or string contains search element.
 * @throws Error if not array or string or {@code null} is passed as a search parameter
 * @example
 * Includes("abc", "c") //true
 * Includes(["a", "b", "c"], "c") //true
 * Includes([1, 3, 5], 5) //true
 */
function Includes(stringOrStrings: Nullable<any | any[]>, searchElement: Nullable<any>): boolean {
    if (searchElement == undefined || stringOrStrings == undefined) {
        return false;
    }
    if (typeof stringOrStrings === "string") {
        return stringOrStrings.indexOf(searchElement) != -1;
    }
    if (Array.isArray(stringOrStrings)) {
        if (stringOrStrings.length) {
            return stringOrStrings.indexOf(searchElement.toString()) != -1;
        } else {
            return false;
        }
    }
    throw new Error("Cannot execute function 'includes' on non array or non string elements.");
}

/**
 * Creates a string from number. If {@code null} is passed then empty string will be returned.
 *
 * @param number to convert to string
 * @return number converted to string
 * @throws Error when not number elements are passed to convert
 * @example
 * NumberToString(1) // "1"
 * NumberToString(1.1) // "1.1"
 * NumberToString(undefined) // ""
 */
function NumberToString(n: Nullable<any>): string {
    if (typeof n === "number") {
        return Numbers.toString(n);
    }
    if (n == undefined) {
        return "";
    }
    throw new Error("Cannot execute function 'String' on not number elements.");
}

/**
 * Pads string on the left side if it's shorter than length. If it is longer that length,
 * base string will be returned as is.
 *
 * @param base      string to pad. Default is ""
 * @param filler    to fill the space to desired length. Only one length strings are applicable here. Default is " "
 * @param length    length of final string. Default is 0.
 * @return padded string.
 * @throws Error if second parameters length is more than one
 */
function PadLeft(base: string = "", filler: string = " ", length: number = 0): string {
    if (filler.length > 1) {
        throw new Error("Failed to execute function 'PadLeft': second parameter length must " +
            +" be '1', got '" + filler.length + "'");
    }
    if (base.length >= length) {
        return base;
    }
    const numberOfCharsToFill = length - base.length;
    const repeated = filler.repeat(numberOfCharsToFill);
    return repeated + base;
}

/**
 * Pads string on the right side if it's shorter than length. If it is longer that length,
 * base string will be returned as is.
 *
 * @param base      string to pad. Default is ""
 * @param filler    to fill the space to desired length. Only one length strings are applicable here. Default is " "
 * @param length    length of final string. Default is 0.
 * @return padded string.
 * @throws Error if second parameters length is more than one
 */
function PadRight(base: string = "", filler: string = " ", length: number = 0): string {
    if (filler.length > 1) {
        throw new Error("Failed to execute function 'PadRight': second parameter length must " +
            +" be '1', got '" + filler.length + "'");
    }
    if (base.length >= length) {
        return base;
    }
    const numberOfCharsToFill = length - base.length;
    const repeated = filler.repeat(numberOfCharsToFill);
    return base + repeated;
}

/**
 * Converts text to lowercase.
 * If text is {@code null}, {@code null} will be returned
 * If text is {@code undefined}, {@code undefined} will be returned
 *
 * @param text to convert
 * @return converted text to uppercase
 */
function LowerCase(text?: string): Nullable<string> {
    if (text == undefined) {
        return text;
    }
    return text.toLowerCase();
}

/**
 * Converts text to uppercase.
 * If text is {@code null}, {@code null} will be returned
 * If text is {@code undefined}, {@code undefined} will be returned
 *
 * @param text to convert
 * @return converted text to uppercase
 */
function UpperCase(text?: string): Nullable<string> {
    if (text == undefined) {
        return text;
    }
    return text.toUpperCase();
}

/**
 * Removes leading and trailing white spaces.
 * If text is {@code null}, {@code null} will be returned
 * If text is {@code undefined}, {@code undefined} will be returned
 *
 * @param text to trim
 * @throws error if value type is not string
 * @return trimmed text
 */
function Trim(text?: string): Nullable<string> {
    if (text == undefined) {
        return text;
    }
    if (typeof text !== "string") {
        throw new Error(message("Trim", "Parameter must be a string"));
    }
    return text.trim();
}

/**
 * Checks string to start with some text. If text to check is shorter, than subtext,
 * {@code false} will be returned.
 *
 * @param text to check for start.
 * @param start to be at the start of the text.
 * @throws Error if 'text' parameter is absent
 * @throws Error if 'start' parameter is absent
 * @throws Error if 'text' parameter is not string
 */
function StartsWith(text?: string, start?: string): boolean {
    if (text == undefined) {
        throw new Error(message("StartsWith", message.reason.firstParam));
    }
    if (start == undefined) {
        throw new Error(message("StartsWith", message.reason.secondParam));
    }
    if (typeof text != "string") {
        throw new Error(message(
            "StartsWith",
            `second parameter must be string, instead got value: '${text}', type '${typeof text}'`
        ));
    }
    return text.startsWith(start);
}

/**
 * Checks string to ends with some text. If text to check is shorter, than subtext,
 * {@code false} will be returned.
 *
 * @param text to check for start.
 * @param start to be at the start of the text.
 * @throws Error if 'text' parameter is absent
 * @throws Error if 'start' parameter is absent
 * @throws Error if 'text' parameter is not string
 */
function EndsWith(text?: string, start?: string): boolean {
    if (text == undefined) {
        throw new Error(message("EndsWith", message.reason.firstParam));
    }
    if (start == undefined) {
        throw new Error(message("EndsWith", message.reason.secondParam));
    }
    if (typeof text != "string") {
        throw new Error(message(
            "EndsWith",
            `second parameter must be string, instead got value: '${text}', type '${typeof text}'`
        ));
    }
    return text.endsWith(start);
}

export const stringFunctions = {
    IsBlank,
    EndsWith,
    StartsWith,
    Substring,
    Concat,
    StringLength,
    Includes,
    NumberToString,
    PadLeft,
    PadRight,
    LowerCase,
    UpperCase,
    Trim
};
