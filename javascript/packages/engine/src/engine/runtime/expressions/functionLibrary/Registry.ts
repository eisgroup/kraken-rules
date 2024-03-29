/* eslint-disable @typescript-eslint/ban-types */
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

import { dateFunctions } from './DateFunctions'
import { arrayFunctions } from './ArrayFunctions'
import { setFunctions } from './SetFunctions'
import { moneyFunctions } from './MoneyFunctions'
import { stringFunctions } from './StringFunctions'
import { genericValueFunctions } from './GenericValueFunctions'
import { quantifierFunctions } from './QuantifierFunctions'
import { nativeFunctions } from './NativeFunctions'
import { mathFunctions } from './MathFunctions'
import { DataObjectInfoResolver } from '../../../contexts/info/DataObjectInfoResolver'
import BigNumber from 'bignumber.js'
import { Numbers } from '../math/Numbers'
import { DateCalculator } from '../date/DateCalculator'

/**
 * Declares custom function implementation to be invocable in Kraken Expression Language.
 * Function implementation can access {@link FunctionScope} that contains utilities
 * and current evaluation session specific metadata, such as a current evaluation timezone.
 * This is useful when implementing custom logic in consistent way. See example on how to access {@link FunctionScope}.
 *
 * @see {@link FunctionScope}
 * @example
 * ``` typescript
 * registry.add({
 *   name : "myCustomMathFunction",
 *   function(first : number, second : number) : number {
 *     return this.normalize(first).multipliedBy(this.normalize(second)).toNumber();
 *   }
 * });
 * ```
 */
export interface FunctionDeclaration {
    /**
     * Unique name of Function. Function will be invokable by this name in Kraken Expression Language.
     */
    name: string

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    function: (this: FunctionScope, ...p: any[]) => any | void
}

export interface FunctionScope {
    /**
     * @param n value to normalize
     * @return number converted to BigNumber in IEE 754 64bit Decimal representation
     */
    normalize: (n: number) => BigNumber

    /**
     * A current evaluation session specific metadata
     */
    functionContext: FunctionContext
}

export type FunctionContext = {
    /**
     *  Timezone of a current evaluation session
     */
    zoneId: string
}

export type BindingConfiguration = {
    zoneId: string
    dateCalculator: DateCalculator
}

export interface InternalFunctionScope extends FunctionScope {
    dateCalculator: DateCalculator
}

export class FunctionRegistry {
    static INSTANCE = new FunctionRegistry()

    /**
     * Creates functions to be used with {@link ContextDefinition} instances.
     * These functions can be added to {@link ExpressionEvaluator}
     *
     * @static
     * @memberof FunctionRegistry
     * @see {@link ExpressionEvaluator}
     * @since 11.2
     */
    static createInstanceFunctions = _createInstanceFunctions

    private internalFunctionRegistry: Record<string, Function> = {
        ...nativeFunctions,
        ...dateFunctions,
        ...arrayFunctions,
        ...moneyFunctions,
        ...stringFunctions,
        ...setFunctions,
        ...genericValueFunctions,
        ...quantifierFunctions,
        ...mathFunctions,
    }

    private functionRegistry: Record<string, Function> = {}

    add(fx: FunctionDeclaration): void {
        if (fx.name == undefined) {
            throw new Error(errorMessage('name is required'))
        }
        if (fx.name === '') {
            throw new Error(errorMessage('name is empty'))
        }
        if (!fx.function) {
            throw new Error(errorMessage('function is required'))
        }
        if (typeof fx.function !== 'function') {
            throw new Error(errorMessage('function is type of ' + typeof fx.function))
        }
        if (fx.name.indexOf('.') > 0) {
            throw new Error(errorMessage("function name cannot contain symbol '.'"))
        }
        if (this.internalFunctionRegistry[fx.name] || this.functionRegistry[fx.name]) {
            throw new Error(errorMessage(`function with name '${fx.name}' already registered`))
        }
        this.functionRegistry[fx.name] = fx.function
    }

    /**
     * @deprecated since 1.33.0. Use {@link registeredFunctions} instead.
     */
    names(): string[] {
        return Object.keys(this.registeredFunctions())
    }

    /**
     * @deprecated since 1.33.0. Use {@link registeredFunctions} instead.
     */
    functions(): Function[] {
        return Object.values(this.registeredFunctions())
    }

    registeredFunctions(): Record<string, Function> {
        return {
            ...this.internalFunctionRegistry,
            ...this.functionRegistry,
        }
    }

    bindRegisteredFunctions(configuration: BindingConfiguration): Record<string, Function> {
        const functionContext: FunctionContext = {
            zoneId: configuration.zoneId,
        }
        const functions: Record<string, Function> = {}
        Object.keys(this.internalFunctionRegistry).forEach(
            name =>
                (functions[name] = this.internalFunctionRegistry[name].bind({
                    normalize: Numbers.normalized,
                    functionContext: functionContext,
                    dateCalculator: configuration.dateCalculator,
                } as InternalFunctionScope)),
        )
        Object.keys(this.functionRegistry).forEach(
            name =>
                (functions[name] = this.functionRegistry[name].bind({
                    normalize: Numbers.normalized,
                    functionContext: functionContext,
                } as FunctionScope)),
        )
        return functions
    }
}

function errorMessage(message: string): string {
    return `Failed to add function to registry: ${message}`
}

function _createInstanceFunctions(
    dataInfoResolver: DataObjectInfoResolver,
    getInheritance: (contextDefinitionName: string) => string[],
): FunctionDeclaration[] {
    /**
     * Using {@link DataObjectInfoResolver} to determine object type.
     *
     * @param {{}} object data object to apply rules
     * @param {string} typeName context definition name
     * @returns {boolean} is type of object same as in parameters
     */
    function typeOf(object: object, typeName: string): boolean {
        if (object == null) {
            return false
        }
        if (dataInfoResolver.validate(object).length) {
            return false
        }
        return dataInfoResolver.resolveName(object) === typeName
    }

    /**
     * Using {@link DataObjectInfoResolver} to determine object type
     * and inheritance registry to determine is object is instance of type
     * from parameters
     *
     * @param {{}} object data object to apply rules
     * @param {string} typeName context definition name
     * @returns {boolean} is type from parameters is in inheritance names or object type
     */
    function instanceOf(object: object, typeName: string): boolean {
        if (object == null) {
            return false
        }
        if (dataInfoResolver.validate(object).length) {
            return false
        }
        const objectType = GetType(object)
        if (!objectType) {
            return false
        }
        const typeInheritance = getInheritance(objectType).concat(objectType)
        return typeInheritance.includes(typeName)
    }

    /**
     * Resolves type of object using {@link DataObjectInfoResolver}.
     *
     * @param {object} [object] to resolve type
     * @returns {(string | undefined)} context definition name
     */
    function GetType(object?: object): string | undefined {
        if (object == null) {
            return undefined
        }
        if (dataInfoResolver.validate(object).length) {
            return undefined
        }
        return dataInfoResolver.resolveName(object)
    }
    return [
        {
            name: '_t',
            function: typeOf,
        },
        {
            name: '_i',
            function: instanceOf,
        },
        {
            name: GetType.name,
            function: GetType,
        },
    ]
}

export const functionsRegistry = new FunctionRegistry()
