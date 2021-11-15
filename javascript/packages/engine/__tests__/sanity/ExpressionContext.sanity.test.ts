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
import { sanityEngine } from "./_SanityEngine";

enum ContextVars {
    PLAN = "Premium",
    PACKAGE = "Pizza"
}

describe("Engine Expression Context Sanity Test", () => {
    const { empty } = sanityMocks;
    it("should check context in condition expression", () => {
        const policy = empty();
        const results = sanityEngine.evaluate(
            policy,
            "ExpressionContextCheck-Condition",
            "ExpressionContextCheck-Condition"
        );
        expect(policy.policyNumber).toBe(ContextVars.PLAN);
        expect(policy.transactionDetails!.txReason).toBe(ContextVars.PACKAGE);
        expect(results).not.k_toHaveExpressionsFailures();
        results.getAllRuleResults()
            .forEach(res => expect(res).k_toBeValidRuleResult());
    });
    it("should check context in default expression", () => {
        const policy = empty();
        const results = sanityEngine.evaluate(
            policy,
            "ExpressionContextCheck-Default",
            "ExpressionContextCheck-Default"
        );
        expect(policy.policyNumber).toBe(ContextVars.PLAN);
        expect(policy.transactionDetails!.txReason).toBe(ContextVars.PACKAGE);
        expect(results).not.k_toHaveExpressionsFailures();
        results.getAllRuleResults()
            .forEach(res => expect(res).k_toBeValidRuleResult());
    });
    it("should check context in assertion expression", () => {
        const policy = empty();
        policy.policyNumber = ContextVars.PLAN;
        policy.transactionDetails!.txReason = ContextVars.PACKAGE;
        const results = sanityEngine.evaluate(
            policy,
            "ExpressionContextCheck-Assert",
            "ExpressionContextCheck-Assert"
        );
        expect(results).not.k_toHaveExpressionsFailures();
        results.getAllRuleResults()
            .forEach(res => expect(res).k_toBeValidRuleResult());
    });
    it("should check context in assert expression with cross component reference", () => {
        const policy = empty();
        policy.policyNumber = ContextVars.PLAN;
        policy.transactionDetails!.txReason = ContextVars.PACKAGE;
        policy.insured!.name = ContextVars.PACKAGE;
        const results = sanityEngine.evaluate(
            policy,
            "ExpressionContextCheck-Assert-CCR",
            "ExpressionContextCheck-Assert-CCR"
        );
        expect(results).not.k_toHaveExpressionsFailures();
        results.getAllRuleResults()
            .forEach(res => expect(res).k_toBeValidRuleResult());
    });
});
