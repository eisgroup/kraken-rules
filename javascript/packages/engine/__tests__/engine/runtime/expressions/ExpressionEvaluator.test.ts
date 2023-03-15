/* eslint-disable @typescript-eslint/no-non-null-assertion */
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
import { ExpressionEvaluationResult } from 'kraken-engine-api'

import { ExpressionEvaluator } from '../../../../src/engine/runtime/expressions/ExpressionEvaluator'
import { dateFunctions } from '../../../../src/engine/runtime/expressions/functionLibrary/DateFunctions'
import { mock } from '../../../mock'
import { Contexts, Expressions } from 'kraken-model'
import { TestProduct } from 'kraken-test-product'
import Insured = TestProduct.kraken.testproduct.domain.Insured

const evaluator = ExpressionEvaluator.DEFAULT

function complex(expressionString: string): Expressions.Expression {
    return {
        expressionType: 'COMPLEX',
        expressionString,
    }
}

function unwrap(expressionResult: ExpressionEvaluationResult.Result): unknown {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult })
        throw new Error('Expression failed')
    }
    return expressionResult.success
}

const { contextBuilder, data } = mock
let dataObject = data.empty()
beforeEach(() => (dataObject = data.empty()))

describe('ExpressionEvaluator', () => {
    describe('evaluate', () => {
        it('should evaluate the expression with _in native function', () => {
            function evaluate(expressionString): unknown {
                return evaluator.evaluate({ expressionString, expressionType: 'COMPLEX' }, mock.dataContextEmpty())
            }
            expect(
                evaluate(
                    "((_in(this.Join(['A','B'],['C']), 'A') && _in(this.Join(['A','B'],['C']), 'B')) && _in(this.Join(['A','B'],['C']), 'C'))",
                ),
            ).toBeTruthy()

            expect(evaluate('_in(["a", "b"], "a")')).toBeTruthy()
        })
        it('should evaluate get with PropertyExpression', () => {
            const name = evaluator.evaluate(
                { expressionType: 'PATH', expressionString: 'id' },
                contextBuilder.buildFromRoot(dataObject),
            )
            expect(unwrap(name)).toBe(dataObject.id)
        })
        it('should evaluate get with PathExpression', () => {
            dataObject.billingInfo = { accountName: 'name' }
            const name = evaluator.evaluate(
                { expressionType: 'PATH', expressionString: 'billingInfo.accountName' },
                contextBuilder.buildFromRoot(dataObject),
            )
            expect(unwrap(name)).toBe(dataObject.billingInfo.accountName)
        })
        it('should evaluate get with LiteralExpression', () => {
            dataObject.billingInfo = { accountName: 'name' }
            const name = evaluator.evaluate(
                {
                    expressionType: 'LITERAL',
                    expressionString: '3',
                    compiledLiteralValue: 3,
                    compiledLiteralValueType: 'Number',
                },
                contextBuilder.buildFromRoot(dataObject),
            )
            expect(unwrap(name)).toBe(3)
        })
        it('should throw error on not supported expression', () => {
            expect(() =>
                evaluator.evaluate(
                    // @ts-expect-error testing negative case
                    { type: 'NonExisting' },
                    contextBuilder.buildFromRoot(dataObject),
                ),
            ).toThrow(`Evaluation of expression ${JSON.stringify({ type: 'NonExisting' })} is not supported`)
        })
        describe('ComplexExpression', () => {
            it("should use '__dataObject__' and get data from DataContext", () => {
                const name = evaluator.evaluate(complex('__dataObject__.id'), contextBuilder.buildFromRoot(dataObject))
                expect(unwrap(name)).toBe(dataObject.id)
            })
            it('should navigate data from DataContext', () => {
                const name = evaluator.evaluate(
                    complex('__dataObject__.insured.id'),
                    contextBuilder.buildFromRoot(dataObject),
                )
                expect(unwrap(name)).toBe(dataObject.insured!.id)
            })
            it('should check is age equals to 10 (true)', () => {
                const result = evaluator.evaluate(
                    complex("__dataObject__.id == '0'"),
                    contextBuilder.buildFromRoot(dataObject),
                )
                expect(!!unwrap(result)).toBeTruthy()
            })
            it('should check two expressions with object calls to be true', () => {
                const result = evaluator.evaluate(
                    complex("__dataObject__.id == '0' && __dataObject__.cd === 'Policy'"),
                    contextBuilder.buildFromRoot(dataObject),
                )
                expect(!!unwrap(result)).toBeTruthy()
            })
            it('should throw on NaN result', () => {
                const result = evaluator.evaluate(complex('Math.sqrt(-1)'), mock.data.dataContextEmpty())
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy()
            })
            it('should throw on +Infinity result', () => {
                const result = evaluator.evaluate(complex('1 / 0'), mock.data.dataContextEmpty())
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy()
            })
            it('should throw on -Infinity result', () => {
                const result = evaluator.evaluate(complex('-1 / 0'), mock.data.dataContextEmpty())
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy()
            })
            it('should invoke kel functions', () => {
                const result = mock.policyEvaluator.evaluate(
                    complex('this.Fibonacci(10) === 55'),
                    contextBuilder.buildFromRoot(dataObject),
                )
                expect(unwrap(result)).toBeTruthy()
            })
        })
    })
    describe('evaluateGet', () => {
        it('should get value by property', () => {
            dataObject.state = 'AZ'
            const name = evaluator.evaluateGet('state', dataObject)
            expect(unwrap(name)).toBe('AZ')
        })
        it('should get value by path', () => {
            dataObject.insured = {
                name: 'Peter',
            }
            const name = evaluator.evaluateGet('insured.name', dataObject)
            expect(unwrap(name)).toBe('Peter')
        })
    })
    describe('evaluateNavigationExpression', () => {
        it('should extract context by path', () => {
            dataObject.insured = {
                id: '1',
                cd: 'Insured',
                name: 'Peter',
            }
            const navigationExpression: Contexts.ContextNavigation = {
                navigationExpression: {
                    expressionType: 'PATH',
                    expressionString: 'insured',
                },
                cardinality: 'SINGLE',
                targetName: 'Insured',
            }
            const dataContext = contextBuilder.buildFromRoot(dataObject)
            const result = evaluator.evaluateNavigationExpression(navigationExpression, dataContext)
            const insured = unwrap(result) as Insured
            expect(insured.id).toBe('1')
            expect(insured.cd).toBe('Insured')
            expect(insured.name).toBe('Peter')
        })
        it('should evaluate complex', () => {
            dataObject.insured = {
                id: '1',
                cd: 'Insured',
                name: 'Peter',
            }
            const navigationExpression: Contexts.ContextNavigation = {
                navigationExpression: {
                    expressionType: 'COMPLEX',
                    expressionString: '__dataObject__.insured',
                },
                cardinality: 'SINGLE',
                targetName: 'Insured',
            }
            const dataContext = contextBuilder.buildFromRoot(dataObject)
            const result = evaluator.evaluateNavigationExpression(navigationExpression, dataContext)
            const insured = unwrap(result) as Insured
            expect(insured.id).toBe('1')
            expect(insured.cd).toBe('Insured')
            expect(insured.name).toBe('Peter')
        })
    })
    describe('evaluateSet', () => {
        describe('PropertyExpression', () => {
            it('should set string by provided path and mutate passed object', () => {
                const name = evaluator.evaluateSet('state', dataObject, 'AZ')
                expect(unwrap(name)).toBe('AZ')
                expect(dataObject.state).toBe('AZ')
            })
        })
        describe('PathExpression', () => {
            it('should set string by provided path and mutate passed object', () => {
                const name = evaluator.evaluateSet('insured.name', dataObject, 'Thomas')
                expect(unwrap(name)).toBe('Thomas')
                expect(dataObject.insured!.name).toBe('Thomas')
            })
        })
        describe('TemplateVariables', () => {
            it('should evaluate empty template variables', () => {
                const templateVariables = evaluator.evaluateTemplateVariables(
                    {
                        templateExpressions: [],
                        templateParts: [],
                        errorCode: 'code',
                    },
                    contextBuilder.buildFromRoot(dataObject),
                    {},
                )
                expect(templateVariables).toStrictEqual([])
            })
            it('should evaluate template variables', () => {
                dataObject.policyNumber = 'policyNumber'
                const templateVariables = evaluator.evaluateTemplateVariables(
                    {
                        templateExpressions: [
                            {
                                expressionType: 'LITERAL',
                                expressionString: '"string"',
                                compiledLiteralValue: 'string',
                                compiledLiteralValueType: 'String',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '10.123',
                                compiledLiteralValue: 10.123,
                                compiledLiteralValueType: 'Number',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: 'true',
                                compiledLiteralValue: true,
                                compiledLiteralValueType: 'Boolean',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: 'false',
                                compiledLiteralValue: false,
                                compiledLiteralValueType: 'Boolean',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '',
                                compiledLiteralValue: null,
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '2020-01-01',
                                compiledLiteralValue: '2020-01-01',
                                compiledLiteralValueType: 'Date',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '2020-01-01T10:00:00Z',
                                compiledLiteralValue: '2020-01-01T10:00:00Z',
                                compiledLiteralValueType: 'DateTime',
                            },
                            {
                                expressionType: 'COMPLEX',
                                expressionString: '__dataObject__.policyNumber',
                            },
                            {
                                expressionType: 'COMPLEX',
                                expressionString: '__dataObject__.unknownAttribute',
                            },
                        ],
                        templateParts: [],
                        errorCode: 'code',
                    },
                    contextBuilder.buildFromRoot(dataObject),
                    {},
                )

                expect(templateVariables).toStrictEqual([
                    'string',
                    '10.123',
                    'true',
                    'false',
                    '',
                    dateFunctions.Format(new Date('2020-01-01'), 'YYYY-MM-DD hh:mm:ss'),
                    dateFunctions.Format(new Date('2020-01-01T10:00:00Z'), 'YYYY-MM-DD hh:mm:ss'),
                    'policyNumber',
                    '',
                ])
            })
        })
    })
})
