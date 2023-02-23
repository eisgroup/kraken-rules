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
import { LengthPayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'
import { Expressions } from '../runtime/expressions/Expressions'
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
    executePayload(
        payload: LengthPayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): LengthPayloadResult {
        const path = Expressions.createPathResolver(dataContext)(rule.targetPath)
        const valueResult = this.evaluator.evaluateGet(path, dataContext.dataObject)
        if (ExpressionEvaluationResult.isError(valueResult)) {
            throw new Error(`Failed to extract attribute '${path}'`)
        }
        const value = valueResult.success
        const valueLength = typeof value === 'string' ? (value as string).length : 0
        const success = valueLength <= payload.length

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )

        logger.debug(
            () =>
                `Evaluated '${payload.type}' to ${success}. Expected length '${payload.length}'. Actual length '${valueLength}'`,
        )
        return payloadResultCreator.length(payload, success, templateVariables)
    }
}
