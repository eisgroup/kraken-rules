/* eslint-disable @typescript-eslint/ban-types */
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

import { FunctionCache } from './FunctionCache'
import { ExpressionEvaluationResult as Result } from 'kraken-engine-api'
import { logger } from '../../../utils/DevelopmentLogger'
import { KelFunction } from './ExpressionEvaluator'
import { Expressions } from 'kraken-model'

export interface ExpressionContext {
    dataObject: object
    references: Record<string, object | object[] | undefined>
    context?: Record<string, unknown>
}

export interface ComplexEvaluation {
    expressionContext: ExpressionContext
    expression: Expressions.ComplexExpression
}

export interface EvaluationFunctions {
    name: string
    fx: Function
}

export class ComplexEvaluator {
    private readonly initialFunctions: Record<string, Function>
    private readonly functionInvoker: Record<string, Function>
    private readonly expressionCache: FunctionCache

    constructor(jsFunctions: EvaluationFunctions[] = [], kelFunctions: KelFunction[] = []) {
        this.functionInvoker = {}
        this.initialFunctions = {}
        for (const f of jsFunctions) {
            this.initialFunctions[f.name] = f.fx
        }
        for (const f of kelFunctions) {
            this.initialFunctions[f.name] = Function.apply(this.functionInvoker, [
                ...f.parameters.map(p => p.name),
                `return (${f.body})`,
            ])
        }
        this.rebuildFunctionInvoker({})
        this.expressionCache = new FunctionCache()
    }

    evaluate(evaluation: ComplexEvaluation): Result.Result {
        const expression = evaluation.expression
        const declarations = ['__dataObject__', '__references__', 'context', `return (${expression.expressionString})`]

        const variables: unknown[] = [
            evaluation.expressionContext.dataObject,
            evaluation.expressionContext.references,
            evaluation.expressionContext.context,
        ]
        try {
            const result = this.expressionCache
                .compute(expression.expressionString, declarations)
                .apply(this.functionInvoker, variables)
            return this.validResult(result, expression.originalExpressionString)
        } catch (error) {
            logger.debug(() => [`Error while evaluating expression: ${expression.originalExpressionString}`, error])
            return Result.expressionError({
                severity: 'info',
                message: `Error while evaluating expression: ${expression.originalExpressionString}`,
            })
        }
    }

    rebuildFunctionInvoker(functions: Record<string, Function>): void {
        Object.assign(this.functionInvoker, functions)
        Object.assign(this.functionInvoker, this.initialFunctions)
    }

    private validResult(result: unknown, expression: string): Result.Result {
        if (typeof result === 'number' && isNaN(result)) {
            return Result.expressionError({
                message: `Expression '${expression}' result is 'NaN' (not a number)`,
                severity: 'critical',
            })
        }
        if (result === +Infinity) {
            return Result.expressionError({
                message: `Expression '${expression}' result is 'Infinity'. It might be that some number was divided by zero, or number is more than 1.7976931348623157e+308`,
                severity: 'critical',
            })
        }
        if (result === -Infinity) {
            return Result.expressionError({
                message: `Expression '${expression}' result is '-Infinity'. It might be that some number was divided by zero, or number is less than -1.7976931348623157e+308`,
                severity: 'critical',
            })
        }
        return Result.expressionSuccess(result)
    }
}
