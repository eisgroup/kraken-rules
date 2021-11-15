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
import { RulesBuilder, PayloadBuilder } from "kraken-model-builder";
import { Payloads } from "kraken-model";

import { RuleEvaluationResults } from "../../../src/dto/RuleEvaluationResults";
import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult;

import { Localization } from "../../../src/engine/results/Localization";
import defaultMessages = Localization.defaultMessages;
import errorMessage = Localization.errorMessage;

import { ConditionEvaluationResult, ConditionEvaluation } from "../../../src/dto/ConditionEvaluationResult";
import { mock } from "../../mock";
import { RuleInfo } from "../../../src/engine/results/RuleInfo";
import { ContextFieldInfo } from "../../../src/dto/ContextFieldInfo";
import { payloadResultCreator } from "../../../src/engine/results/PayloadResult";

const rule = (p: Payloads.Payload) => new RulesBuilder()
    .setName("r01")
    .setContext("mock")
    .setTargetPath("mock")
    .setPayload(p)
    .build();
const info = () => new ContextFieldInfo(
    mock.dataContextEmpty(),
    "state"
);

describe("Localization", () => {
    it("should leave error code defined and resolve message", () => {
        const payload = PayloadBuilder.lengthLimit().limit(0);
        payload.errorMessage = {
            errorCode: "BR",
            templateParts: [],
            templateExpressions: []
        };
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new RuleInfo(rule(payload)),
            conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.length(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false
            }
        };
        const message = errorMessage(defaultMessages)(res, info());
        expect(message.errorCode).toBe("BR");
        expect(message.errorMessage).toBe("Text must not be longer than 0");
    });
    it("should resolve error message for length rule", () => {
        const payload = PayloadBuilder.lengthLimit().limit(0);
        payload.errorMessage = undefined;
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new RuleInfo(rule(payload)),
            conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.length(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false
            }
        };
        const message = errorMessage(defaultMessages)(res, info());
        expect(message.errorCode).toBe("rule-length-error");
        expect(message.errorMessage).toBe("Text must not be longer than 0");
    });
    it("should resolve error message for usage.mandatory rule", () => {
        const payload = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mandatory);
        payload.errorMessage = undefined;
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new RuleInfo(rule(payload)),
            conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.usage(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false
            }
        };
        const message = errorMessage(defaultMessages)(res, info());
        expect(message.errorCode).toBe("rule-mandatory-error");
        expect(message.errorMessage).toBe("Field is mandatory");
    });
    it("should resolve error message for usage.empty rule", () => {
        const payload = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mustBeEmpty);
        payload.errorMessage = undefined;
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new RuleInfo(rule(payload)),
            conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.usage(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false
            }
        };
        const message = errorMessage(defaultMessages)(res, info());
        expect(message.errorCode).toBe("rule-mandatory-empty-error");
        expect(message.errorMessage).toBe("Field must be empty");
    });
    it("should resolve error message for regexp rule", () => {
        const payload = PayloadBuilder.regExp().match("*");
        payload.errorMessage = undefined;
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new RuleInfo(rule(payload)),
            conditionEvaluationResult: ConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.regexp(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false
            }
        };
        const message = errorMessage(defaultMessages)(res, info());
        expect(message.errorCode).toBe("rule-regexp-error");
        expect(message.errorMessage).toBe("Field must match regular expression pattern: *");
    });
});
