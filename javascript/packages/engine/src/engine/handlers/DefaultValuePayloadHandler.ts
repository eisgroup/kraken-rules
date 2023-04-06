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
import { DefaultValuePayloadResult, ExpressionEvaluationResult } from 'kraken-engine-api'
import { Contexts, Payloads, Rule } from 'kraken-model'

import { DataContext } from '../contexts/data/DataContext'
import { ExecutionSession } from '../ExecutionSession'
import { payloadResultCreator } from '../results/PayloadResultCreator'
import { ValueChangedEvent } from '../results/ValueChangedEvent'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { TargetPathUtils } from '../runtime/expressions/TargetPathUtils'
import { Moneys } from '../runtime/expressions/math/Moneys'
import { RulePayloadHandler } from './RulePayloadHandler'
import { logger } from '../../utils/DevelopmentLogger'

import DefaultingType = Payloads.Derive.DefaultingType
import DefaultValuePayload = Payloads.Derive.DefaultValuePayload
import PayloadType = Payloads.PayloadType
import ContextField = Contexts.ContextField
import { formatExpressionEvaluationMessage } from '../../utils/ExpressionEvaluationMessageFormatter'

function isDefaultType(payload: DefaultValuePayload): boolean {
    return payload.defaultingType === DefaultingType.defaultValue
}

function isResetType(payload: DefaultValuePayload): boolean {
    return payload.defaultingType === DefaultingType.resetValue
}

function toMoney(currency: string, amount: number): Contexts.MoneyType | undefined {
    if (amount == undefined) {
        return undefined
    }
    return {
        amount: amount,
        currency: currency,
    }
}

/**
 * Payload handler implementation to process {@link DefaultValuePayload}s
 */
