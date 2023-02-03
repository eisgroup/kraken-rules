/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import { ConditionEvaluation, ConditionEvaluationResult, ExpressionEvaluationResult } from 'kraken-engine-api'

export class DefaultConditionEvaluationResult implements ConditionEvaluationResult {
    private constructor(
        public readonly conditionEvaluation: ConditionEvaluation,
        public readonly error: ExpressionEvaluationResult.ErrorResult | undefined,
    ) {}

    static fromError = (error: ExpressionEvaluationResult.ErrorResult) => {
        return new DefaultConditionEvaluationResult(ConditionEvaluation.ERROR, error)
    }

    static of = (evaluation: ConditionEvaluation) => new DefaultConditionEvaluationResult(evaluation, undefined)
}

export const conditionEvaluationTypeChecker = {
    isApplicable(result: ConditionEvaluationResult): boolean {
        return result.conditionEvaluation === ConditionEvaluation.APPLICABLE
    },
}
