/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

export namespace Expressions {
    /**
     * Models expression to be used in rules model
     */
    export interface BaseExpression {
        expressionType: ExpressionType
        expressionString: string
        originalExpressionString: string
        expressionEvaluationType?: string
    }

    export interface ComplexExpression extends BaseExpression {
        expressionType: 'COMPLEX'
        expressionVariables?: ExpressionVariable[]
    }

    export interface ExpressionVariable {
        name: string
        type: ExpressionVariableType
    }

    export type ExpressionVariableType = 'CROSS_CONTEXT'

    export interface LiteralExpression extends BaseExpression {
        expressionType: 'LITERAL'
        compiledLiteralValue: number | string | boolean | null
    }

    export interface PathExpression extends BaseExpression {
        expressionType: 'PATH'
    }

    export type ExpressionType =
        /**
         * Indicates that expression is a simple path that consists of one or more identifiers separated by dot
         */
        | 'PATH'
        /**
         * Indicates that expression is more complex that any other simple types or that type cannot be determined
         */
        | 'COMPLEX'
        /**
         * Indicates that expression was a simple literal, like string, number or boolean
         */
        | 'LITERAL'

    export type Expression = LiteralExpression | ComplexExpression | PathExpression

    export function isPath(e: Expression): e is PathExpression {
        return e.expressionType === 'PATH'
    }
    export function isComplex(e: Expression): e is ComplexExpression {
        return e.expressionType === 'COMPLEX'
    }
    export function isLiteral(e: Expression): e is LiteralExpression {
        return e.expressionType === 'LITERAL'
    }
}
