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

import { ExpressionEvaluator } from "../runtime/expressions/ExpressionEvaluator";
import { RulePayloadHandler } from "./RulePayloadHandler";
import { DefaultValuePayloadResult, payloadResultCreator } from "../results/PayloadResult";
import { DataContext } from "../contexts/data/DataContext";
import { ValueChangedEvent } from "../results/Events";
import { ExecutionSession } from "../ExecutionSession";

import { Expressions } from "../runtime/expressions/Expressions";
import { ExpressionEvaluationResult } from "../runtime/expressions/ExpressionEvaluationResult";
import { expressionFactory } from "../runtime/expressions/ExpressionFactory";

import { Payloads, Rule, Contexts } from "kraken-model";
import DefaultingType = Payloads.Derive.DefaultingType;
import DefaultValuePayload = Payloads.Derive.DefaultValuePayload;
import PayloadType = Payloads.PayloadType;

function isDefaultType(payload: DefaultValuePayload): boolean {
    return payload.defaultingType === DefaultingType.defaultValue;
}

function isResetType(payload: DefaultValuePayload): boolean {
    return payload.defaultingType === DefaultingType.resetValue;
}

function toMoney(currency: string, amount: number): Contexts.MoneyType | undefined {
    // tslint:disable-next-line: triple-equals
    if (amount == undefined) {
        return undefined;
    }
    return {
        amount: amount,
        currency: currency
    };
}

/**
 * Payload handler implementation to process {@link DefaultValuePayload}s
 */
export class DefaultValuePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) { }

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.DEFAULT;
    }
    executePayload(
        payload: Payloads.Derive.DefaultValuePayload, rule: Rule, dataCtx: DataContext, session: ExecutionSession
    ): DefaultValuePayloadResult {
        // resolve current value
        const resolvePath = Expressions.createPathResolver(dataCtx);
        const targetExpression = expressionFactory.fromPath(resolvePath(rule.targetPath));
        const result = this.evaluator.evaluate(targetExpression, dataCtx);
        if (ExpressionEvaluationResult.isError(result)) {
            throw new Error(`Failed to extract attribute ${rule.targetPath}`);
        }
        const value = result.success;

        // resolve update value
        let updatedValue = value;
        const expression = expressionFactory.fromExpression(payload.valueExpression);
        const defaultValueResult = this.evaluator.evaluate(expression, dataCtx, session.expressionContext);
        if (ExpressionEvaluationResult.isError(defaultValueResult)) {
            return payloadResultCreator.defaultFail(defaultValueResult);
        }

        const defaultValue = defaultValueResult.success;
        let isMoney = false;
        if (dataCtx.definitionProjection) {
            const field = dataCtx.definitionProjection[rule.targetPath];
            if (field) {
                isMoney = Contexts.fieldTypeChecker.isMoney(field.fieldType);
            }
        }

        const newDefaultValue = isMoney
            ? toMoney(session.currencyCd, defaultValue)
            : defaultValue;

        // tslint:disable: triple-equals
        const isDefault = isDefaultType(payload) && (value == undefined || value === "");
        const isReset = isResetType(payload);

        if (isDefault || isReset) {
            const updateValueResult = this.evaluator.evaluateSet(
                targetExpression,
                dataCtx.dataObject,
                (isDefault || isReset)
                    ? newDefaultValue
                    : undefined,
                session.expressionContext
            );
            if (ExpressionEvaluationResult.isError(updateValueResult)) {
                return payloadResultCreator.defaultFail(updateValueResult);
            }
            updatedValue = updateValueResult.success;
        }

        if (updatedValue === value) {
            return payloadResultCreator.defaultNoEvents();
        }
        const path = dataCtx.definitionProjection[rule.targetPath]
            ? dataCtx.definitionProjection[rule.targetPath].fieldPath
            : rule.targetPath;
        return payloadResultCreator.default([
            new ValueChangedEvent(path, dataCtx.contextName, dataCtx.contextId, updatedValue, value)
        ]);
    }
}
