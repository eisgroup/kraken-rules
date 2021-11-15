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

import { Expressions, Contexts } from "kraken-model";
import { ErrorCode, KrakenRuntimeError } from "../../../error/KrakenRuntimeError";
import { RuntimeExpression } from "./RuntimeExpression";

function fromExpression(expression: Expressions.Expression): RuntimeExpression {
    switch (expression.expressionType) {
        case "COMPLEX":
            return {
                type: "ComplexExpression",
                expression: expression.expressionString
            };
        case "LITERAL":
            return {
                type: "LiteralExpression",
                value: expression.compiledLiteralValue,
                valueType: expression.compiledLiteralValueType
            };
        case "PATH":
            return fromPath(expression.expressionString);
        default:
            throw new KrakenRuntimeError(
                ErrorCode.UNKNOWN_EXPRESSION_TYPE,
                `Unknown expression '${JSON.stringify(expression)}'`
            );
    }
}

function fromNavigation(navigation: Contexts.ContextNavigation): RuntimeExpression {
    const type = navigation.navigationExpression.expressionType;
    if (type === "COMPLEX") {
        return {
            expression: navigation.navigationExpression.expressionString,
            type: "ComplexExpression"
        };
    }
    if (type === "PATH") {
        return fromPath(navigation.navigationExpression.expressionString);
    }
    throw new KrakenRuntimeError(
        ErrorCode.UNKNOWN_NAVIGATION_TYPE,
        `Not supported navigation type '${type}'`
    );
}

function fromPath(path: string): RuntimeExpression {
    if (path.indexOf(".") === -1) {
        return {
            type: "PropertyExpression",
            expression: path
        };
    } else {
        return {
            type: "PathExpression",
            expression: path
        };
    }
}

export const expressionFactory = {
    fromExpression, fromNavigation, fromPath
};
