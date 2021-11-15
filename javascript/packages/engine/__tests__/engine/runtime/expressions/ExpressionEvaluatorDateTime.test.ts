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

import { mock } from "../../../mock";
import { DataContext } from "../../../../src/engine/contexts/data/DataContext";
import { ComplexExpression } from "../../../../src/engine/runtime/expressions/RuntimeExpression";
import { ExpressionEvaluationResult } from "../../../../src/engine/runtime/expressions/ExpressionEvaluationResult";

const { evaluator } = mock;

export interface TestDataObject {
    creationTime: string;
}
const path = "__dataObject__.creationTime";

function unwrap(expressionResult: ExpressionEvaluationResult.Result): any {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult });
        throw new Error("Expression failed");
    }
    return expressionResult.success;
}

function complex(expression: string): ComplexExpression {
    return {
        type: "ComplexExpression",
        expression
    };
}

let dataObject;
let dataContext: DataContext;
beforeEach(() => {
    dataObject = { creationTime: new Date() };
    dataContext = new DataContext("1", "dummyCtx", dataObject, mock.contextInstanceInfo, {}, undefined);
});

describe("DateTime in ExpressionEvaluator", () => {
    it("should create date time passed in params", () => {
        const date = evaluator.evaluate(complex("DateTime('2011-11-11T10:10:10')"), dataContext);
        // tslint:disable-next-line
        unwrap(evaluator.evaluateSet(
            complex(path),
            dataContext.dataObject,
            unwrap(date)
        )) as TestDataObject;
        const creationTime = (dataContext.dataObject as TestDataObject).creationTime;
        expect(creationTime).k_toBeDateTime(unwrap(date));
    });
    it("should compare two dates in kraken format from command expression", () => {
        const is = evaluator.evaluate(complex(`Today() > DateTime('2011-11-11T10:10:10')`), dataContext);
        expect(unwrap(is)).toBeTruthy();
    });
    it("should create current dateTime", () => {
        expect(unwrap(evaluator.evaluate(complex("Now()"), dataContext))).k_toBeDateTime(new Date());
    });
});
