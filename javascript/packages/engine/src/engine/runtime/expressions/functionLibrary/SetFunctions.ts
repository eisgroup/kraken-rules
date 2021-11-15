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

export const setFunctions = {
    Union,
    Intersection,
    Difference,
    SymmetricDifference,
    Distinct
};

/**
 * @param a
 * @param b
 * @return a mathematical union of two sets; null parameter is treated as empty set
 */
function Union(a?: any[] | null, b?: any[] | null): any[] {
    const setA = new Set(normalizeNulls(a));
    const setB = new Set(normalizeNulls(b));
    const union = new Set([...setA, ...setB]);
    return [...union];
}

/**
 * @param a
 * @param b
 * @return a mathematical intersection of two sets; null parameter is treated as empty set
 */
function Intersection(a?: any[] | null, b?: any[] | null): any[] {
    const setA = new Set(normalizeNulls(a));
    const setB = new Set(normalizeNulls(b));
    const intersection = new Set([...setA].filter(x => setB.has(x)));
    return [...intersection];
}

/**
 * @param a
 * @param b
 * @return a mathematical difference between first set and second set; null parameter is treated as empty set
 */
function Difference(a?: any[] | null, b?: any[] | null): any[] {
    const setA = new Set(normalizeNulls(a));
    const setB = new Set(normalizeNulls(b));
    const difference = new Set([...setA].filter(x => !setB.has(x)));
    return [...difference];
}

/**
 * @param a
 * @param b
 * @return a mathematical symmetric different of two sets; null parameter is treated as empty set
 */
function SymmetricDifference(a?: any[] | null, b?: any[] | null): any[] {
    const setA = new Set(normalizeNulls(a));
    const setB = new Set(normalizeNulls(b));
    const differenceA = new Set([...setA].filter(x => !setB.has(x)));
    const differenceB = new Set([...setB].filter(x => !setA.has(x)));
    return [...differenceA, ...differenceB];
}

/**
 * @param a
 * @param b
 * @return a set of values with duplicates removed; null parameter is treated as empty set
 */
function Distinct(a?: any[] | null): any[] {
    return [...new Set(normalizeNulls(a))];
}

function normalizeNulls(a?: any[] | null): any[] {
    return a == undefined ? [] : a.map(i => i === null ? undefined : i);
}
