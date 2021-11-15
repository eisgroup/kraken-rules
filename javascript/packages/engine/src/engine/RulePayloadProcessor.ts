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

import { Reducer, optional, MethodMap } from "declarative-js";
import toMap = Reducer.toMap;
import Map = Reducer.Map;

import { RuleEvaluationResults } from "../dto/RuleEvaluationResults";
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult;
import { ExecutionSession } from "./ExecutionSession";
import { ConditionEvaluationResult, ConditionEvaluation } from "../dto/ConditionEvaluationResult";
import { payloadResultTypeChecker, ValidationPayloadResult } from "./results/PayloadResult";
import { RulePayloadHandler } from "./handlers/RulePayloadHandler";
import { AssertionPayloadHandler } from "./handlers/AssertionPayloadHandler";
import { DefaultValuePayloadHandler } from "./handlers/DefaultValuePayloadHandler";
import { accessibilityPayloadHandler } from "./handlers/AccessibilityPayloadHandler";
import { visibilityPayloadHandler } from "./handlers/VisibilityPayloadHandler";
import { RegExpPayloadHandler } from "./handlers/RegExpPayloadHandler";
import { UsagePayloadHandler } from "./handlers/UsagePayloadHandler";
import { LengthPayloadHandler } from "./handlers/LengthPayloadHandler";
import { SizePayloadHandler } from "./handlers/SizePayloadHandler";
import { SizeRangePayloadHandler } from "./handlers/SizeRangePayloadHandler";
import { RuleEvaluation } from "./processors/OrderedEvaluationLoop";
import { ExpressionEvaluator } from "./runtime/expressions/ExpressionEvaluator";
import { RuleInfo } from "./results/RuleInfo";
import { Payloads, Dependency } from "kraken-model";
import { RuleOverrideContextExtractor } from "./results/RuleOverrideContextExtractor";
import { DataContext } from "./contexts/data/DataContext";

export class RulePayloadProcessor {

    private readonly payloadHandlers: MethodMap<RulePayloadHandler>;
    private readonly overrideExtractor: RuleOverrideContextExtractor;

    constructor(
        expressionEvaluator: ExpressionEvaluator,
        overrideExtractor: RuleOverrideContextExtractor
    ) {
        this.overrideExtractor = overrideExtractor;
        this.payloadHandlers = [
            new AssertionPayloadHandler(expressionEvaluator),
            new DefaultValuePayloadHandler(expressionEvaluator),
            accessibilityPayloadHandler,
            visibilityPayloadHandler,
            new RegExpPayloadHandler(expressionEvaluator),
            new UsagePayloadHandler(expressionEvaluator),
            new LengthPayloadHandler(expressionEvaluator),
            new SizePayloadHandler(expressionEvaluator),
            new SizeRangePayloadHandler(expressionEvaluator)
        ].reduce(toMap(
            h => h.handlesPayloadType().toString()),
            Map()
        );
    }

    processRule(ruleEvaluationInstance: RuleEvaluation, session: ExecutionSession): RuleEvaluationResult {
        const { rule, dataContext } = ruleEvaluationInstance;
        const ruleInfo = new RuleInfo(rule);
        const payloadResult = optional(this.payloadHandlers.get(rule.payload.type))
            .orElseThrow(`Not supported payload type: ${rule.payload.type}`)
            .executePayload(rule.payload, rule, dataContext, session);
        // results for validation payload
        if (payloadResultTypeChecker.isValidation(payloadResult) && Payloads.isValidationPayload(rule.payload)) {
            // results for failed validation with rule
            return this.resolveValidationResult(
                payloadResult,
                rule.payload,
                dataContext,
                ruleInfo,
                rule.dependencies || [],
                session.timestamp
            );
        } else {
            return {
                kind: RuleEvaluationResults.Kind.REGULAR,
                ruleInfo,
                conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                payloadResult
            };
        }
    }

    private resolveValidationResult(
        payloadResult: ValidationPayloadResult,
        payload: Payloads.Validation.ValidationPayload,
        dataContext: DataContext,
        ruleInfo: RuleInfo,
        dependencies: Dependency[],
        timestamp: Date
    ): RuleEvaluationResults.ValidationRuleEvaluationResult {
        if (payloadResult.success === false && payload.isOverridable) {
            return {
                kind: RuleEvaluationResults.Kind.VALIDATION,
                conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                ruleInfo,
                payloadResult,
                overrideInfo: {
                    overrideApplicable: true,
                    overridable: payload.isOverridable,
                    overrideGroup: payload.overrideGroup,
                    overrideContext: this.overrideExtractor.extract(
                        dataContext,
                        ruleInfo,
                        dependencies.map(d => ({
                            contextName: d.contextName,
                            contextFieldName: d.targetPath
                        })),
                        timestamp
                    )
                }
            };
        } else {
            return {
                kind: RuleEvaluationResults.Kind.VALIDATION,
                conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                ruleInfo,
                payloadResult,
                overrideInfo: {
                    overrideApplicable: false,
                    overridable: Boolean(payload.isOverridable),
                    overrideGroup: payload.overrideGroup
                }
            };
        }
    }
}