export class DefaultValuePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) {}

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.DEFAULT
    }
    executePayload(rule: Rule, dataCtx: DataContext, session: ExecutionSession): DefaultValuePayloadResult {
        const payload = rule.payload as DefaultValuePayload
        const value = this.evaluator.evaluateTargetField(rule.targetPath, dataCtx)

        const targetPath = TargetPathUtils.resolveTargetPath(rule.targetPath, dataCtx)

        logger.debug(() => formatExpressionEvaluationMessage('default value', payload.valueExpression, dataCtx))
        const expressionResult = this.evaluator.evaluate(payload.valueExpression, dataCtx, session.expressionContext)
        if (ExpressionEvaluationResult.isError(expressionResult)) {
            return payloadResultCreator.defaultFail(expressionResult)
        }
        const coercionResult = this.coerce(expressionResult.success, rule, dataCtx, session)
        if (ExpressionEvaluationResult.isError(coercionResult)) {
            return payloadResultCreator.defaultFail(coercionResult)
        }

        const defaultValue = coercionResult.success

        // apply default value on field

        let updatedValue = value
        const isDefault = isDefaultType(payload) && this.isEmptyValue(value)
        const isReset = isResetType(payload)
        if (isDefault || isReset) {
            const updateValueResult = this.evaluator.evaluateSet(targetPath, dataCtx.dataObject, defaultValue)
            if (ExpressionEvaluationResult.isError(updateValueResult)) {
                return payloadResultCreator.defaultFail(updateValueResult)
            }
            updatedValue = updateValueResult.success
        }

        if (this.areValuesEqual(updatedValue, value)) {
            return payloadResultCreator.defaultNoEvents()
        }

        return payloadResultCreator.default([
            new ValueChangedEvent(targetPath, dataCtx.contextName, dataCtx.contextId, updatedValue, value),
        ])
    }

    describePayloadResult(payloadResult: DefaultValuePayloadResult): string {
        if (!payloadResult.events?.length) {
            return payloadResult.error
                ? `Field value was not changed. Default value is not applied due to expression error.`
                : `Field value was not changed.`
        }

        const defaultValueEvent = payloadResult.events[0] as ValueChangedEvent
        if (this.isEmptyValue(defaultValueEvent.previousValue)) {
            return `Field value set to '${ExpressionEvaluator.renderFieldValue(defaultValueEvent.newValue)}'.`
        }
        return `Field value reset from '${ExpressionEvaluator.renderFieldValue(
            defaultValueEvent.previousValue,
        )}' to '${ExpressionEvaluator.renderFieldValue(defaultValueEvent.newValue)}'.`
    }

    private isEmptyValue(value: unknown): boolean {
        return value == undefined || value === ''
    }

    private coerce(
        defaultValue: unknown,
        rule: Rule,
        dataCtx: DataContext,
        session: ExecutionSession,
    ): ExpressionEvaluationResult.Result {
        const field = dataCtx.contextDefinition.fields?.[rule.targetPath]
        if (!field) {
            return ExpressionEvaluationResult.expressionSuccess(defaultValue)
        }
        if (!Contexts.fieldTypeChecker.isPrimitive(field.fieldType) || field.cardinality !== 'SINGLE') {
            throw Error(
                `Unsupported operation. Default value rule '${rule.name}' is being applied on attribute '${
                    dataCtx.contextName
                }.${field.name}' which is not a primitive attribute, but a '${this.toTypeSymbol(field)}'.`,
            )
        }

        if (defaultValue == undefined) {
            return ExpressionEvaluationResult.expressionSuccess(defaultValue)
        }

        switch (field.fieldType) {
            case 'MONEY':
                if (Moneys.isMoney(defaultValue)) {
                    return ExpressionEvaluationResult.expressionSuccess(defaultValue)
                }
                if (typeof defaultValue === 'number') {
                    return ExpressionEvaluationResult.expressionSuccess(toMoney(session.currencyCd, defaultValue))
                }
                return this.logWarningAndReturnErrorResult(defaultValue, dataCtx, field)
            case 'INTEGER':
            case 'DECIMAL':
                if (typeof defaultValue === 'number') {
                    return ExpressionEvaluationResult.expressionSuccess(defaultValue)
                }
                if (Moneys.isMoney(defaultValue)) {
                    return ExpressionEvaluationResult.expressionSuccess(defaultValue.amount)
                }
                return this.logWarningAndReturnErrorResult(defaultValue, dataCtx, field)
            case 'DATE':
            case 'DATETIME':
                if (defaultValue instanceof Date) {
                    return ExpressionEvaluationResult.expressionSuccess(defaultValue)
                }
                return this.logWarningAndReturnErrorResult(defaultValue, dataCtx, field)
            case 'BOOLEAN':
                if (typeof defaultValue === 'boolean') {
                    return ExpressionEvaluationResult.expressionSuccess(defaultValue)
                }
                return this.logWarningAndReturnErrorResult(defaultValue, dataCtx, field)
            case 'STRING':
                if (typeof defaultValue === 'string') {
                    return ExpressionEvaluationResult.expressionSuccess(defaultValue)
                }
                return this.logWarningAndReturnErrorResult(defaultValue, dataCtx, field)
            case 'UUID':
                return ExpressionEvaluationResult.expressionSuccess(defaultValue)
        }
        return ExpressionEvaluationResult.expressionError({
            message: `Unknown primitive field type: ${field.fieldType}`,
            severity: 'critical',
        })
    }

    private logWarningAndReturnErrorResult(
        value: unknown,
        dataContext: DataContext,
        field: ContextField,
    ): ExpressionEvaluationResult.Result {
        const message = `Cannot apply value '${value} (typeof ${typeof value})' on '${dataContext.contextName}.${
            field.name
        }' because value type is not assignable to field type '${this.toTypeSymbol(
            field,
        )}'. Rule will be silently ignored.`
        logger.warning(() => message)
        return ExpressionEvaluationResult.expressionError({ message, severity: 'critical' })
    }

    private toTypeSymbol(field: ContextField): string {
        return field.cardinality === 'SINGLE' ? field.fieldType : field.fieldType + '[]'
    }

    private areValuesEqual(previousValue: unknown, updatedValue: unknown): boolean {
        return (
            (previousValue == undefined && updatedValue == undefined) ||
            updatedValue === previousValue ||
            this.areValuesEqualAsMoney(previousValue, updatedValue) ||
            this.areValuesEqualAsDate(previousValue, updatedValue)
        )
    }

    private areValuesEqualAsMoney(previousValue: unknown, updatedValue: unknown): boolean {
        return (
            Moneys.isMoney(updatedValue) &&
            Moneys.isMoney(previousValue) &&
            updatedValue.amount === previousValue.amount
        )
    }

    private areValuesEqualAsDate(previousValue: unknown, updatedValue: unknown): boolean {
        return (
            updatedValue instanceof Date &&
            previousValue instanceof Date &&
            updatedValue.getTime() === previousValue.getTime()
        )
    }
}
