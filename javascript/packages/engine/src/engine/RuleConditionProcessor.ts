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

import { Condition } from 'kraken-model'
import { ConditionEvaluationResult, ConditionEvaluation, ExpressionEvaluationResult } from 'kraken-engine-api'
import { ExpressionEvaluator } from './runtime/expressions/ExpressionEvaluator'
import { ExecutionSession } from './ExecutionSession'
import { DataContext } from './contexts/data/DataContext'
import { DefaultConditionEvaluationResult } from '../dto/DefaultConditionEvaluationResult'
import { logger } from '../utils/DevelopmentLogger'
import { formatExpressionEvaluationMessage } from '../utils/ExpressionEvaluationMessageFormatter'

export class RuleConditionProcessor {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    evaluateCondition(
        dataContext: DataContext,
        session: ExecutionSession,
        condition?: Condition,
    ): ConditionEvaluationResult {
        if (!condition) {
            return DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE)
        }
        const expressionResult = logger.groupDebug(
            () => formatExpressionEvaluationMessage('condition', condition.expression, dataContext),
            () => this.evaluator.evaluate(condition.expression, dataContext, session.expressionContext),
            result =>
                ExpressionEvaluationResult.isError(result)
                    ? `Couldn't evaluate condition expression due to error.`
                    : `Evaluated condition to '${result.success}'.`,
        )

        if (ExpressionEvaluationResult.isError(expressionResult)) {
            return DefaultConditionEvaluationResult.fromError(expressionResult)
        }
        return expressionResult.success
            ? DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE)
            : DefaultConditionEvaluationResult.of(ConditionEvaluation.NOT_APPLICABLE)
    }
}
