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

import { FunctionCache } from "./FunctionCache";
import { registry } from "./ExpressionEvaluator";
import { ExpressionEvaluationResult as Result } from "./ExpressionEvaluationResult";
import { logger } from "../../../utils/DevelopmentLogger";

export interface ExpressionContext {
    dataObject: object;
    references: Record<string, object | object[] | undefined>;
    context?: Record<string, unknown>;
    setValue?: any;
}

export interface ComplexEvaluation {
    expressionContext: ExpressionContext;
    expression: string;
}

export interface EvaluationFunctions {
    name: string;
    fx: Function;
}

export class ComplexEvaluator {

    private functionNames: string[];
    private functions: Function[];
    private readonly functionCache: FunctionCache;

    constructor(functions: EvaluationFunctions[] = []) {
        this.functionNames = functions.map(f => f.name);
        this.functions = functions.map(f => f.fx);
        this.functionCache = new FunctionCache();
    }
    evaluate(evaluation: ComplexEvaluation): Result.Result {
        const expression = evaluation.expression;
        const expressionWithReturn = this.withReturn(expression);

        const declarations = [
            "__dataObject__",
            "__references__",
            "__defaultValue__",
            "context",
            ...registry.names(),
            ...this.functionNames,
            expressionWithReturn
        ];

        const variables: unknown[] = [
            evaluation.expressionContext.dataObject,
            evaluation.expressionContext.references,
            evaluation.expressionContext.setValue,
            evaluation.expressionContext.context,
            ...registry.functions(),
            ...this.functions
        ];
        try {
            const result = this.functionCache
                .compute(expression, declarations)
                .apply(undefined, variables);
            return this.validResult(result, expression);
        } catch (error) {
            if (logger.isEnabled()) {
                logger.warning(
                    `Expression evaluation failed:`,
                    `\n\texpression: ${expression}`,
                    error
                );
            }
            return Result.expressionError({
                severity: "info",
                message: `Expression: '${expression}' failed`
            });
        }
    }

    private withReturn(expression: string): string {
        return `return (${expression})`;
    }

    private validResult(result: unknown, expression: string): Result.Result {
        if (typeof result === "number" && isNaN(result)) {
            return Result.expressionError({
                message: `Expression '${expression}' result is 'NaN' (not a number)`,
                severity: "critical"
            });
        }
        if (result === +Infinity) {
            return Result.expressionError({
                // tslint:disable-next-line:max-line-length
                message: `Expression '${expression}' result is 'Infinity'. It might be that some number was divided by zero, or number is more than 1.7976931348623157e+308`,
                severity: "critical"
            });
        }
        if (result === -Infinity) {
            return Result.expressionError({
                // tslint:disable-next-line:max-line-length
                message: `Expression '${expression}' result is '-Infinity'. It might be that some number was divided by zero, or number is less than -1.7976931348623157e+308`,
                severity: "critical"
            });
        }
        return Result.expressionSuccess(result);

    }
}
