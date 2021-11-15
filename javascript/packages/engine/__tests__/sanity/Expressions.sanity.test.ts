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
import { TestProduct } from "kraken-test-product";

import AnubisCoverage = TestProduct.kraken.testproduct.domain.AnubisCoverage;

describe("Engine Expressions Sanity Test", () => {
    const { valid, empty } = sanityMocks;
    it("should execute filter expressions", () => {
        const policy = valid();
        policy.policyNumber = "P01";
        policy.policies = ["P01", "P02"];
        policy.insured = { name: "P01", cd: "Insured", id: "909" };
        policy.riskItems = [
            { cd: "Vehicle", id: "99", model: "P01" },
            { cd: "Vehicle", id: "89", model: "P02" }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions-Filter");

        expect(results).k_toMatchResultsStats({ total: 10, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should Default Dates With Nested Functions", () => {
        const policy = valid();
        policy.transactionDetails = { cd: "TransactionDetails", id: "99" };
        policy.termDetails = { termExpirationDate: new Date("2019-06-06") };
        const results = sanityEngine.evaluate(policy, "Expressions_Date_Nested_Functions");
        expect(results).k_toMatchResultsStats({ total: 2 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(policy.transactionDetails.txCreateDate).k_toBeDate(new Date("2022-05-05"));
        expect(policy.termDetails.termEffectiveDate).k_toBeDate(new Date("2022-05-05"));
    });
    it("should Evaluate Flat With Predicate", () => {
        const policy = empty();
        policy.riskItems = [
            { id: "20", cd: "Vehicle", serviceHistory: [new Date("2017-10-10"), new Date("2018-10-10")] },
            { id: "21", cd: "Vehicle", serviceHistory: [new Date("2015-10-10"), new Date("2016-10-10")] }
        ];
        policy.transactionDetails = { id: "17", cd: "TransactionDetails" };
        policy.termDetails = { id: "15", cd: "TermDetails", termExpirationDate: new Date("2019-06-06") };
        const results = sanityEngine.evaluate(policy, "Expressions_Flat_with_Predicate");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.policyNumber).toBe("1");
    });
    it("should Default With Value From CCR Collection Count", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "20", cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage", cult: { date: new Date("2018-01-05") } },
                    { id: "22", cd: "AnubisCoverage", cult: { date: new Date("2017-01-05") } }
                ]
            }
        ];
        policy.parties = [
            { id: "15", cd: "Party" }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_default_with_value_from_CCR_collection_count");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.parties[0].relationToPrimaryInsured).toBe("2");
    });
    it("should Find Service History And Match With PlusMonths Expression Output", () => {
        const policy = empty();
        policy.riskItems = [
            { id: "20", cd: "Vehicle", serviceHistory: [new Date("2018-01-05"), new Date("2017-01-05")] },
            { id: "21", cd: "Vehicle", serviceHistory: [new Date("2015-01-05"), new Date("2014-01-05")] },
            { id: "22", cd: "Vehicle", serviceHistory: [new Date("2012-01-05"), new Date("2013-01-05")] }
        ];
        policy.insured = { id: "19", cd: "Insured", childrenAges: [5, 9, 11] };
        policy.parties = [
            {
                id: "15",
                cd: "Party",
                driverInfo: {
                    id: "14", cd: "DriverInfo",
                    trainingCompletionDate: new Date("2013-05-05") // + 25 month = 2015-06-05
                }
            }
        ];
        const results = sanityEngine.evaluate(
            policy, "Expressions_default_driverType_with_value_from_CCR_count_with_predicate"
        );
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.parties[0].driverInfo!.driverType).toBe("2");
    });
    it("should Assert With Proposition Operations", () => {
        const policy = empty();
        policy.parties = [
            {
                id: "15",
                cd: "Party",
                driverInfo: {
                    id: "14",
                    cd: "DriverInfo",
                    trainingCompletionDate: new Date("2017-05-05"), //NumberOfDaysBetween(2017-05-05, 2017-10-10) > 365
                    convicted: true
                },
                personInfo: {
                    id: "12",
                    cd: "PersonInfo",
                    sameHomeAddress: false,
                    addressInfo: {
                        id: "11", cd: "AddressInfo", doNotSolicit: false
                    }
                }
            }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_assert_with_proposition_operations");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should Default With Filter Count Result", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "20",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage", cult: { name: "Yinepu", date: new Date("2018-01-05") } },
                    { id: "22", cd: "AnubisCoverage", cult: { name: "Pharaoh", date: new Date("2017-01-05") } }
                ]
            },
            {
                id: "21",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "23", cd: "AnubisCoverage", cult: { name: "Yinepu", date: new Date("2014-01-05") } },
                    { id: "24", cd: "AnubisCoverage", cult: { name: "Pharaoh", date: new Date("2015-01-05") } }
                ]
            },
            {
                id: "22",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "25", cd: "AnubisCoverage", cult: { name: "Yinepu", date: new Date("2012-01-05") } },
                    { id: "26", cd: "AnubisCoverage", cult: { name: "Pharaoh", date: new Date("2013-01-05") } }
                ]
            }
        ];
        policy.parties = [
            { id: "15", cd: "Party", driverInfo: { id: "14", cd: "DriverInfo" } }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_default_with_filter_count_result");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.policyNumber).toBe("3");
    });
    it("should Compare Sum And Fail If Lower", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "20",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "11", cd: "AnubisCoverage", limitAmount: 50 },
                    { id: "12", cd: "AnubisCoverage", limitAmount: 10 }
                ]
            },
            {
                id: "21",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage", limitAmount: 50 },
                    { id: "22", cd: "AnubisCoverage", limitAmount: 10 }
                ]
            }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_compare_sum");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should Compare Sum And Not Fail If Not Lower", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "20",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "11", cd: "AnubisCoverage", limitAmount: 50 },
                    { id: "12", cd: "AnubisCoverage", limitAmount: 100 }
                ]
            },
            {
                id: "21",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage", limitAmount: 50 },
                    { id: "22", cd: "AnubisCoverage", limitAmount: 100 }
                ]
            }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_compare_sum");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("should Evaluate Expression With CCR And Predicate", () => {
        const policy = empty();
        policy.riskItems = [
            {
                id: "20",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage", cult: { date: new Date("2018-01-05") } },
                    { id: "22", cd: "AnubisCoverage", cult: { date: new Date("2017-01-05") } }
                ]
            },
            {
                id: "21",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "23", cd: "AnubisCoverage", cult: { date: new Date("2015-01-05") } },
                    { id: "24", cd: "AnubisCoverage", cult: { date: new Date("2014-01-05") } }
                ]
            },
            {
                id: "22",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "25", cd: "AnubisCoverage", cult: { date: new Date("2012-01-05") } },
                    { id: "26", cd: "AnubisCoverage", cult: { date: new Date("2013-01-05") } }
                ]
            }
        ];
        policy.parties = [
            {
                id: "15",
                cd: "Party",
                driverInfo: {
                    id: "14",
                    cd: "DriverInfo",
                    trainingCompletionDate: new Date("2017-05-05")
                }
            }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_default_to_with_value_from_CCR_with_predicate");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.parties[0].driverInfo!.driverType).toBe("4");
    });
    it("should Default With Flat And Filter Result Count", () => {
        const policy = empty();
        policy.parties = [
            { id: "15", cd: "Party", driverInfo: { id: "14", cd: "DriverInfo" } }
        ];
        policy.riskItems = [
            {
                id: "20",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage", cult: { name: "Yinepu", date: new Date("2018-01-05") } },
                    { id: "22", cd: "AnubisCoverage", cult: { name: "Pharaoh", date: new Date("2017-01-05") } }
                ]
            },
            {
                id: "21",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "23", cd: "AnubisCoverage", cult: { name: "Yinepu", date: new Date("2014-01-05") } },
                    { id: "24", cd: "AnubisCoverage", cult: { name: "Pharaoh", date: new Date("2015-01-05") } }
                ]
            },
            {
                id: "22",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "25", cd: "AnubisCoverage", cult: { name: "Yinepu", date: new Date("2012-01-05") } },
                    { id: "26", cd: "AnubisCoverage", cult: { name: "Pharaoh", date: new Date("2013-01-05") } }
                ]
            }
        ];
        const results = sanityEngine.evaluate(policy, "Expressions_default_with_flat_and_filter_result_count");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.policyNumber).toBe("3");
    });
    it("should Default State With If Expression Result", () => {
        const policy = empty();
        policy.policies = ["One", "Two", "Three"];
        const results = sanityEngine.evaluate(policy, "Expressions_default_to_policy_state_with_if");
        expect(results).k_toMatchResultsStats({ total: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
        expect(policy.state).toBe("CA");
    });
    it("shouldAddSecretLimitAmountIfCoverageIsInstanceOfAnubisSecretCoverage_AssertPasses", () => {
        const policy = empty();

        policy.riskItems = [
            {
                id: "1",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "11", cd: "AnubisCoverage" },
                    { id: "12", cd: "AnubisSecretCoverage", secretLimitAmount: 30 } as AnubisCoverage
                ]
            },
            {
                id: "2",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage" },
                    { id: "22", cd: "AnubisSecretCoverage", secretLimitAmount: 300 } as AnubisCoverage
                ]
            }
        ];

        const results = sanityEngine.evaluate(policy, "Expressions_instanceof");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("shouldAddSecretLimitAmountIfCoverageIsInstanceOfAnubisSecretCoverage_AssertFails", () => {
        const policy = empty();

        policy.riskItems = [
            {
                id: "1",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "11", cd: "AnubisCoverage" },
                    { id: "12", cd: "AnubisSecretCoverage", secretLimitAmount: 3 } as AnubisCoverage
                ]
            },
            {
                id: "2",
                cd: "Vehicle",
                anubisCoverages: [
                    { id: "21", cd: "AnubisCoverage" },
                    { id: "22", cd: "AnubisSecretCoverage", secretLimitAmount: 300 } as AnubisCoverage
                ]
            }
        ];

        const results = sanityEngine.evaluate(policy, "Expressions_instanceof");
        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
    it("shouldEvaluateRuleOnlyOnCorrectTypeWhenCheckedWithTypeOfInCondition", () => {
        const policy = empty();

        policy.riskItems = [
            {
                id: "1",
                cd: "Vehicle",
                collCoverages: [
                    { id: "11", cd: "COLLCoverage", limitAmount: 10 }
                ],
                rentalCoverage: { id: "12", cd: "RRCoverage", limitAmount: 10 }
            }
        ];

        const results = sanityEngine.evaluate(policy, "Expressions_typeof");
        expect(results.getAllRuleResults()).toHaveLength(2);
        expect(results).k_toMatchResultsStats({ total: 1, critical: 1 });
        expect(results).k_toHaveExpressionsFailures(0);
        expect(results).k_toMatchResultsSnapshots();
    });
});
