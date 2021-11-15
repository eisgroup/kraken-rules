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

import { arrayFunctions } from "../../../../../src/engine/runtime/expressions/functionLibrary/ArrayFunctions";

describe("Array Functions Test", () => {
    describe("Count function", () => {
        it("return 0 when collection is undefined", () => {
            const count = arrayFunctions.Count(undefined);
            expect(count).toBe(0);
        });
        it("return collection length", () => {
            const count = arrayFunctions.Count([1, 2, 3]);
            expect(count).toBe(3);
        });
        it("should return 1 on non array", () => {
            const count = arrayFunctions.Count(1);
            expect(count).toBe(1);
        });
    });
    describe("Flat function", () => {
        const { Flat } = arrayFunctions;
        it("should flat 2d", () => {
            const array = [
                [1, 2, 3],
                [1, 2, 3]
            ];
            expect(Flat(array)).toHaveLength(6);
        });
        it("should flat 3d", () => {
            const array = [
                [
                    [1, 2, 3],
                    [1, 2, 3]
                ],
                [
                    [1, 2, 3],
                    [1, 2, 3]
                ]
            ];
            expect(Flat(Flat(array))).toHaveLength(12);
        });
        it("should flat empty", () => {
            expect(Flat([])).toHaveLength(0);

        });
        it("should flat undefined", () => {
            expect(Flat(undefined)).toBeUndefined();

        });
        it("should flat null", () => {

            expect(Flat(null)).toBeUndefined();

        });
        it("should flat random array", () => {
            const array = [
                1,
                "a",
                [new Date(), {}],

                Object.create(null),
                undefined
            ];
            expect(Flat(array)).toHaveLength(6);
        });
        it("should throw on non array", () => {
            expect(() => arrayFunctions.Flat(1)).toThrow();
        });
    });
    describe("Join function", () => {
        it("should join two arrays", () => {
            expect(arrayFunctions.Join(null, null)).toStrictEqual([]);
            expect(arrayFunctions.Join(null, undefined)).toStrictEqual([]);
            expect(arrayFunctions.Join(undefined, null)).toStrictEqual([]);
            expect(arrayFunctions.Join(undefined, undefined)).toStrictEqual([]);
            expect(arrayFunctions.Join([], [])).toStrictEqual([]);
            expect(arrayFunctions.Join(null, ["a"])).toStrictEqual(["a"]);
            expect(arrayFunctions.Join(["a"], ["b"])).toStrictEqual(["a", "b"]);
            expect(arrayFunctions.Join(["a"], ["b", "b", null, undefined, "a"]))
                .toStrictEqual(["a", "b", "b", null, undefined, "a"]);
        });
    });
});
