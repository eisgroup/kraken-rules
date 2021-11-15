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

import { Expressions } from "kraken-model";

/**
 * Expression object generic type used for runtime expression evaluation.
 */
export type RuntimeExpression =
    PropertyExpression | LiteralExpression | ComplexExpression | PathExpression;

/**
 * Expression type that must be used to access property directly as computed property.
 * @example
 * let expression = "a";
 * let data = { a: 1 }
 */
export interface PropertyExpression {
    type: "PropertyExpression";
    expression: string;
}

/**
 * Expression type that must be used to access properties in object.
 * @example
 *  let expression = "a.b";
 *  let data = { a: { b: 1 } }
 */
export interface PathExpression {
    type: "PathExpression";
    expression: string;
}

/**
 * Expression type that already contains value.
 * Value can be type of {@code number} | {@code string} | {@code null} | {@code undefined}
 */
export interface LiteralExpression {
    type: "LiteralExpression";
    value: Expressions.LiteralExpression["compiledLiteralValue"];

    /**
     * Indicates type of a literal. Can be: String, Number, Boolean, Date, DateTime.
     * If value is not available then literal is null value.
     */
    valueType? : Expressions.LiteralExpression["compiledLiteralValueType"];
}

/**
 * Expression type that contains complex expression, that has access to all
 * scopes, context and cross context references, functions from function
 * library.
 * @example
 * let expression = "context.isNew ? Today() : Insured.birthDate"
 */
export interface ComplexExpression {
    type: "ComplexExpression";
    expression: string;
}
