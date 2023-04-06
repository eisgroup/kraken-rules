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
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { LengthPayloadResult } from 'kraken-engine-api'
import { Payloads, Rule } from 'kraken-model'
import LengthPayload = Payloads.Validation.LengthPayload
import PayloadType = Payloads.PayloadType
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'

export class LengthPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): PayloadType {
        return PayloadType.LENGTH
    }
    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): LengthPayloadResult {
        const payload = rule.payload as LengthPayload

        const value = this.evaluator.evaluateTargetField(rule.targetPath, dataContext)
        logger.debug(() => `Validating field which has value: ${ExpressionEvaluator.renderFieldValue(value)}`)
        const valueLength = typeof value === 'string' ? (value as string).length : 0
        const success = valueLength <= payload.length

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )

        return payloadResultCreator.length(payload, success, templateVariables)
    }

    describePayloadResult(payloadResult: LengthPayloadResult): string {
        return payloadResult.success
            ? `Field is valid. String length is not more than ${payloadResult.length}.`
            : `Field is not valid. String length is more than ${payloadResult.length}.`
    }
}
