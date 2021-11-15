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

import { RulePayloadProcessor } from "../../src/engine/RulePayloadProcessor";
import { Payloads, Rule } from "kraken-model";
import {
    DefaultValuePayloadResult,
    AccessibilityPayloadResult,
    VisibilityPayloadResult
} from "../../src/engine/results/PayloadResult";
import { mock } from "../mock";
import { PayloadBuilder, RulesBuilder } from "kraken-model-builder";
import { ExecutionSession } from "../../src/engine/ExecutionSession";
import { RuleEvaluationResults } from "../../src/dto/RuleEvaluationResults";
import ApplicableRuleEvaluationResult = RuleEvaluationResults.ApplicableRuleEvaluationResult;

import { RuleOverrideContextExtractorImpl } from "../../src/engine/results/RuleOverrideContextExtractor";

const payloadProcessor = new RulePayloadProcessor(mock.evaluator, new RuleOverrideContextExtractorImpl());

function execute(rule: Rule): ApplicableRuleEvaluationResult {
    function config(): ExecutionSession {
        return { currencyCd: "USD", expressionContext: {}, timestamp: new Date() };
    }
    return payloadProcessor.processRule({
        rule,
        dataContext: mock.data.dataContextEmpty()
    }, config()) as ApplicableRuleEvaluationResult;
}
const createRule = (payload: Payloads.Payload) => new RulesBuilder()
    .setContext(mock.modelTreeJson.contexts.Policy.name)
    .setName("mock")
    .setTargetPath(mock.modelTreeJson.contexts.Policy.fields.state.name)
    .setPayload(payload)
    .build();

describe("RulePayloadProcessor", () => {
    it("should process rule evaluation instance with AssertionPayload", () => {
        const rule = createRule(PayloadBuilder.asserts().that("true"));
        expect(execute(rule)).k_toBeValidRuleResult();
    });
    it("should process rule evaluation instance with LengthPayload", () => {
        const rule = createRule(PayloadBuilder.lengthLimit().limit(111));
        expect(execute(rule)).k_toBeValidRuleResult();
    });
    it("should process rule evaluation instance with DefaultValuePayload", () => {
        const rule = createRule(PayloadBuilder.default().to("'AZ'"));
        expect((execute(rule).payloadResult as DefaultValuePayloadResult).events).toHaveLength(1);
    });
    it("should process rule evaluation instance with VisibilityPayload", () => {
        const rule = createRule(PayloadBuilder.visibility().notVisible());
        expect((execute(rule).payloadResult as VisibilityPayloadResult).visible).toBeFalsy();
    });

    it("should process rule evaluation instance with AccessibilityPayload", () => {
        const rule = createRule(PayloadBuilder.accessibility().notAccessible());
        expect((execute(rule).payloadResult as AccessibilityPayloadResult).accessible).toBeFalsy();
    });
});
