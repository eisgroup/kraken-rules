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

import { moneyFunctions } from "../../../../../src/engine/runtime/expressions/functionLibrary/MoneyFunctions";

describe("MoneyFunction for ExpressionEvaluator", () => {
    it("should extract amount", () => {
        expect(moneyFunctions.FromMoney({currency : "USD", amount : 100})).toBe(100);
    });
    it("should return undefined if Money is undefined", () => {
        expect(moneyFunctions.FromMoney(undefined)).toBe(undefined);
    });
});
