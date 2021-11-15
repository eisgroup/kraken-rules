/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

import { Payloads, Rule } from "kraken-model";
import PayloadType = Payloads.PayloadType;
import SizeRangePayload = Payloads.Validation.SizeRangePayload;
import { RulePayloadHandler } from "./RulePayloadHandler";
import { ExpressionEvaluator } from "../runtime/expressions/ExpressionEvaluator";
import { DataContext } from "../contexts/data/DataContext";
import { SizeRangePayloadResult, payloadResultCreator } from "../results/PayloadResult";
import { Expressions } from "../runtime/expressions/Expressions";
import { expressionFactory } from "../runtime/expressions/ExpressionFactory";
import { ExpressionEvaluationResult } from "../runtime/expressions/ExpressionEvaluationResult";
import { ExecutionSession } from "../ExecutionSession";

export class SizeRangePayloadHandler implements RulePayloadHandler {
    constructor(private readonly evaluator: ExpressionEvaluator) { }

    handlesPayloadType(): PayloadType {
        return PayloadType.SIZE_RANGE;
    }
    executePayload(
        payload: SizeRangePayload, rule: Rule, dataContext: DataContext, session: ExecutionSession
    ): SizeRangePayloadResult {
        const expression = expressionFactory.fromPath(Expressions.createPathResolver(dataContext)(rule.targetPath));
        const exResult = this.evaluator.evaluate(expression, dataContext);
        if (ExpressionEvaluationResult.isError(exResult)) {
            throw new Error(`Failed to extract attribute ${expression}`);
        }
        let target = exResult.success;
        const templateVariables = this.evaluator.evaluateTemplateVariables(
            payload.errorMessage,
            dataContext,
            session.expressionContext
        );
        const result = (success: boolean) => payloadResultCreator.sizeRange(payload, success, templateVariables);
        // tslint:disable-next-line
        if (target == undefined) {
            target = [];
        }
        if (Array.isArray(target)) {
            return result(target.length >= payload.min && target.length <= payload.max);
        }
        return result(true);
    }
}
