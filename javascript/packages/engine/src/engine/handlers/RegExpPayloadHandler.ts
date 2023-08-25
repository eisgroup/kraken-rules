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
import { RegExpPayloadResult } from 'kraken-engine-api'

import { Payloads, Rule } from 'kraken-model'
import PayloadType = Payloads.PayloadType

import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import RegExpPayload = Payloads.Validation.RegExpPayload
import { logger } from '../../utils/DevelopmentLogger'

export class RegExpPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.REGEX
    }
    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): RegExpPayloadResult {
        const payload = rule.payload as RegExpPayload

        const value = this.evaluator.evaluateTargetField(rule.targetPath, dataContext)
        logger.debug(() => `Validating field which has value: ${ExpressionEvaluator.renderFieldValue(value)}`)
        const evaluationResult = this.valueMatchesRegExp(value, payload)

        const templateVariables = this.evaluator.evaluateTemplateVariables(payload.errorMessage, dataContext, session)
        return payloadResultCreator.regexp(payload, evaluationResult, templateVariables)
    }

    describePayloadResult(payloadResult: RegExpPayloadResult): string {
        return payloadResult.success
            ? `Field is valid. String value matches regular expression ${payloadResult.regExp}.`
            : `Field is not valid. String value does not match regular expression ${payloadResult.regExp}.`
    }

    private valueMatchesRegExp(value: unknown, payload: Payloads.Validation.RegExpPayload): boolean {
        return value === '' || value === undefined || value === null
            ? true
            : // eslint-disable-next-line @typescript-eslint/no-explicit-any
              new RegExp(payload.regExp).test((value as any).toString())
    }
}
