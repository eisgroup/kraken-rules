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
import { ContextModelTree } from '../../models/ContextModelTree'
import {
    DEFAULT_VALUE_PAYLOAD_INCOMPATIBLE_VALUE,
    formatCodeWithMessage,
    SystemMessageBuilder,
} from '../../error/KrakenRuntimeError'

function isDefaultType(payload: DefaultValuePayload): boolean {
    return payload.defaultingType === DefaultingType.defaultValue
}

function isResetType(payload: DefaultValuePayload): boolean {
    return payload.defaultingType === DefaultingType.resetValue
}

/**
 * Payload handler implementation to process {@link DefaultValuePayload}s
 */
export class DefaultValuePayloadHandler implements RulePayloadHandler {
    constructor(
        private readonly evaluator: ExpressionEvaluator,
        private readonly modelTree: ContextModelTree.ContextModelTree,
    ) {}

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.DEFAULT
    }
    executePayload(rule: Rule, dataCtx: DataContext, session: ExecutionSession): DefaultValuePayloadResult {
        const payload = rule.payload as DefaultValuePayload
        const value = this.evaluator.evaluateTargetField(rule.targetPath, dataCtx)

        const targetPath = TargetPathUtils.resolveTargetPath(rule.targetPath, dataCtx)

        logger.debug(() => formatExpressionEvaluationMessage('default value', payload.valueExpression, dataCtx))
        const expressionResult = this.evaluator.evaluate(payload.valueExpression, dataCtx, session)
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
        return value == undefined || value === '' || (Array.isArray(value) && value.length === 0)
    }

    private coerce(
        value: unknown,
        rule: Rule,
        dataCtx: DataContext,
        session: ExecutionSession,
    ): ExpressionEvaluationResult.Result {
        const field = dataCtx.contextDefinition.fields?.[rule.targetPath]
        if (!field) {
            return ExpressionEvaluationResult.expressionSuccess(value)
        }
        const isPrimitive = Contexts.fieldTypeChecker.isPrimitive(field.fieldType)
        const isComplexSystem = this.modelTree.contexts[field.fieldType]?.system
        if (!isPrimitive && !isComplexSystem) {
            throw Error(
                `Unsupported operation. Default value rule '${rule.name}' is being applied on attribute '${
                    dataCtx.contextName
                }.${field.name}' whose type is '${this.toTypeSymbol(
                    field,
                )}'. Default value rule can only be applied on attribute which is primitive or a collection of primitives or complex system type.`,
            )
        }
        if (isComplexSystem) {
            return ExpressionEvaluationResult.expressionSuccess(value)
        }
        return this.coerceValue(value, field, dataCtx, session)
    }

    private coerceValue(
        value: unknown,
        field: ContextField,
        dataCtx: DataContext,
        session: ExecutionSession,
    ): ExpressionEvaluationResult.Result {
        if (field.cardinality === 'MULTIPLE') {
            if (value == undefined) {
                return ExpressionEvaluationResult.expressionSuccess(value)
            }
            if (Array.isArray(value)) {
                const arrayValue = []
                for (const v of value) {
                    try {
                        arrayValue.push(this.coercePrimitiveValue(v, field, session))
                    } catch (e) {
                        return this.logWarningAndReturnErrorResult(value, dataCtx, field)
                    }
                }
                return ExpressionEvaluationResult.expressionSuccess(arrayValue)
            }
            return this.logWarningAndReturnErrorResult(value, dataCtx, field)
        }

        try {
            const coercedValue = this.coercePrimitiveValue(value, field, session)
            return ExpressionEvaluationResult.expressionSuccess(coercedValue)
        } catch (e) {
            return this.logWarningAndReturnErrorResult(value, dataCtx, field)
        }
    }

    private coercePrimitiveValue(value: unknown, field: ContextField, session: ExecutionSession): unknown {
        if (value == undefined) {
            return value
        }
        switch (field.fieldType) {
            case 'MONEY':
                if (Moneys.isMoney(value)) {
                    return value
                }
                if (typeof value === 'number') {
                    return Moneys.toMoney(session.currencyCd, value)
                }
                throw new Error(`Cannot convert to: ${field.fieldType}`)
            case 'INTEGER':
            case 'DECIMAL':
                if (typeof value === 'number') {
                    return value
                }
                if (Moneys.isMoney(value)) {
                    return value.amount
                }
                throw new Error(`Cannot convert to: ${field.fieldType}`)
            case 'DATE':
                if (value instanceof Date && session.dateCalculator.isDate(value)) {
                    return value
                }
                if (value instanceof Date && session.dateCalculator.isDateTime(value)) {
                    return session.dateCalculator.toDate(value, session.ruleTimezoneId)
                }
                throw new Error(`Cannot convert to: ${field.fieldType}`)
            case 'DATETIME':
                if (value instanceof Date && session.dateCalculator.isDateTime(value)) {
                    return value
                }
                if (value instanceof Date && session.dateCalculator.isDate(value)) {
                    return session.dateCalculator.toDateTime(value, session.ruleTimezoneId)
                }
                throw new Error(`Cannot convert to: ${field.fieldType}`)
            case 'BOOLEAN':
                if (typeof value === 'boolean') {
                    return value
                }
                throw new Error(`Cannot convert to: ${field.fieldType}`)
            case 'STRING':
                if (typeof value === 'string') {
                    return value
                }
                throw new Error(`Cannot convert to: ${field.fieldType}`)
            case 'UUID':
                return value
        }
        throw new Error(`Unknown primitive field type: ${field.fieldType}`)
    }

    private logWarningAndReturnErrorResult(
        value: unknown,
        dataContext: DataContext,
        field: ContextField,
    ): ExpressionEvaluationResult.Result {
        const m = new SystemMessageBuilder(DEFAULT_VALUE_PAYLOAD_INCOMPATIBLE_VALUE)
            .parameters(
                ExpressionEvaluator.renderFieldValue(value),
                typeof value,
                dataContext.contextName,
                field.name,
                this.toTypeSymbol(field),
            )
            .build()
        const formattedMessage = formatCodeWithMessage(m)
        logger.warning(() => formattedMessage)
        return ExpressionEvaluationResult.expressionError({ message: formattedMessage, severity: 'critical' })
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
