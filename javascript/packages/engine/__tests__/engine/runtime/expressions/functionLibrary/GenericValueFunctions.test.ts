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

import {
    genericValueFunctions
} from "../../../../../src/engine/runtime/expressions/functionLibrary/GenericValueFunctions";

describe("Generic Value Functions Test", () => {
    describe("Empty function", () => {
        it("should check if value is empty", () => {
            expect(genericValueFunctions.IsEmpty(undefined)).toBeTruthy();
            expect(genericValueFunctions.IsEmpty(null)).toBeTruthy();
            expect(genericValueFunctions.IsEmpty([])).toBeTruthy();
            expect(genericValueFunctions.IsEmpty([1])).toBeFalsy();
            expect(genericValueFunctions.IsEmpty([null])).toBeFalsy();
            expect(genericValueFunctions.IsEmpty([undefined])).toBeFalsy();
            expect(genericValueFunctions.IsEmpty(0)).toBeFalsy();
            expect(genericValueFunctions.IsEmpty("")).toBeTruthy();
            expect(genericValueFunctions.IsEmpty("string")).toBeFalsy();
        });
    });
});
