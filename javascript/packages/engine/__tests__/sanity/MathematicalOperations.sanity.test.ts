/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

describe("Engine Mathematical Operations Sanity Test", () => {
    const { empty } = sanityMocks;
    it("should execute mathematical operations", () => {
        const policy = empty();

        sanityEngine.evaluate(policy, "Math");

        expect(policy.transactionDetails!.totalLimit).toBe(10);
        expect(policy.transactionDetails!.changePremium).toBe(8);
        expect(policy.transactionDetails!.totalPremium).toBe(-0.8413);
    });
    it("should execute mathematical operations dev testing", () => {
        const policy = empty();

        sanityEngine.evaluate(policy, "Math_DevTesting");

        expect(policy.policyNumber).toBe("7");
        expect(policy.transactionDetails!.txType).toBe("8");
        expect(policy.transactionDetails!.txReason).toBe("0.117");
        expect(policy.policyDetail!.oosProcessingStage).toBe("6.923076923076923");
        expect(policy.transactionDetails!.changePremium).toBe(0);
        expect(policy.transactionDetails!.totalPremium).toBe(9025);
        expect(policy.policyDetail!.versionDescription).toBe(
            "1000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000"
        );
    });
});
