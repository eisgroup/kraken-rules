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
import { SizePayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'
import { Expressions } from '../runtime/expressions/Expressions'
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'

export class SizePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): PayloadType {
        return PayloadType.SIZE
    }
    executePayload(
        payload: SizePayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): SizePayloadResult {
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
            switch (payload.orientation) {
                case Orientation.MIN:
                    result = value.length >= payload.size
                    break
                case Orientation.MAX:
                    result = value.length <= payload.size
                    break
                case Orientation.EQUALS:
                    result = value.length === payload.size
                    break
                default:
                    throw new Error('Failed to find size payload orientation with value ' + payload.orientation)
            }
        }

        logger.debug(() => this.describePayloadResult(payload, result, value))

        return payloadResultCreator.size(payload, result, templateVariables)
    }

    private describePayloadResult(payload: SizePayload, result: boolean, value: unknown): string {
        const expectedSize = this.describeExpectedSize(payload)
        const actualSize = Array.isArray(value) ? value.length : ''
        return `Evaluated '${payload.type}' to ${result}. Expected size ${expectedSize} - actual size is ${actualSize}.`
    }

    private describeExpectedSize(payload: SizePayload) {
        switch (payload.orientation) {
            case Orientation.MIN:
                return `no less than ${payload.size}`
            case Orientation.MAX:
                return `no more than ${payload.size}`
            case Orientation.EQUALS:
                return `equal to ${payload.size}`
            default:
                return 'unknown'
        }
    }
}
