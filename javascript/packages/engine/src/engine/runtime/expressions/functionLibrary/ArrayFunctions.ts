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

export const arrayFunctions = {
    Flat,
    Count,
    Join
};

function Flat(array: any): any[] | undefined {
    if (isUndefined(array)) {
        return undefined;
    }
    if (Array.isArray(array)) {
        let flattened: any[] = [];
        for (const element of array) {
            if (Array.isArray(element)) {
                flattened = flattened.concat(element);
            } else {
                flattened.push(element);
            }
        }
        return flattened;
    }
    throw new Error("Flat function accepts only ArrayLike objects");
}

function Count(array: any): number {
    if (Array.isArray(array)) {
        return array.length;
    }
    if (isUndefined(array)) {
        return 0;
    }
    return 1;
}

/**
 *
 * @param a
 * @param b
 * @return joins two arrays into single array by preserving order and duplicates
 */
function Join(a: any[] | null | undefined, b: any[] | null | undefined): any[] {
    return [...(a || []), ...(b || [])];
}

function isUndefined(array: any): boolean {
    // tslint:disable-next-line
    return array == undefined;
}
