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

describe("Engine Sanity Rule Order Function Complex Types", () => {
    const { empty } = sanityMocks;
    it("should set default coverage and transaction detail limit amounts to 1000", async () => {
        const policy = empty();
        policy.transactionDetails = { id: "2", cd: "TransactionDetails" };
        policy.coverage = {
            id: "1", cd: "CarCoverage"
        };

        const results = sanityEngine.evaluate(policy, "FunctionCheck-RulesUsingFunctionOrderCheck");

        expect(policy.coverage.limitAmount).toBe(1000);
        expect(policy.transactionDetails.totalLimit).toBe(1000);

        expect(results).k_toMatchResultsStats({ total: 0, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
    });
});
