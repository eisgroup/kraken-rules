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
import { NumberSetPayloadResult } from 'kraken-engine-api'
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
    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): NumberSetPayloadResult {
        const payload = rule.payload as NumberSetPayload

        let value = this.evaluator.evaluateTargetField(rule.targetPath, dataContext)
        if (Moneys.isMoney(value)) {
            value = value.amount
        }

        let success = true
        if (typeof value === 'number') {
            logger.debug(() => `Validating field which has value: ${ExpressionEvaluator.renderFieldValue(value)}`)
            success = Numbers.isValueInNumberSet(value, payload.min, payload.max, payload.step)
        }

        const templateVariables = this.evaluator.evaluateTemplateVariables(payload.errorMessage, dataContext, session)

        return payloadResultCreator.numberSet(payload, success, templateVariables)
    }

    describePayloadResult(payloadResult: NumberSetPayloadResult): string {
        const min = payloadResult.min !== undefined ? payloadResult.min : '-∞'
        const max = payloadResult.max !== undefined ? payloadResult.max : '∞'
        const stepMsg = payloadResult.step !== undefined ? ` with step ${payloadResult.step}` : ''
        const numberSetDescription = `number set [${min}, ${max}]${stepMsg}`
        return payloadResult.success
            ? `Field is valid. Field value is in ${numberSetDescription}.`
            : `Field is not valid. Field value is not in ${numberSetDescription}.`
    }
}
