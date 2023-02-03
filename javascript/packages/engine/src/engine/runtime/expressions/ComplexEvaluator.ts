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
import { KelFunction, registry } from './ExpressionEvaluator'

export interface ExpressionContext {
    dataObject: object
    references: Record<string, object | object[] | undefined>
    context?: Record<string, unknown>
}

export interface ComplexEvaluation {
    expressionContext: ExpressionContext
    expression: string
}

export interface EvaluationFunctions {
    name: string
    fx: Function
}

export class ComplexEvaluator {
    private readonly initialFunctions: Record<string, Function>

    private readonly functionInvoker: Record<string, Function>
    private readonly functionCache: FunctionCache

    private functionRegistrySize = 0

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

        this.rebuildFunctionInvokerIfNeeded()

        this.functionCache = new FunctionCache()
    }

    evaluate(evaluation: ComplexEvaluation): Result.Result {
        const expression = evaluation.expression

        const declarations = ['__dataObject__', '__references__', 'context', `return (${expression})`]

        const variables: unknown[] = [
            evaluation.expressionContext.dataObject,
            evaluation.expressionContext.references,
            evaluation.expressionContext.context,
        ]
        try {
            const result = this.functionCache.compute(expression, declarations).apply(this.functionInvoker, variables)
            return this.validResult(result, expression)
        } catch (error) {
            logger.warning(`Expression evaluation failed:`, `\n\texpression: ${expression}`, error)
            return Result.expressionError({
                severity: 'info',
                message: `Expression: '${expression}' failed`,
            })
        }
    }

    rebuildFunctionInvokerIfNeeded(): void {
        const registeredFunctions = registry.registeredFunctions()
        const currentFunctionRegistrySize = Object.keys(registeredFunctions).length
        if (this.functionRegistrySize < currentFunctionRegistrySize) {
            Object.assign(this.functionInvoker, registeredFunctions)
            Object.assign(this.functionInvoker, this.initialFunctions)
            this.functionRegistrySize = currentFunctionRegistrySize
        }
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
