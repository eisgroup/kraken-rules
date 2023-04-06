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
import SizePayload = Payloads.Validation.SizePayload
import Orientation = Payloads.Validation.SizeOrientation
import { RulePayloadHandler } from './RulePayloadHandler'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { SizePayloadResult } from 'kraken-engine-api'
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'

export class SizePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): PayloadType {
        return PayloadType.SIZE
    }
    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): SizePayloadResult {
        const payload = rule.payload as SizePayload

        let value = this.evaluator.evaluateTargetField(rule.targetPath, dataContext)
        if (value == undefined) {
            value = []
        }

        let result = true
        if (Array.isArray(value)) {
            const size = value.length
            logger.debug(() => `Validating collection field which has ${size} element(s).`)
            switch (payload.orientation) {
                case Orientation.MIN:
                    result = size >= payload.size
                    break
                case Orientation.MAX:
                    result = size <= payload.size
                    break
                case Orientation.EQUALS:
                    result = size === payload.size
                    break
                default:
                    throw new Error('Failed to find size payload orientation with value ' + payload.orientation)
            }
        }

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )
        return payloadResultCreator.size(payload, result, templateVariables)
    }

    describePayloadResult(payloadResult: SizePayloadResult): string {
        const size = this.describeExpectedSize(payloadResult)

        return payloadResult.success
            ? `Field is valid. Collection size is ${size}.`
            : `Field is not valid. Collection size is not ${size}.`
    }

    private describeExpectedSize(payloadResult: SizePayloadResult) {
        switch (payloadResult.sizeOrientation) {
            case Orientation.MIN:
                return `no less than ${payloadResult.min}`
            case Orientation.MAX:
                return `no more than ${payloadResult.min}`
            case Orientation.EQUALS:
                return `equal to ${payloadResult.min}`
            default:
                return 'unknown'
        }
    }
}
