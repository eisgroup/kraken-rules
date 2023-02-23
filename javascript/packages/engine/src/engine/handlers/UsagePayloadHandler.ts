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
import { ExpressionEvaluationResult, UsagePayloadResult } from 'kraken-engine-api'

import { Expressions } from '../runtime/expressions/Expressions'

import { Payloads, Rule } from 'kraken-model'

import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'
import PayloadType = Payloads.PayloadType
import UsagePayload = Payloads.Validation.UsagePayload
import UsageType = Payloads.Validation.UsageType

function isEmpty(value: unknown): boolean {
    return value === '' || value == null
}

export class UsagePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.USAGE
    }
    executePayload(
        payload: UsagePayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): UsagePayloadResult {
        const path = Expressions.createPathResolver(dataContext)(rule.targetPath)
        const valueResult = this.evaluator.evaluateGet(path, dataContext.dataObject)
        if (ExpressionEvaluationResult.isError(valueResult)) {
            throw new Error(`Failed to extract attribute '${path}'`)
        }
        const value = valueResult.success

        let result = true
        if (Payloads.Validation.UsageType.mandatory === payload.usageType) {
            result = !isEmpty(value)
        } else if (Payloads.Validation.UsageType.mustBeEmpty === payload.usageType) {
            result = isEmpty(value)
        }

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )

        logger.debug(() => this.describePayloadResult(payload, result, value))

        return payloadResultCreator.usage(payload, result, templateVariables)
    }

    private describePayloadResult(payload: UsagePayload, result: boolean, value: unknown): string {
        const resultDescription = this.describePayloadResultType(payload, result, value)
        return `Evaluated '${payload.type}' to ${result}. ${resultDescription}.`
    }

    private describePayloadResultType(payload: UsagePayload, result: boolean, value: unknown): string {
        switch (payload.usageType) {
            case UsageType.mandatory:
                return result
                    ? `Field is mandatory and it has value '${ExpressionEvaluator.render(value)}'`
                    : `Field is mandatory but it has no value`
            case UsageType.mustBeEmpty:
                return result
                    ? `Field must be empty and it has no value`
                    : `Field must be empty but it has value '${ExpressionEvaluator.render(value)}'`
            default:
                return ''
        }
    }
}
