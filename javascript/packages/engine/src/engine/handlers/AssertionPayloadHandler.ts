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

import { RulePayloadHandler } from './RulePayloadHandler'
import { AssertionPayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { Payloads, Rule } from 'kraken-model'
import { ExecutionSession } from '../ExecutionSession'
import PayloadType = Payloads.PayloadType
import AssertionPayload = Payloads.Validation.AssertionPayload
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'
import { formatExpressionEvaluationMessage } from '../../utils/ExpressionEvaluationMessageFormatter'

/**
 * Payload handler implementation to process {@link AssertionPayload}s.
 */
export class AssertionPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}
    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.ASSERTION
    }
    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): AssertionPayloadResult {
        const payload = rule.payload as AssertionPayload

        logger.debug(() => formatExpressionEvaluationMessage('assertion', payload.assertionExpression, dataContext))

        const expressionResult = this.evaluator.evaluate(
            payload.assertionExpression,
            dataContext,
            session.expressionContext,
        )
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )
        if (ExpressionEvaluationResult.isSuccess(expressionResult)) {
            return payloadResultCreator.assertion(payload, Boolean(expressionResult.success), templateVariables)
        }
        return payloadResultCreator.assertionFail(expressionResult)
    }

    describePayloadResult(payloadResult: AssertionPayloadResult): string {
        if (payloadResult.error) {
            return 'Field is valid. Assertion is not evaluated due to expression error.'
        }
        return payloadResult.success
            ? 'Field is valid. Assertion evaluated to true.'
            : 'Field is not valid. Assertion evaluated to false.'
    }
}
