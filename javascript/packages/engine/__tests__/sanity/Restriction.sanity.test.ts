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

import { sanityMocks } from "./_AutoPolicyObject.mocks";
import { sanityEngine as engine } from "./_SanityEngine";
import { mock } from "../mock";
import { ConditionEvaluation } from "../../src/dto/ConditionEvaluationResult";

const RRCoverage = mock.modelTreeJson.contexts.RRCoverage;
const Vehicle = mock.modelTreeJson.contexts.Vehicle;

describe("Engine Sanity rule order test", () => {
    const { empty } = sanityMocks;
    it("should execute rules in order", () => {
        const policy = empty();
        policy.riskItems = [{
            cd: Vehicle.name,
            id: `${Vehicle.name}-1`,
            rentalCoverage: {
                cd: RRCoverage.name,
                id: `${RRCoverage.name}-1`
            },
            modelYear: 2020
        },
        {
            cd: Vehicle.name,
            id: `${Vehicle.name}-2`,
            rentalCoverage: {
                cd: RRCoverage.name,
                id: `${RRCoverage.name}-2`
            },
            modelYear: 2021
        }];
        const data = mock.data.dataContextCustom(policy);
        const r1 = engine.evaluateSubTree(
            data.dataObject,
            policy,
            "ForRestrictionCache",
            undefined,
            { evaluationId: "1" }
        );
        const r2 = engine.evaluateSubTree(
            data.dataObject,
            policy.riskItems[0],
            "ForRestrictionCache",
            undefined,
            { evaluationId: "1" }
        );
        const r3 = engine.evaluateSubTree(
            data.dataObject,
            policy.riskItems[1],
            "ForRestrictionCache",
            undefined,
            { evaluationId: "1" }
        );
        expect(r1.getAllRuleResults()).toHaveLength(2);
        expect(r1.getAllRuleResults()[0].conditionEvaluationResult.error).toBeUndefined();
        expect(r1.getAllRuleResults()[1].conditionEvaluationResult.error).toBeUndefined();
        expect(r1.getAllRuleResults()[0].conditionEvaluationResult.conditionEvaluation)
            .toBe(ConditionEvaluation.APPLICABLE);
        expect(r1.getAllRuleResults()[1].conditionEvaluationResult.conditionEvaluation)
            .toBe(ConditionEvaluation.NOT_APPLICABLE);

        expect(r2.getAllRuleResults()).toHaveLength(1);
        expect(r2.getAllRuleResults()[0].conditionEvaluationResult.error).toBeUndefined();
        expect(r2.getAllRuleResults()[0].conditionEvaluationResult.conditionEvaluation)
            .toBe(ConditionEvaluation.APPLICABLE);

        expect(r3.getAllRuleResults()).toHaveLength(1);
        expect(r3.getAllRuleResults()[0].conditionEvaluationResult.error).toBeUndefined();
        expect(r3.getAllRuleResults()[0].conditionEvaluationResult.conditionEvaluation)
            .toBe(ConditionEvaluation.NOT_APPLICABLE);
    });
});
