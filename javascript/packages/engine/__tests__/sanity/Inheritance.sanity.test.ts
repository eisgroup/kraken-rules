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
import { TestProduct } from "kraken-test-product";

function validWithCoverages(): TestProduct.kraken.testproduct.domain.Policy {
    const validPolicy = sanityMocks.valid();
    validPolicy.riskItems![0].collCoverages = [
        {
            id: "2",
            cd: "COLLCoverage",
            code: "COLLCoverage",
            limitAmount: 50001,
            deductibleAmount: 0,
            effectiveDate: new Date("2000-01-01"),
            expirationDate: new Date("3000-01-01")
        }
    ],
        validPolicy.riskItems![0].fullCoverages = [
            {
                id: "5",
                cd: "FullCoverage",
                code: "FullCoverage",
                limitAmount: 800000,
                deductibleAmount: 0,
                effectiveDate: new Date("2000-01-01"),
                expirationDate: new Date("3000-01-01")
            }
        ];
    validPolicy.riskItems![0].anubisCoverages = [
        {
            id: "A5",
            cd: "AnubisCoverage",
            code: "AnubisCoverage",
            limitAmount: 800000,
            deductibleAmount: 0
        }
    ];
    validPolicy.riskItems![0].rentalCoverage = {
        id: "R7",
        cd: "RRCoverage",
        code: "RRCoverage",
        combinedLimit: "5200",
        limitAmount: 800000,
        deductibleAmount: 0
    };
    return validPolicy;
}

function emptyWithCoverages(): TestProduct.kraken.testproduct.domain.Policy {
    const emptyPolicy = sanityMocks.empty();
    emptyPolicy.riskItems![0].collCoverages = [
        {
            id: "2",
            cd: "COLLCoverage"
        }
    ],
        emptyPolicy.riskItems![0].fullCoverages = [
            {
                id: "5",
                cd: "FullCoverage"
            }
        ];
    emptyPolicy.riskItems![0].anubisCoverages = [
        {
            id: "A5",
            cd: "AnubisCoverage"
        }
    ];
    emptyPolicy.riskItems![0].rentalCoverage = {
        id: "7",
        cd: "RRCoverage"
    };
    return emptyPolicy;
}

describe("Sanity Coverages Inheritance tests", () => {
    describe("Inherited Field Sanity tests", () => {
        it("should validate local and inherited fields", () => {
            const results = sanityEngine.evaluate(sanityMocks.valid(), "AddressInfoLocalAndInheritedFieldRules");
            expect(results).k_toMatchResultsStats({ total: 7, critical: 0 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
        it("should validate inherited mandatory fields", () => {
            const data = sanityMocks.valid();
            data.riskItems![0].collCoverages = [{
                id: "2",
                cd: "FullCoverage"
            }];
            const results = sanityEngine.evaluate(data, "ValidateInheritedMandatoryFields");
            expect(results).k_toMatchResultsStats({ total: 5, critical: 5 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
        it("should set inherited default fields", () => {
            const data = sanityMocks.valid();
            data.riskItems![0].fullCoverages = [{
                id: "2",
                cd: "FullCoverage"
            }];
            const results = sanityEngine.evaluate(data, "ValidateInheritedDefaultField");
            expect(data.riskItems![0].fullCoverages[0].limitAmount).toBe(1000);
            expect(data.riskItems![0].fullCoverages[0].deductibleAmount).toBe(1000);
            expect(results).k_toMatchResultsStats({ total: 2 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
        it("should validate inherited presentation fields", () => {
            const data = sanityMocks.valid();
            data.riskItems![0].fullCoverages = [{
                id: "2",
                cd: "COLLCoverage"
            }];
            const results = sanityEngine.evaluate(data, "ValidateInheritedPresentationFields");
            expect(results).k_toMatchResultsStats({ total: 2, disabled: 1, hidden: 1 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
    });
    describe("Inheritance field test on AccessibilityCarCoverage", () => {
        it("should execute 'AccessibilityCarCoverage' entry point", () => {
            const results = sanityEngine.evaluate(validWithCoverages(), "AccessibilityCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 8, disabled: 8, hidden: 0 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
    });
    describe("Inheritance field test on VisibilityCarCoverage", () => {
        it("should execute 'VisibilityCarCoverage' entry point", async () => {
            const results = sanityEngine.evaluate(validWithCoverages(), "VisibilityCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 7, hidden: 7, disabled: 0 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
    });

    describe("Inheritance field test on UsageCarCoverage", () => {
        it("should execute 'UsageCarCoverage' entry point on empty data", async () => {
            const results = sanityEngine.evaluate(emptyWithCoverages(), "UsagePayloadCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 13, critical: 13 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
        it("should execute 'UsageCarCoverage' entry point on valid data", async () => {
            const results = sanityEngine.evaluate(validWithCoverages(), "UsagePayloadCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 13, critical: 0 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
    });

    describe("Inheritance field test on InitCarCoverage", () => {
        it("should execute 'InitCarCoverage' entry point rules to defaultValue on empty data", async () => {
            const results = sanityEngine.evaluate(emptyWithCoverages(), "InitCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 7 });
            expect(results).k_toHaveExpressionsFailures(0);
        });
        it("should execute 'InitCarCoverage' entry point rules to resetValue on valid data", async () => {
            const validData = validWithCoverages();
            const results = sanityEngine.evaluate(validData, "InitCarCoverage");

            expect(validData.riskItems![0].fullCoverages![0]["deductibleAmount"]).toBe(20000);
            expect(results).k_toMatchResultsStats({ total: 8 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
    });

    describe("Inheritance field test on AssertionCarCoverage", () => {
        it("should execute 'AssertionCarCoverage' entry point on empty data", async () => {
            const results = sanityEngine.evaluate(emptyWithCoverages(), "AssertionCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 5, critical: 0 });
            expect(results).k_toHaveExpressionsFailures(5);
            expect(results).k_toMatchResultsSnapshots();
        });
        it("should execute 'AssertionCarCoverage' entry point on valid data", async () => {
            const results = sanityEngine.evaluate(validWithCoverages(), "AssertionCarCoverage");
            expect(results).k_toMatchResultsStats({ total: 5, critical: 0 });
            expect(results).k_toHaveExpressionsFailures(0);
            expect(results).k_toMatchResultsSnapshots();
        });
    });

});
