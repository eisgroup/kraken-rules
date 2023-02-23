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
import { RegExpPayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'

import { Expressions } from '../runtime/expressions/Expressions'

import { Payloads, Rule } from 'kraken-model'
import PayloadType = Payloads.PayloadType

import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function isSuccess(value: any, payload: Payloads.Validation.RegExpPayload): boolean {
    return value === '' || value === undefined || value === null
        ? true
        : new RegExp(payload.regExp).test(value.toString())
}

export class RegExpPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.REGEX
    }
    executePayload(
        payload: Payloads.Validation.RegExpPayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): RegExpPayloadResult {
        const path = Expressions.createPathResolver(dataContext)(rule.targetPath)
        const valueResult = this.evaluator.evaluateGet(path, dataContext.dataObject)
        if (ExpressionEvaluationResult.isError(valueResult)) {
            throw new Error(`Failed to extract attribute '${path}'`)
        }
        const value = valueResult.success
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )
        const evaluationResult = isSuccess(value, payload)

        logger.debug(() => this.describePayloadResult(payload, evaluationResult, value))

        return payloadResultCreator.regexp(payload, evaluationResult, templateVariables)
    }

    private describePayloadResult(payload: Payloads.Validation.RegExpPayload, result: boolean, value: unknown): string {
        const explanationStr = result ? 'matches' : 'does not match'
        return `Evaluated '${payload.type}' to ${result}. Value '${ExpressionEvaluator.render(
            value,
        )}' ${explanationStr} regular expression '${payload.regExp}'.`
    }
}
