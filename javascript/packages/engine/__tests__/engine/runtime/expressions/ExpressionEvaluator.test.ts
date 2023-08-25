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
import { mock } from '../../../mock'
import { Contexts, Expressions } from 'kraken-model'
import { TestProduct } from 'kraken-test-product'
import Insured = TestProduct.kraken.testproduct.domain.Insured
import { Moneys } from '../../../../src/engine/runtime/expressions/math/Moneys'

const evaluator = ExpressionEvaluator.DEFAULT

function complex(expressionString: string): Expressions.ComplexExpression {
    return {
        expressionType: 'COMPLEX',
        expressionString,
        originalExpressionString: expressionString,
    }
}
function path(expressionString: string): Expressions.PathExpression {
    return {
        expressionType: 'PATH',
        expressionString,
        originalExpressionString: expressionString,
    }
}

function unwrap(expressionResult: ExpressionEvaluationResult.Result): unknown {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult })
        throw new Error('Expression failed')
    }
    return expressionResult.success
}

const { session, contextBuilder, data } = mock
let dataObject = data.empty()
beforeEach(() => (dataObject = data.empty()))

describe('ExpressionEvaluator', () => {
    describe('evaluate', () => {
        it('should evaluate the expression with _in native function', () => {
            function evaluate(expressionString): unknown {
                return evaluator.evaluate(complex(expressionString), mock.dataContextEmpty(), session)
            }
            expect(
                evaluate(
                    "((_in(this.Join(['A','B'],['C']), 'A') && _in(this.Join(['A','B'],['C']), 'B')) && _in(this.Join(['A','B'],['C']), 'C'))",
                ),
            ).toBeTruthy()

            expect(evaluate('_in(["a", "b"], "a")')).toBeTruthy()
        })
        it('should evaluate get with PropertyExpression', () => {
            const name = evaluator.evaluate(path('id'), contextBuilder.buildFromRoot(dataObject), session)
            expect(unwrap(name)).toBe(dataObject.id)
        })
        it('should evaluate get with PathExpression', () => {
            dataObject.billingInfo = { accountName: 'name' }
            const name = evaluator.evaluate(
                path('billingInfo.accountName'),
                contextBuilder.buildFromRoot(dataObject),
                session,
            )
            expect(unwrap(name)).toBe(dataObject.billingInfo.accountName)
        })
        it('should evaluate get with LiteralExpression', () => {
            dataObject.billingInfo = { accountName: 'name' }
            const name = evaluator.evaluate(
                {
                    expressionType: 'LITERAL',
                    expressionString: '3',
                    originalExpressionString: '3',
                    compiledLiteralValue: 3,
                    expressionEvaluationType: 'Number',
                },
                contextBuilder.buildFromRoot(dataObject),
                session,
            )
            expect(unwrap(name)).toBe(3)
        })
        it('should coerce number to money', () => {
            const name = evaluator.evaluate(
                {
                    expressionType: 'LITERAL',
                    expressionString: '10',
                    originalExpressionString: '10',
                    compiledLiteralValue: 10,
                    expressionEvaluationType: 'Money',
                },
                contextBuilder.buildFromRoot(dataObject),
                session,
            )
            expect(unwrap(name)).toStrictEqual(Moneys.toMoney('USD', 10))
        })
        it('should throw error on not supported expression', () => {
            expect(() =>
                evaluator.evaluate(
                    // @ts-expect-error testing negative case
                    { type: 'NonExisting' },
                    contextBuilder.buildFromRoot(dataObject),
                    session,
                ),
            ).toThrow(`Unknown expression type encountered: ${JSON.stringify({ type: 'NonExisting' })}`)
        })
        describe('ComplexExpression', () => {
            it("should use '__dataObject__' and get data from DataContext", () => {
                const name = evaluator.evaluate(
                    complex('__dataObject__.id'),
                    contextBuilder.buildFromRoot(dataObject),
                    session,
                )
                expect(unwrap(name)).toBe(dataObject.id)
            })
            it('should navigate data from DataContext', () => {
                const name = evaluator.evaluate(
                    complex('__dataObject__.insured.id'),
                    contextBuilder.buildFromRoot(dataObject),
                    session,
                )
                expect(unwrap(name)).toBe(dataObject.insured!.id)
            })
            it('should check is age equals to 10 (true)', () => {
                const result = evaluator.evaluate(
                    complex("__dataObject__.id == '0'"),
                    contextBuilder.buildFromRoot(dataObject),
                    session,
                )
                expect(!!unwrap(result)).toBeTruthy()
            })
            it('should check two expressions with object calls to be true', () => {
                const result = evaluator.evaluate(
                    complex("__dataObject__.id == '0' && __dataObject__.cd === 'Policy'"),
                    contextBuilder.buildFromRoot(dataObject),
                    session,
                )
                expect(!!unwrap(result)).toBeTruthy()
            })
            it('should throw on NaN result', () => {
                const result = evaluator.evaluate(complex('Math.sqrt(-1)'), mock.data.dataContextEmpty(), session)
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy()
            })
            it('should throw on +Infinity result', () => {
                const result = evaluator.evaluate(complex('1 / 0'), mock.data.dataContextEmpty(), session)
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy()
            })
            it('should throw on -Infinity result', () => {
                const result = evaluator.evaluate(complex('-1 / 0'), mock.data.dataContextEmpty(), session)
                expect(ExpressionEvaluationResult.isError(result)).toBeTruthy()
            })
            it('should invoke kel functions', () => {
                const result = mock.policyEvaluator.evaluate(
                    complex('this.Fibonacci(10) === 55'),
                    contextBuilder.buildFromRoot(dataObject),
                    session,
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
                navigationExpression: path('insured'),
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
                navigationExpression: complex('__dataObject__.insured'),
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
                    session,
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
                                originalExpressionString: '"string',
                                compiledLiteralValue: 'string',
                                expressionEvaluationType: 'String',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '10.123',
                                originalExpressionString: '10.123',
                                compiledLiteralValue: 10.123,
                                expressionEvaluationType: 'Number',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: 'true',
                                originalExpressionString: 'true',
                                compiledLiteralValue: true,
                                expressionEvaluationType: 'Boolean',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: 'false',
                                originalExpressionString: 'false',
                                compiledLiteralValue: false,
                                expressionEvaluationType: 'Boolean',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '',
                                originalExpressionString: '',
                                compiledLiteralValue: null,
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '2020-01-01',
                                originalExpressionString: '2020-01-01',
                                compiledLiteralValue: '2020-01-01',
                                expressionEvaluationType: 'Date',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '2020-01-01T10:00:00Z',
                                originalExpressionString: '2020-01-01T10:00:00Z',
                                compiledLiteralValue: '2020-01-01T10:00:00Z',
                                expressionEvaluationType: 'DateTime',
                            },
                            {
                                expressionType: 'LITERAL',
                                expressionString: '25.55',
                                originalExpressionString: '25.55',
                                compiledLiteralValue: 25.55,
                                expressionEvaluationType: 'Money',
                            },
                            complex('__dataObject__.policyNumber'),
                            complex('__dataObject__.unknownAttribute'),
                        ],
                        templateParts: [],
                        errorCode: 'code',
                    },
                    contextBuilder.buildFromRoot(dataObject),
                    session,
                )

                expect(templateVariables).toStrictEqual([
                    'string',
                    10.123,
                    true,
                    false,
                    null,
                    new Date('2020-01-01'),
                    new Date('2020-01-01T10:00:00Z'),
                    Moneys.toMoney('USD', 25.55),
                    'policyNumber',
                    undefined,
                ])
            })
        })
    })
})
