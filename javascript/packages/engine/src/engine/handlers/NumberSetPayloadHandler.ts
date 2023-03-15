/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
import { RulePayloadHandler } from './RulePayloadHandler'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { NumberSetPayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'
import { Expressions } from '../runtime/expressions/Expressions'
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import NumberSetPayload = Payloads.Validation.NumberSetPayload
import { Numbers } from '../runtime/expressions/math/Numbers'
import { Moneys } from '../runtime/expressions/math/Moneys'
import { logger } from '../../utils/DevelopmentLogger'

export class NumberSetPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): PayloadType {
        return PayloadType.NUMBER_SET
    }
    executePayload(
        payload: NumberSetPayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): NumberSetPayloadResult {
        const path = Expressions.createPathResolver(dataContext)(rule.targetPath)
        const valueResult = this.evaluator.evaluateGet(path, dataContext.dataObject)
        if (ExpressionEvaluationResult.isError(valueResult)) {
            throw new Error(`Failed to extract attribute '${path}'`)
        }

        let value = valueResult.success
        let success = true
        if (Moneys.isMoney(value)) {
            value = value.amount
        }
        if (typeof value === 'number') {
            const numberValue = value as number
            success = Numbers.isValueInNumberSet(numberValue, payload.min, payload.max, payload.step)
        }

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )

        logger.debug(() => this.describePayloadResult(payload, success, value))

        return payloadResultCreator.numberSet(payload, success, templateVariables)
    }

    private describePayloadResult(payload: NumberSetPayload, result: boolean, value: unknown): string {
        const inSet = result ? '∈' : '∉'
        const min = payload.min !== undefined ? payload.min : '-∞'
        const max = payload.max !== undefined ? payload.max : '∞'
        const stepMsg = payload.step !== undefined ? ` with step ${payload.step}` : ''
        return `Evaluated '${payload.type}' to ${result}. ${value} ${inSet} [${min}, ${max}]${stepMsg}.`
    }
}
