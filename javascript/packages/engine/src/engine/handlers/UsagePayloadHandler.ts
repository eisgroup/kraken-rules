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
import { UsagePayloadResult } from 'kraken-engine-api'

import { Payloads, Rule } from 'kraken-model'

import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import PayloadType = Payloads.PayloadType
import UsagePayload = Payloads.Validation.UsagePayload
import UsageType = Payloads.Validation.UsageType
import { logger } from '../../utils/DevelopmentLogger'

function isEmpty(value: unknown): boolean {
    return value === '' || value == null
}

export class UsagePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.USAGE
    }
    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): UsagePayloadResult {
        const payload = rule.payload as UsagePayload

        const value = this.evaluator.evaluateTargetField(rule.targetPath, dataContext)
        logger.debug(() => `Validating field which has value: ${ExpressionEvaluator.renderFieldValue(value)}`)
        let result = true
        if (Payloads.Validation.UsageType.mandatory === payload.usageType) {
            result = !isEmpty(value)
        } else if (Payloads.Validation.UsageType.mustBeEmpty === payload.usageType) {
            result = isEmpty(value)
        }

        const templateVariables = this.evaluator.evaluateTemplateVariables(payload.errorMessage, dataContext, session)

        return payloadResultCreator.usage(payload, result, templateVariables)
    }

    describePayloadResult(payloadResult: UsagePayloadResult): string {
        switch (payloadResult.usageType) {
            case UsageType.mandatory:
                return payloadResult.success
                    ? `Field is valid. Field is mandatory and it has a value.`
                    : `Field is not valid. Field is mandatory but it has no value.`
            case UsageType.mustBeEmpty:
                return payloadResult.success
                    ? `Field is valid. Field must be empty and it has no value.`
                    : `Field is not valid. Field must be empty but it has a value.`
            default:
                throw new Error(`Unsupported usage type: ${payloadResult.usageType}`)
        }
    }
}
