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

export namespace ExpressionEvaluationResult {

    /**
     * {@link ExpressionEvaluationResult} error interface.
     *
     * @export
     * @interface ExpressionEvaluationError
     * @since 1.0.41
     */
    export interface ExpressionEvaluationError {
        severity: "info" | "critical";
        message: string;
    }

    export interface SuccessResult {
        /**
         * Return value of an evaluated expression
         *
         * @type {*}
         * @memberof ExpressionEvaluationResult
         */
        success: any;
        kind: 1;

    }

    export interface ErrorResult {
        /**
         * Error ocurred in expression
         *
         * @type {ExpressionEvaluationError}
         * @memberof ExpressionEvaluationResult
         */
        error: ExpressionEvaluationError;
        kind: 2;
    }

    export type Result = SuccessResult | ErrorResult;

    export function isError(result: Result): result is ErrorResult {
        return Boolean(result.kind & 2);
    }

    export function isSuccess(result: Result): result is SuccessResult {
        return Boolean(result.kind & 1);
    }

    export function expressionSuccess(result: any | undefined): SuccessResult {
        return {
            kind: 1,
            success: result
        };
    }

    export function expressionError(error: ExpressionEvaluationError): ErrorResult {
        return {
            kind: 2,
            error
        };
    }
}
