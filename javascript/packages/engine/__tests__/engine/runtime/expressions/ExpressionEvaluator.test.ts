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

import { ExpressionEvaluator, registry } from "../../../../src/engine/runtime/expressions/ExpressionEvaluator";
import { mock } from "../../../mock";
import { ComplexExpression, PropertyExpression } from "../../../../src/engine/runtime/expressions/RuntimeExpression";
import { ExpressionEvaluationResult } from "../../../../src/engine/runtime/expressions/ExpressionEvaluationResult";
import { dateFunctions } from "../../../../src/engine/runtime/expressions/functionLibrary/DateFunctions";

const evaluator = ExpressionEvaluator.DEFAULT;

function complex(expression: string): ComplexExpression {
    return {
        type: "ComplexExpression",
        expression
    };
}

function property(expression: string): PropertyExpression {
    return {
        type: "PropertyExpression",
        expression
    };
}

function unwrap(expressionResult: ExpressionEvaluationResult.Result): any {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult });
        throw new Error("Expression failed");
    }
    return expressionResult.success;
}

const { contextBuilder, data } = mock;
let dataObject = data.empty();
beforeEach(() => dataObject = data.empty());

describe("ExpressionEvaluator", () => {
    describe("evaluate", () => {
        it("should evaluate get with PropertyExpression", () => {
            const name = evaluator.evaluate(
                { type: "PropertyExpression", expression: "id" },
                contextBuilder.buildFromRoot(dataObject)
            );
            expect(unwrap(name)).toBe(dataObject.id);
        });
        it("should evaluate get with PathExpression", () => {
            dataObject.billingInfo = { accountName: "name" };
            const name = evaluator.evaluate(
                { type: "PathExpression", expression: "billingInfo.accountName" },
                contextBuilder.buildFromRoot(dataObject)
            );
            expect(unwrap(name)).toBe(dataObject.billingInfo.accountName);
        });
        it("should evaluate get with LiteralExpression", () => {
            dataObject.billingInfo = { accountName: "name" };
            const name = evaluator.evaluate(
                { type: "LiteralExpression", value: 3, valueType: "Number" },
                contextBuilder.buildFromRoot(dataObject)
            );
            expect(unwrap(name)).toBe(3);
        });
        it("should throw error on not supported expression", () => {
            expect(() => evaluator.evaluate(
                // @ts-expect-error
                { type: "NonExisting" } ,
                contextBuilder.buildFromRoot(dataObject)
            )).toThrow(`Evaluation of expression ${JSON.stringify({ type: "NonExisting" })} is not supported`);
        });
        describe("ComplexExpression", () => {
            it("should use '__dataObject__' and get data from DataContext", () => {
                const name = evaluator.evaluate(complex("__dataObject__.id"),
                    contextBuilder.buildFromRoot(dataObject));
                expect(unwrap(name)).toBe(dataObject.id);
            });
            it("should navigate data from DataContext", () => {
                const name = evaluator.evaluate(complex("__dataObject__.insured.id"),
                    contextBuilder.buildFromRoot(dataObject));
                expect(unwrap(name)).toBe(dataObject.insured!.id);
            });
            it("should check is age equals to 10 (true)", () => {
                const result =
                    evaluator.evaluate(complex("__dataObject__.id == '0'"),
                        contextBuilder.buildFromRoot(dataObject));
                expect(!!unwrap(result)).toBeTruthy();
            });
            it("should check two expressions with object calls to be true", () => {
                const result = evaluator.evaluate(
                    complex("__dataObject__.id == '0' && __dataObject__.cd === 'Policy'"),
                    contextBuilder.buildFromRoot(dataObject));
                expect(!!unwrap(result)).toBeTruthy();
            });
            it("should throw on NaN result", () => {
                const result = evaluator.evaluate(complex("Math.sqrt(-1)"), mock.data.dataContextEmpty());
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy();
            });
            it("should throw on +Infinity result", () => {
                const result = evaluator.evaluate(complex("1 / 0"), mock.data.dataContextEmpty());
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy();
            });
            it("should throw on -Infinity result", () => {
                const result = evaluator.evaluate(complex("-1 / 0"), mock.data.dataContextEmpty());
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy();
            });
        });
    });
    describe("evaluateSet", () => {
        describe("PropertyExpression", () => {
            it("should set string by provided path and mutate passed object", () => {
                const name = evaluator.evaluateSet(property("state"), dataObject, "AZ");
                expect(unwrap(name)).toBe("AZ");
                expect(dataObject.state).toBe("AZ");
            });
        });
        describe("ComplexExpression", () => {
            it("should set default with complex expression", () => {
                const name = evaluator.evaluateSet(complex("__dataObject__.state"), dataObject, "AZ");
                expect(unwrap(name)).toBe("AZ");
                expect(dataObject.state).toBe("AZ");
            });
            it("should set default with complex expression with context access", () => {
                const name = evaluator.evaluateSet(
                    complex("context.isState ? __dataObject__.state : __dataObject__.policyNumber"),
                    dataObject, "AZ",
                    { isState: false }
                );
                expect(unwrap(name)).toBe("AZ");
                expect(dataObject.policyNumber).toBe("AZ");
            });
            it("should access normalize function", () => {
                registry.add({
                    name : "customCalculation",
                    function(first : number, second : number) : number {
                        return this.normalize(first).multipliedBy(this.normalize(second)).toNumber();
                    }
                });
                const result = evaluator.evaluate(complex("customCalculation(0.1, 0.2)"), mock.dataContextEmpty());
                expect(unwrap(result)).toBe(0.02);
            });
        });
        describe("LiteralExpression", () => {
            it("should thrown on accessing object with literal expression", () => {
                expect(() => evaluator.evaluateSet({
                    type: "LiteralExpression",
                    value: "state",
                    valueType: "String"
                }, dataObject, "AZ"))
                    .toThrow("Access by 'LiteralExpression' type expression is not supported.");
            });
        });
        describe("PathExpression", () => {
            it("should set string by provided path and mutate passed object", () => {
                const name = evaluator.evaluateSet(
                    { type: "PathExpression", expression: "insured.name" },
                    dataObject,
                    "Thomas"
                );
                expect(unwrap(name)).toBe("Thomas");
                expect(dataObject.insured!.name).toBe("Thomas");
            });
        });
        describe("TemplateVariables", () => {
            it("should evaluate empty template variables", () => {
                const templateVariables = evaluator.evaluateTemplateVariables(
                    {
                        templateExpressions : [],
                        templateParts : [],
                        errorCode : "code"
                    },
                    contextBuilder.buildFromRoot(dataObject),
                    {}
                );
                expect(templateVariables).toStrictEqual([]);
            });
            it("should evaluate template variables", () => {
                dataObject.policyNumber = "policyNumber";
                const templateVariables = evaluator.evaluateTemplateVariables(
                    {
                        templateExpressions : [
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: "string",
                                compiledLiteralValueType: "String"
                            },
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: 10.123,
                                compiledLiteralValueType: "Number"
                            },
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: true,
                                compiledLiteralValueType: "Boolean"
                            },
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: false,
                                compiledLiteralValueType: "Boolean"
                            },
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: null
                            },
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: "2020-01-01",
                                compiledLiteralValueType: "Date"
                            },
                            {
                                expressionType: "LITERAL",
                                compiledLiteralValue: "2020-01-01T10:00:00Z",
                                compiledLiteralValueType: "DateTime"
                            },
                            {
                                expressionType: "COMPLEX",
                                expressionString: "__dataObject__.policyNumber"
                            },
                            {
                                expressionType: "COMPLEX",
                                expressionString: "__dataObject__.unknownAttribute"
                            }
                        ],
                        templateParts : [],
                        errorCode : "code"
                    },
                    contextBuilder.buildFromRoot(dataObject),
                    {}
                );

                expect(templateVariables).toStrictEqual(
                    [
                        "string",
                        "10.123",
                        "true",
                        "false",
                        "",
                        dateFunctions.Format(new Date("2020-01-01"), "YYYY-MM-DD hh:mm:ss"),
                        dateFunctions.Format(new Date("2020-01-01T10:00:00Z"), "YYYY-MM-DD hh:mm:ss"),
                        "policyNumber",
                        ""
                    ]
                );
            });
        });
    });
});
