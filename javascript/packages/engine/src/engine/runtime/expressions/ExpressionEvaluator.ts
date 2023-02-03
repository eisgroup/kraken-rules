/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
import { Reducer } from 'declarative-js'

import { FunctionRegistry, FunctionDeclaration } from './functionLibrary/Registry'
import { RuntimeExpression } from './RuntimeExpression'
import { pathAccessor } from './PathAccessor'
import { ComplexEvaluator } from './ComplexEvaluator'
import { ExpressionEvaluationResult, ExpressionEvaluationResult as Expression } from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import ErrorMessage = Payloads.Validation.ErrorMessage
import { expressionFactory } from './ExpressionFactory'
import { Numbers } from './math/Numbers'
import { dateFunctions } from './functionLibrary/DateFunctions'
import { Moneys } from './math/Moneys'
import { DataContext } from '../../contexts/data/DataContext'
import { ErrorCode, KrakenRuntimeError } from '../../../error/KrakenRuntimeError'

/**
 * Global expression functions registry.
 * It is exposed to add functions to the registry.
 *
 * @see {@link FunctionDeclaration}
 * @example
 * ``` typescript
 * registry.add({
 *   name : "WithUSDPrefix",
 *   function(s : string) : string {
 *     return s + "USD;
 *   }
 * });
 * ```
 */
export const registry = FunctionRegistry.INSTANCE

/**
 * Represents a single KEL function which may be invoked from kraken rule expressions.
 * Usually KelFunction instance should not be created manually,
 * but rather obtained from KEL Function defined in Kraken DSL and then generated to javascript
 * by using maven plugin tools provided for this purpose.
 */
export interface KelFunction {
    /**
     * Name of the function by which it may be invoked in kraken rule expression
     */
    name: string
    /**
     * a list of parameters that function uses
     */
    parameters: FunctionParameter[]
    /**
     * A body which contains function logic implemented in javascript language.
     * Body must not have a return statement. A final javascript code is obtained by 'return (<body>)'
     */
    body: string
}

/**
 * Represents a single parameter of {@link KelFunction}
 */
export interface FunctionParameter {
    name: string
}

export class ExpressionEvaluator {
    /**
     * This evaluator will not have functions defined in runtime,
     * after creating engine instance. It will have all functions defined
     * in function registry, before creating engine instance.
     *
     * @static
     * @memberof ExpressionEvaluator
     *
     * @since 11.2
     */
    static DEFAULT = new ExpressionEvaluator()

    readonly #evaluator: ComplexEvaluator

    /**
     * Ability to add function, to be accessible in expression evaluation context.
     * These functions will be appended to existing functions from {@link FunctionRegistry}.
     *
     * {@link FunctionRegistry#createInstanceFunctions} construct additional functions
     *
     * @param functions to add in expression evaluation context.
     * @param kelFunctions to add to expression evaluation context
     * @see FunctionRegistry#createInstanceFunctions
     * @since 11.2
     */
    constructor(functions: FunctionDeclaration[] = [], kelFunctions: KelFunction[] = []) {
        const evaluationFunctions = functions
            .map(f => f.name)
            .reduce(Reducer.zip(functions.map(f => f.function)), [])
            .map(fd => ({ name: fd[0], fx: fd[1] }))
        this.#evaluator = new ComplexEvaluator(evaluationFunctions, kelFunctions)
    }

    /**
     * Evaluates {@Link RuntimeExpression} on {@Link DataContext} and context object.
     *
     * @param expression    to evaluate. IT can be constructed with global object "expressionFactory"
     * @param dataContext   contains data object and external references, that can
     *                      be accessed in expression string. External references can be
     *                      accessed from ComplexExpressions.
     * @param context       optional parameter, that provides external context. Can be
     *                      Accessed from ComplexExpressions.
     * @returns result of expression.
     * @throws  if expression is not {@Link RuntimeExpression}
     * @throws  error on invalid complex expression. The cause can be invalid expression context,
     *          invalid complex expression or invalid complex expression result for {@code NaN},
     *          {@code -Infinity} or {@code +Infinity}
     */
    evaluate(
        expression: RuntimeExpression,
        dataContext: DataContext,
        context?: Record<string, unknown>,
    ): Expression.Result {
        switch (expression.type) {
            case 'PropertyExpression':
                return Expression.expressionSuccess(dataContext.dataObject[expression.expression])
            case 'PathExpression':
                return Expression.expressionSuccess(pathAccessor.access(dataContext.dataObject, expression.expression))
            case 'LiteralExpression':
                if (expression.valueType === 'Date' || expression.valueType === 'DateTime') {
                    return Expression.expressionSuccess(new Date(expression.value as string))
                }
                return Expression.expressionSuccess(expression.value)
            case 'ComplexExpression':
                return this.#evaluator.evaluate({
                    expressionContext: {
                        dataObject: dataContext.dataObject,
                        references: dataContext.externalReferenceObjects.references,
                        context,
                    },
                    expression: expression.expression,
                })
            default:
                throw new KrakenRuntimeError(
                    ErrorCode.UNKNOWN_EXPRESSION_TYPE,
                    `Evaluation of expression ${JSON.stringify(expression)} is not supported`,
                )
        }
    }
    /**
     * Set value to object at path.
     *
     * @param path    path to set value at
     * @param dataObject    object to modify and set value.
     * @param value         value to set in object
     * @returns value that was passed in parameter to set. If {@code undefined} is
     *          returned, value was not set or value to set was {@code undefined}.
     * @throws  Error when LiteralExpression or other expression
     *          that is not supported is used as expression parameter
     * @throws  error on invalid complex expression. The cause can be invalid expression context,
     *          invalid complex expression or invalid complex expression result for {@code NaN},
     *          {@code -Infinity} or {@code +Infinity}
     */
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    evaluateSet(path: string, dataObject: any, value: unknown): Expression.Result {
        return Expression.expressionSuccess(pathAccessor.accessAndSet(dataObject, path, value))
    }

    /**
     * Evaluates and formats template expressions from {@link ErrorMessage}.
     * Variables are returned in the order they appear in validation message template.
     *
     * @param message       message that may contain template expressions which would be evaluated and formatted
     * @param dataContext   contains data object and external references, that can
     *                      be accessed in expression string. External references can be
     *                      accessed from ComplexExpressions.
     * @param context       optional parameter, that provides external context. Can be
     *                      Accessed from ComplexExpression.
     * @returns a list of formatted values obtained by evaluating message template expressions
     */
    evaluateTemplateVariables(
        message: ErrorMessage | undefined,
        dataContext: DataContext,
        context?: Record<string, unknown>,
    ): string[] {
        return message
            ? message.templateExpressions
                  .map(p => this.evaluate(expressionFactory.fromExpression(p), dataContext, context))
                  .map(result => (ExpressionEvaluationResult.isError(result) ? undefined : result.success))
                  .map(ExpressionEvaluator.render)
            : []
    }

    /**
     * Rebuilds registered functions in the evaluator.
     */
    rebuildFunctions(): void {
        this.#evaluator.rebuildFunctionInvokerIfNeeded()
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    static render(value: any | undefined): string {
        if (value == undefined) {
            return ''
        }
        if (typeof value === 'number') {
            return Numbers.toString(value)
        }
        if (value instanceof Date) {
            return dateFunctions.Format(value, 'YYYY-MM-DD hh:mm:ss')
        }
        if (Moneys.isMoney(value)) {
            return ExpressionEvaluator.render(value.amount)
        }
        if (Array.isArray(value)) {
            return '[' + value.map(ExpressionEvaluator.render).join(', ') + ']'
        }
        return value.toString()
    }
}
