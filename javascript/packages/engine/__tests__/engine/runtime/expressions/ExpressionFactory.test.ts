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

import { expressionFactory } from '../../../../src/engine/runtime/expressions/ExpressionFactory'

const { fromExpression, fromNavigation, fromPath } = expressionFactory

describe('ExpressionFactory', () => {
    describe('Rule Payload Expression', () => {
        it('should create LiteralExpression', () => {
            expect(
                fromExpression({
                    expressionType: 'LITERAL',
                    compiledLiteralValue: 'A',
                    compiledLiteralValueType: 'String',
                }),
            ).toMatchObject({
                type: 'LiteralExpression',
                value: 'A',
            })
        })
        it('should create ComplexExpression', () => {
            expect(
                fromExpression({
                    expressionType: 'COMPLEX',
                    expressionString: "'A' == 'B'",
                }),
            ).toMatchObject({
                type: 'ComplexExpression',
                expression: "'A' == 'B'",
            })
        })
        it('should create PathExpression for PATH type, not cross context reference', () => {
            expect(
                fromExpression({
                    expressionType: 'PATH',
                    expressionString: 'a.b',
                }),
            ).toMatchObject({
                type: 'PathExpression',
                expression: 'a.b',
            })
        })
        it('should create PropertyExpression for PATH type, not cross context reference', () => {
            expect(
                fromExpression({
                    expressionType: 'PATH',
                    expressionString: 'a',
                }),
            ).toMatchObject({
                type: 'PropertyExpression',
                expression: 'a',
            })
        })
        it('should throw on unknown expression type', () => {
            expect(() =>
                fromExpression({
                    compiledLiteralValue: '',
                    // @ts-expect-error testing negative case
                    expressionType: 'none',
                    expressionString: 'A.B',
                }),
            ).toThrow(
                'Unknown expression \'{"compiledLiteralValue":"","expressionType":"none","expressionString":"A.B"}\'',
            )
        })
    })
    describe('Context Navigation', () => {
        it('should create from Path expression', () => {
            expect(
                fromNavigation({
                    navigationExpression: {
                        expressionString: 'a.b',
                        expressionType: 'PATH',
                    },
                    targetName: 'B',
                    cardinality: 'SINGLE',
                }),
            ).toMatchObject({
                type: 'PathExpression',
                expression: 'a.b',
            })
        })
        it('should create from Path expression to Property', () => {
            expect(
                fromNavigation({
                    navigationExpression: {
                        expressionString: 'b',
                        expressionType: 'PATH',
                    },
                    targetName: 'B',
                    cardinality: 'SINGLE',
                }),
            ).toMatchObject({
                type: 'PropertyExpression',
                expression: 'b',
            })
        })
        it('should create from Complex expression', () => {
            expect(
                fromNavigation({
                    navigationExpression: {
                        expressionString: 'b.filter(x => 100 > x.limit)',
                        expressionType: 'COMPLEX',
                    },
                    cardinality: 'MULTIPLE',
                    targetName: 'B',
                }),
            ).toMatchObject({
                type: 'ComplexExpression',
                expression: 'b.filter(x => 100 > x.limit)',
            })
        })
        it('should throw on invalid type', () => {
            expect(() =>
                fromNavigation({
                    cardinality: 'SINGLE',
                    navigationExpression: {
                        expressionString: 'b.filter(x => A.limit > x.limit)',
                        // @ts-expect-error testing negative case
                        expressionType: 'LITERAL',
                        expressionVariables: [],
                    },
                    targetName: 'B',
                }),
            ).toThrow("Not supported navigation type 'LITERAL'")
        })
    })
    describe('Target path or fieldPath', () => {
        it('should create PathExpression', () => {
            expect(fromPath('a.b')).toMatchObject({
                type: 'PathExpression',
                expression: 'a.b',
            })
        })
        it('should create PropertyExpression', () => {
            expect(fromPath('a')).toMatchObject({
                type: 'PropertyExpression',
                expression: 'a',
            })
        })
    })
})
