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

import { RulePayloadHandler } from "./RulePayloadHandler";
import { ExpressionEvaluator } from "../runtime/expressions/ExpressionEvaluator";
import { DataContext } from "../contexts/data/DataContext";
import { UsagePayloadResult, payloadResultCreator } from "../results/PayloadResult";

import { Expressions } from "../runtime/expressions/Expressions";

import { Payloads, Rule } from "kraken-model";
import PayloadType = Payloads.PayloadType;
import UsagePayload = Payloads.Validation.UsagePayload;

import { expressionFactory } from "../runtime/expressions/ExpressionFactory";
import { ExpressionEvaluationResult } from "../runtime/expressions/ExpressionEvaluationResult";
import { ExecutionSession } from "../ExecutionSession";

function isValid(value: any): boolean {
    // tslint:disable-next-line: triple-equals
    return value === "" || value == null;
}

export class UsagePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) { }

    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.USAGE;
    }
    executePayload(
        payload: UsagePayload, rule: Rule, dataContext: DataContext, session: ExecutionSession
    ): UsagePayloadResult {
        const expression = expressionFactory.fromPath(Expressions.createPathResolver(dataContext)(rule.targetPath));
        const result = this.evaluator.evaluate(expression, dataContext);
        if (ExpressionEvaluationResult.isError(result)) {
            throw new Error(`Failed to extract attribute ${expression}`);
        }
        const value = result.success;

        const isUsageMandatory =
            (Payloads.Validation.UsageType.mandatory === payload.usageType) && isValid(value);
        const isUsageEmpty =
            (Payloads.Validation.UsageType.mustBeEmpty === payload.usageType) && !isValid(value);
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext
        );
        return payloadResultCreator.usage(payload, !(isUsageMandatory || isUsageEmpty), templateVariables);
    }
}
