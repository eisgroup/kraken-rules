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

import { FunctionCache } from "../../../src/engine/runtime/expressions/FunctionCache";

describe("FunctionCache", () => {
    it("should compile function in cache and return it", () => {
        const fx = new FunctionCache().compute("return Math.round(a)", ["a", "return Math.round(a)"]);
        expect(fx(1.001)).toBe(1);
    });
});
