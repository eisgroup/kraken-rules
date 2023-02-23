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

import { Payloads, Rule } from 'kraken-model'
import PayloadType = Payloads.PayloadType
import SizeRangePayload = Payloads.Validation.SizeRangePayload
import { RulePayloadHandler } from './RulePayloadHandler'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { SizeRangePayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'
import { Expressions } from '../runtime/expressions/Expressions'
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'

export class SizeRangePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): PayloadType {
        return PayloadType.SIZE_RANGE
    }
    executePayload(
        payload: SizeRangePayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): SizeRangePayloadResult {
        const path = Expressions.createPathResolver(dataContext)(rule.targetPath)
        const valueResult = this.evaluator.evaluateGet(path, dataContext.dataObject)
        if (ExpressionEvaluationResult.isError(valueResult)) {
            throw new Error(`Failed to extract attribute '${path}'`)
        }
        let value = valueResult.success
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )

        if (value == undefined) {
            value = []
        }

        let result = true
        if (Array.isArray(value)) {
            result = value.length >= payload.min && value.length <= payload.max
        }

        logger.debug(() => this.describePayloadResult(payload, result, value))

        return payloadResultCreator.sizeRange(payload, result, templateVariables)
    }

    private describePayloadResult(payload: SizeRangePayload, result: boolean, value: unknown): string {
        const actualSize = Array.isArray(value) ? value.length : ''
        const resultDescription = result
            ? `Collection field size is within expected range.`
            : `Expected size within ${payload.min} and ${payload.max}. Actual size is ${actualSize}.`
        return `Evaluated '${payload.type}' to ${result}. ${resultDescription}.`
    }
}
