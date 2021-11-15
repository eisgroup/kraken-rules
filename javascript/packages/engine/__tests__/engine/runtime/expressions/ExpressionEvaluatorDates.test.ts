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
import { TestProduct } from "kraken-test-product";
import { ComplexExpression } from "../../../../src/engine/runtime/expressions/RuntimeExpression";
import { ExpressionEvaluationResult } from "../../../../src/engine/runtime/expressions/ExpressionEvaluationResult";

const { evaluator } = mock;

export const q = (value: string) => {
    let modValue = value[0] !== "'" ? `'${value}` : value;
    modValue = modValue[value.length] !== "'" ? `${modValue}'` : modValue;
    return modValue;
};

function complex(expression: string): ComplexExpression {
    return {
        type: "ComplexExpression",
        expression
    };
}

function unwrap(expressionResult: ExpressionEvaluationResult.Result): ExpressionEvaluationResult.SuccessResult {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult });
        throw new Error("Expression failed");
    }
    return expressionResult.success;
}

describe("Date functions in expression evaluator", () => {
    const dataContext = mock.data.dataContextEmpty();
    it("should create current date", () => {
        const result = evaluator.evaluate(complex("Today()"), dataContext);
        expect(unwrap(result)).k_toBeTodayDate();
    });
    it("should create date passed in params", () => {
        const result = evaluator.evaluate(complex("Date('2011-11-11')"), dataContext);
        expect(unwrap(result)).k_toBeDate(new Date("2011-11-11"));
    });
    it("should add to 1 year to current date", () => {
        const result = evaluator.evaluate(complex("PlusYears(Today(), 1)"), dataContext);
        const date = new Date();
        const dateToCompare = new Date(date.getFullYear() + 1, date.getMonth(), date.getDate());
        expect(unwrap(result)).k_toBeDate(dateToCompare);
    });
    it("should add to 1 month to current date", () => {
        const result = evaluator.evaluate(complex("PlusMonths(Date('2011-11-11'), 3)"), dataContext);
        expect(unwrap(result)).k_toBeDate(new Date("2012-02-11"));
    });
    it("should add to 1 day to current date", () => {
        const result = evaluator.evaluate(complex("PlusDays(Date('2011-11-11'), 3)"), dataContext);
        expect(unwrap(result)).k_toBeDate(new Date("2011-11-14"));
    });
    it("should compare two dates from date field kraken format", () => {
        const dc = mock.data.dataContextEmpty();
        const dcObject = (dc.dataObject as unknown as TestProduct.kraken.testproduct.domain.Policy);
        dcObject.accessTrackInfo!.createdOn = new Date(2000, 1, 1);
        const result = evaluator.evaluate(complex(`Today() > __dataObject__.accessTrackInfo.createdOn`), dc);
        expect(unwrap(result)).toBeTruthy();
    });
    it("should test NumberOfDaysBetween to true", () => {
        const result = evaluator.evaluate(complex(
            "NumberOfDaysBetween(Date('2011-11-01'), Date('2011-11-10')) == 9"),
            dataContext
        );
        expect(unwrap(result)).toBeTruthy();
    });
    it("should test NumberOfDaysBetween to false", () => {
        const result = evaluator.evaluate(complex(
            "NumberOfDaysBetween(Date('2011-11-01'), Date('2011-11-11')) == 9"),
            dataContext
        );
        expect(unwrap(result)).toBeFalsy();
    });
    it("should test NumberOfDaysBetween to true, with negative", () => {
        const result = evaluator.evaluate(complex(
            "NumberOfDaysBetween(Date('2011-11-10'), Date('2011-11-01')) == 9"),
            dataContext
        );
        expect(unwrap(result)).toBeTruthy();
    });
    it("should test IsDateBetween to true", () => {
        const result = evaluator.evaluate(complex(
            "IsDateBetween(Date('2011-11-05'), Date('2011-11-01'), Date('2011-11-10'))"),
            dataContext
        );
        expect(unwrap(result)).toBeTruthy();
    });
    it("should test IsDateBetween to false", () => {
        const result = evaluator.evaluate(complex(
            "IsDateBetween(Date('2011-11-11'), Date('2011-11-01'), Date('2011-11-10'))"),
            dataContext
        );
        expect(unwrap(result)).toBeFalsy();
    });
    it("should test IsDateBetween inclusive to false", () => {
        const result = evaluator.evaluate(complex(
            "IsDateBetween(Date('2011-11-10'), Date('2011-11-01'), Date('2011-11-10'))"),
            dataContext
        );
        expect(unwrap(result)).toBeTruthy();
    });
});
