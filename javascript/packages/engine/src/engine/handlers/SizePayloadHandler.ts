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
import { expressionFactory } from '../runtime/expressions/ExpressionFactory'
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'

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
        const expression = expressionFactory.fromPath(Expressions.createPathResolver(dataContext)(rule.targetPath))
        const exResult = this.evaluator.evaluate(expression, dataContext)
        if (ExpressionEvaluationResult.isError(exResult)) {
            throw new Error(`Failed to extract attribute ${expression}`)
        }
        let target = exResult.success
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )
        const result = (success: boolean) => payloadResultCreator.size(payload, success, templateVariables)

        if (target == undefined) {
            target = []
        }
        if (Array.isArray(target)) {
            switch (payload.orientation) {
                case Orientation.MIN:
                    return result(target.length >= payload.size)
                case Orientation.MAX:
                    return result(target.length <= payload.size)
                case Orientation.EQUALS:
                    return result(target.length === payload.size)
                default:
                    throw new Error('Failed to find size payload orientation with value ' + payload.orientation)
            }
        }
        return result(true)
    }
}
