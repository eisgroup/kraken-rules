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

import { Payloads, Rule, ValueListDataType } from 'kraken-model'
import { RulePayloadHandler } from './RulePayloadHandler'
import { ExecutionSession } from '../ExecutionSession'
import { DataContext } from '../contexts/data/DataContext'
import { ValueListPayloadResult } from 'kraken-engine-api'

import PayloadType = Payloads.PayloadType
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { Moneys } from '../runtime/expressions/math/Moneys'
import ValueListPayload = Payloads.Validation.ValueListPayload
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { logger } from '../../utils/DevelopmentLogger'

/**
 * A payload handler specific to {@link ValueListPayload}. Evaluates that field value
 * is equal to at least one value provided in {@link ValueList}.
 *
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
export class ValueListPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): PayloadType {
        return PayloadType.VALUE_LIST
    }

    executePayload(rule: Rule, dataContext: DataContext, session: ExecutionSession): ValueListPayloadResult {
        const payload = rule.payload as ValueListPayload

        const value = this.evaluator.evaluateTargetField(rule.targetPath, dataContext)
        logger.debug(() => `Validating field which has value: ${ExpressionEvaluator.renderFieldValue(value)}`)
        const result = this.doExecute(value, payload)

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )
        return payloadResultCreator.valueList(payload, result, templateVariables)
    }

    describePayloadResult(payloadResult: ValueListPayloadResult): string {
        const values = payloadResult.valueList.values.map(v => v.toString()).join(', ')
        return payloadResult.success
            ? `Field is valid. Field value is one of [ ${values} ].`
            : `Field is not valid. Field value is not one of [ ${values} ].`
    }

    private doExecute(fieldValue: unknown, payload: ValueListPayload): boolean {
        if (fieldValue == undefined) {
            return true
        }

        const coercedFieldValue = this.coerce(fieldValue, payload.valueList.valueType)

        return payload.valueList.values.some(value => value === coercedFieldValue)
    }

    private coerce(value: unknown, dataType: ValueListDataType): unknown {
        switch (dataType) {
            case 'DECIMAL':
                return this.toNumber(value)
            case 'STRING':
                return this.toString(value)
            default:
                throw Error(`Unsupported data type ${dataType} encountered.`)
        }
    }

    private toString(value: unknown): string {
        if (typeof value === 'string') {
            return value
        }
        throw Error(`Unable to convert ${value} to a string.`)
    }

    private toNumber(value: unknown): number {
        if (Moneys.isMoney(value)) {
            return value.amount
        }
        if (typeof value === 'number') {
            return value as number
        }
        throw Error(`Unable to convert ${value} to a number.`)
    }
}
