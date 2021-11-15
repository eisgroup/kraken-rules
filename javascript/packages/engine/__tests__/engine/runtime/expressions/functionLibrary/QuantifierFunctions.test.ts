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

import { quantifierFunctions } from "../../../../../src/engine/runtime/expressions/functionLibrary/QuantifierFunctions";

describe("Quantifier Functions Test", () => {
    it("should check if Any is true in array", () => {
        expect(quantifierFunctions.Any(null)).toBeFalsy();
        expect(quantifierFunctions.Any(undefined)).toBeFalsy();
        expect(quantifierFunctions.Any([])).toBeFalsy();
        expect(quantifierFunctions.Any([false, false])).toBeFalsy();
        expect(quantifierFunctions.Any([true, false])).toBeTruthy();
        expect(quantifierFunctions.Any([true, true])).toBeTruthy();

        expect(quantifierFunctions.Any([null])).toBeFalsy();
        expect(quantifierFunctions.Any([undefined])).toBeFalsy();
        expect(quantifierFunctions.Any([null, undefined, true])).toBeTruthy();
    });
    it("should check if All is true in array", () => {
        expect(quantifierFunctions.All(null)).toBeTruthy();
        expect(quantifierFunctions.All(undefined)).toBeTruthy();
        expect(quantifierFunctions.All([])).toBeTruthy();
        expect(quantifierFunctions.All([false, false])).toBeFalsy();
        expect(quantifierFunctions.All([true, false])).toBeFalsy();
        expect(quantifierFunctions.All([true, true])).toBeTruthy();

        expect(quantifierFunctions.All([null])).toBeFalsy();
        expect(quantifierFunctions.All([undefined])).toBeFalsy();
        expect(quantifierFunctions.All([null, true, true])).toBeFalsy();
    });
});
