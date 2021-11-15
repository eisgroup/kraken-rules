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

describe("SizeRange payload sanity test", () => {
    it("should fail when size is less than min", () => {
        const policy = sanityMocks.empty();
        policy.policies = ["1"];
        const result = sanityEngine.evaluate(policy, "SizeRangePayload");
        expect(result).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(result).k_toHaveExpressionsFailures(0);
        expect(result).k_toMatchResultsSnapshots();
    });
    it("should fail when size is more than max", () => {
        const policy = sanityMocks.empty();
        policy.policies = ["1", "2", "3", "4"];
        const result = sanityEngine.evaluate(policy, "SizeRangePayload");
        expect(result).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(result).k_toHaveExpressionsFailures(0);
        expect(result).k_toMatchResultsSnapshots();
    });
    it("should succeed when size is in range", () => {
        const policy = sanityMocks.empty();
        policy.policies = ["1", "2"];
        const result = sanityEngine.evaluate(policy, "SizeRangePayload");
        expect(result).k_toMatchResultsStats({ total: 1, critical: 0 });
        expect(result).k_toHaveExpressionsFailures(0);
        expect(result).k_toMatchResultsSnapshots();
    });
});
