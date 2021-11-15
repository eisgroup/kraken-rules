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

import { sanityEngine } from "./_SanityEngine";
import { sanityMocks } from "./_AutoPolicyObject.mocks";

describe("Engine Expressions Sanity Test", () => {
    const { empty } = sanityMocks;
    it("shouldEvaluateDeeplyNestedLoopExpression", () => {
        const policy = empty();

        const results = sanityEngine.evaluate(policy, "Expressions_nested_for");

        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.transactionDetails!.totalPremium).toBe(123);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("shouldEvaluateDeeplyNestedFilterExpression", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "1",
                cd: "Vehicle",
                odometerReading : 100000
            }
        ];

        const results = sanityEngine.evaluate(policy, "Expressions_nested_filter");

        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.termDetails!.termNo).toBe(100000);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("shouldEvaluateDeeplyNestedMixedExpression", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "1",
                cd: "Vehicle",
                odometerReading : 100000
            }
        ];

        const results = sanityEngine.evaluate(policy, "Expressions_nested_mixed");

        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.transactionDetails!.changePremium).toBe(100000);
        expect(results).k_toMatchResultsSnapshots();
    });
});
