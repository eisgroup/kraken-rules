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
import { LengthPayloadResult, payloadResultCreator } from "../results/PayloadResult";
import { Expressions } from "../runtime/expressions/Expressions";
import { Payloads, Rule } from "kraken-model";
import LengthPayload = Payloads.Validation.LengthPayload;
import PayloadType = Payloads.PayloadType;
import { expressionFactory } from "../runtime/expressions/ExpressionFactory";
import { ExpressionEvaluationResult } from "../runtime/expressions/ExpressionEvaluationResult";
import { ExecutionSession } from "../ExecutionSession";

export class LengthPayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) { }

    handlesPayloadType(): PayloadType {
        return PayloadType.LENGTH;
    }
    executePayload(
        payload: LengthPayload, rule: Rule, dataContext: DataContext, session: ExecutionSession
    ): LengthPayloadResult {
        const targetPathExpression =
            expressionFactory.fromPath(Expressions.createPathResolver(dataContext)(rule.targetPath));
        const result = this.evaluator.evaluate(targetPathExpression, dataContext);
        if (ExpressionEvaluationResult.isError(result)) {
            throw new Error(`Failed to extract attribute '${rule.targetPath}'`);
        }
        const target = result.success;
        const success = typeof target === "string"
            ? (target as string).length <= payload.length
            : true;

        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext
        );

        return payloadResultCreator.length(payload, success, templateVariables);
    }
}
