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

describe("Engine Sanity Assertion Payload Test", () => {
    const { empty, inValid, valid } = sanityMocks;
    it("should execute 'AssertionAutoPolicy' entrypoint with empty data", () => {
        const results = sanityEngine.evaluate(empty(), "AssertionAutoPolicy");
        expect(results).k_toMatchResultsStats({ total: 4, critical: 3 });
        expect(results).k_toHaveExpressionsFailures(1);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should execute 'AssertionAutoPolicy' entrypoint with valid data", () => {
        const results = sanityEngine.evaluate(valid(), "AssertionAutoPolicy");
        expect(results).k_toMatchResultsStats({ total: 6, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should execute 'AssertionAutoPolicy' entrypoint with not valid data", async () => {
        const results = sanityEngine.evaluate(inValid(), "AssertionAutoPolicy");
        expect(results).k_toMatchResultsStats({ total: 4, critical: 4 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should execute 'AssertionAutoPolicy' entrypoint with not valid data with restriction", () => {
        const data = inValid();
        const results = sanityEngine.evaluateSubTree(data, data.riskItems![0], "AssertionAutoPolicy");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });

    it("should evaluate 'AssertionAutoPolicy' with node that has no rule", () => {
        const data = valid();
        data.insured = {
            id: "121",
            cd: "Insured",
            name: "insuredName"
        };
        const results = sanityEngine.evaluateSubTree(data, data.insured, "AssertionAutoPolicy");
        expect(results).k_toMatchResultsStats({ total: 0, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
    });
});
