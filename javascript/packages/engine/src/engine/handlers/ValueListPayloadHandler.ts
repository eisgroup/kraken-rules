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
import { ValueListPayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'

import PayloadType = Payloads.PayloadType
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { Expressions } from '../runtime/expressions/Expressions'
import { expressionFactory } from '../runtime/expressions/ExpressionFactory'
import { Moneys } from '../runtime/expressions/math/Moneys'
import ValueListPayload = Payloads.Validation.ValueListPayload
import { payloadResultCreator } from '../results/PayloadResultCreator'

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

    executePayload(
        payload: Payloads.Validation.ValueListPayload,
        rule: Rule,
        dataContext: DataContext,
        session: ExecutionSession,
    ): ValueListPayloadResult {
        const value = this.resolveFieldValue(rule, dataContext)
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext,
        )

        return payloadResultCreator.valueList(payload, this.doExecute(value, payload), templateVariables)
    }

    private doExecute(fieldValue: unknown, payload: ValueListPayload): boolean {
        if (fieldValue == undefined) {
            return true
        }

        const coercedFieldValue = this.coerce(fieldValue, payload.valueList.valueType)

        return payload.valueList.values.some(value => value === coercedFieldValue)
    }

    private resolveFieldValue(rule: Rule, dataContext: DataContext): unknown {
        const expression = expressionFactory.fromPath(Expressions.createPathResolver(dataContext)(rule.targetPath))
        const result = this.evaluator.evaluate(expression, dataContext)
        if (ExpressionEvaluationResult.isError(result)) {
            throw new Error(`Failed to resolver attribute value for path ${rule.targetPath}`)
        }
        return result.success
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
