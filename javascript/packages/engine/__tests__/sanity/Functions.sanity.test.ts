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

describe("Engine Functions Sanity Test", () => {
    const { empty } = sanityMocks;
    it("should default with Count function ", () => {
        const policy = empty();
        policy.policies = ["fourth", "sixth"];
        const results = sanityEngine.evaluate(
            policy,
            "FunctionCheck-Default-With-Count",
            "FunctionCheck-Default-With-Count"
        );
        expect(results).k_toMatchResultsStats({ total: 2 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.policyNumber).toBe("4");
        expect(policy.state).toBe("2");
    });
    it("should default with Sum function ", () => {
        const policy = empty();
        policy.billingInfo = {
            creditCardInfo: {
                id: "13",
                cd: "CreditCardInfo",
                cardCreditLimitAmount: { amount: 1555.35, currency: "USD" },
                cvv: 123
            }
        };
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Default-With-Sum");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.policyNumber).toBe("1678.35");
    });
    it("should default with Avg function ", () => {
        const policy = empty();
        policy.insured!.childrenAges = [5, 9, 18];
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Default-With-Avg");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.policyNumber).toBe("10.66666666666667");
    });
    it("should default with Max function ", () => {
        const policy = empty();
        policy.riskItems![0] = {
            id: "20",
            cd: "Vehicle",
            modelYear: 2001,
            declaredAnnualMiles: 1548458,
            costNew: 12542.652
        };
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Default-With-Max");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.riskItems![0].newValue).toBe(1548458.0);
    });
    it("should default with Min function ", () => {
        const policy = empty();
        policy.insured!.childrenAges = [5, 9, 18];
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Default-With-Min");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.policyNumber).toBe("5");
    });
    it("should flat, assert to have 7 items (3 of them serviceHistories)", () => {
        const policy = empty();
        policy.riskItems = [
            {
                cd: "Vehicle",
                id: "ve1",
                serviceHistory: [new Date()]
            },
            {
                cd: "Vehicle",
                id: "ve2",
                serviceHistory: [new Date(), new Date()]
            }
        ];
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Flat");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should flat and fail, assert to have 7 items (3 of them serviceHistories)", () => {
        const policy = empty();
        policy.riskItems = [
            {
                cd: "Vehicle",
                id: "ve1",
                serviceHistory: [new Date()]
            },
            {
                cd: "Vehicle",
                id: "ve2",
                serviceHistory: [new Date()]
            }
        ];
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Flat");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should default with Substring output ", () => {
        const policy = empty();
        policy.billingInfo!.creditCardInfo! = {
            id: "20",
            cd: "CreditCardInfo",
            cardNumber: "378282246310005",
            cardType: "American Express"
        };
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Default-With-Substring");
        expect(results).k_toMatchResultsStats({ total: 2 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.policyNumber).toBe("22463");
        expect(policy.state).toBe(" Express");
    });
    it("should default with value from function and change field name to field path", () => {
        const policy = empty();
        policy.termDetails!.termCd = "cd125425464";
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Default-PolicyNumber");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.policyNumber).toBe("125425464");
    });
    it("should run functions in assert payload on policy number", () => {
        const policy = empty();
        policy.billingInfo!.creditCardInfo!.cardCreditLimitAmount = {
            amount: 11,
            currency: "USD"
        };
        const results = sanityEngine.evaluate(policy, "FunctionCheck-Assert-PolicyNumber");
        expect(results).k_toMatchResultsStats({ total: 29, critical: 0, warning: 0, info: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
});
