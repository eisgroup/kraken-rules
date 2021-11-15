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

import { RepoClientCache } from "../../src/repository/RepoClientCache";
import { MockBundleBuilder } from "../MockBundleBuilder";
import { RulesBuilder, PayloadBuilder } from "kraken-model-builder";
import { Rule } from "kraken-model";

const isDimensional = true;

function rule(name: string, options?: { isDimensional?: boolean }): Rule {
    const builder = new RulesBuilder()
        .setName(name)
        .setTargetPath("mockPath")
        .setContext("mockContextName")
        .setPayload(PayloadBuilder.size().min(1));
    if (options && options.isDimensional) {
        builder.setDimensional();
    }
    return builder.build();
}

const EP_ONE = "OneCat";
const EP_TWO = "TwoCat";
const EP_THREE = "ThreeCat";
const EP_STATIC = "Static";

let cache: RepoClientCache;
beforeEach(() => {
    cache = new RepoClientCache();
    const addBundleForEmptyDimensions = cache.addBundleForDimension({});
    addBundleForEmptyDimensions({
        entryPointName: EP_ONE,
        entryPointBundle: MockBundleBuilder.builder()
            .addRule(rule("Default.OneCat"))
            .build()
    });
    addBundleForEmptyDimensions({
        entryPointName: EP_TWO,
        entryPointBundle: MockBundleBuilder.builder()
            .addRule(rule("Default.TwoCat"))
            .build()
    });
    addBundleForEmptyDimensions({
        entryPointName: EP_THREE,
        entryPointBundle: MockBundleBuilder.builder()
            .addRule(rule("Default.ThreeCat"))
            .build()
    });

    const addBundleForStateCodeDimensions = cache.addBundleForDimension({ state: "CA", code: "000" });
    const addBundleForCodeStateDimensions = cache.addBundleForDimension({ code: "000", state: "CA" });
    addBundleForStateCodeDimensions({
        entryPointName: "OneCat",
        entryPointBundle: MockBundleBuilder.builder()
            .addRule(rule("State:CA.OneCat"))
            .build()
    });
    addBundleForCodeStateDimensions({
        entryPointName: "TwoCat",
        entryPointBundle: MockBundleBuilder.builder()
            .addRule(rule("State:CA.TwoCat"))
            .build()
    });
    addBundleForStateCodeDimensions({
        entryPointName: "ThreeCat",
        entryPointBundle: MockBundleBuilder.builder()
            .addRule(rule("State:CA.ThreeCat"))
            .build()
    });
});

describe("RepoClientCache", () => {
    it("should invalidate cache", () => {
        cache.clearCache();
        expect(cache.isCached("ThreeCat", {})).toBeFalsy();
    });
    describe("dimensional and static support", () => {
        it("should concat static and dynamic rules", () => {
            const deltaCache = new RepoClientCache(true);
            deltaCache.addBundleForDimension({ static: "1" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("R1"))
                    .addRule(rule("R2", { isDimensional }))
                    .build()
            });
            deltaCache.addBundleForDimension({ static: "2" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addOrder("R1")
                    .addRule(rule("R3", { isDimensional }))
                    .build()
            });
            deltaCache.addBundleForDimension({ static: "2" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("R0"))
                    .addOrder("R1")
                    .addRule(rule("R3", { isDimensional }))
                    .build()
            });
            const bundle = deltaCache.getBundleForDimension({ static: "2" })(EP_STATIC);
            expect(bundle.evaluation.rules).toHaveLength(2);
            expect(bundle.evaluation.rules.map(r => r.name)).toContain("R1");
            expect(bundle.evaluation.rules.map(r => r.name)).toContain("R3");
        });
        it("should not override static rules with delta enabled repo", () => {
            const deltaCache = new RepoClientCache(true);

            deltaCache.addBundleForDimension({ static: "1" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("STATIC1"))
                    .build()
            });
            deltaCache.addBundleForDimension({ static: "3" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("STATIC2"))
                    .build()
            });
            deltaCache.addBundleForDimension({ static: "2" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addOrder("STATIC1")
                    .addRule(rule("R3", { isDimensional }))
                    .build()
            });
            const bundle = deltaCache.getBundleForDimension({ static: "2" })(EP_STATIC);
            expect(bundle.evaluation.rules).toHaveLength(2);
            expect(bundle.evaluation.rules.map(r => r.name)).toContain("R3");
            expect(bundle.evaluation.rules.map(r => r.name)).toContain("STATIC1");
        });
        it("should clear static rules cache", () => {
            const deltaCache = new RepoClientCache(true);

            deltaCache.addBundleForDimension({ static: "1" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("STATIC1"))
                    .build()
            });
            deltaCache.clearCache();
            deltaCache.addBundleForDimension({ static: "3" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("STATIC2"))
                    .build()
            });
            deltaCache.addBundleForDimension({ static: "2" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addOrder("STATIC2")
                    .addRule(rule("R3", { isDimensional }))
                    .build()
            });
            const bundle = deltaCache.getBundleForDimension({ static: "2" })(EP_STATIC);
            expect(bundle.evaluation.rules).toHaveLength(2);
            expect(bundle.evaluation.rules.map(r => r.name)).toContain("R3");
            expect(bundle.evaluation.rules.map(r => r.name)).toContain("STATIC2");
        });
        it("should order rules", () => {
            const deltaCache = new RepoClientCache(true);
            deltaCache.addBundleForDimension({})({
                entryPointName: "order",
                entryPointBundle: {
                    engineVersion: "1",
                    expressionContext: {},
                    evaluation: {
                        delta: false,
                        entryPointName: "order",
                        rules: [rule("qqq", { isDimensional }), rule("www")],
                        rulesOrder: {
                            qqq: 0,
                            www: 1
                        }
                    }
                }
            });
            deltaCache.addBundleForDimension({ a: 1 })({
                entryPointName: "order",
                entryPointBundle: {
                    engineVersion: "1",
                    expressionContext: {},
                    evaluation: {
                        delta: true,
                        entryPointName: "order",
                        rules: [
                            rule("eee", { isDimensional }),
                            rule("rrr", { isDimensional })
                        ],
                        rulesOrder: {
                            eee: 0,
                            www: 1,
                            rrr: 2
                        }
                    }
                }
            });
        });
        it("should order rules with missing order", () => {
            function getOrder(ruleNames: string[]): string[] {
                const deltaCache = new RepoClientCache(true);

                // add static rules to the cache
                deltaCache.addBundleForDimension({})({
                    entryPointName: "order",
                    entryPointBundle: {
                        engineVersion: "1",
                        expressionContext: {},
                        evaluation: {
                            delta: false,
                            entryPointName: "order",
                            rules: ruleNames.map(rn => rule(rn, { isDimensional: rn === "v" })),
                            rulesOrder: {
                                d1: 0,
                                d2: 1
                            }
                        }
                    }
                });

                // add dimensional rules to the cache
                deltaCache.addBundleForDimension({ result: "1" })({
                    entryPointName: "order",
                    entryPointBundle: {
                        engineVersion: "1",
                        expressionContext: {},
                        evaluation: {
                            delta: false,
                            entryPointName: "order",
                            rules: ruleNames.map(rn => rule(rn, { isDimensional: rn === "v" })),
                            rulesOrder: {
                                d1: 0,
                                d2: 1
                            }
                        }
                    }
                });
                const bundle = deltaCache.getBundleForDimension({ result: "1" })("order");
                return bundle.evaluation.rules.map(r => r.name);
            }
            const expected = ["d1", "d2", "v"];
            expect(getOrder(["v", "d1", "d2"])).toMatchObject(expected);
            expect(getOrder(["v", "d2", "d1"])).toMatchObject(expected);
            expect(getOrder(["d1", "v", "d2"])).toMatchObject(expected);
            expect(getOrder(["d2", "v", "d1"])).toMatchObject(expected);
            expect(getOrder(["d1", "d2", "v"])).toMatchObject(expected);
            expect(getOrder(["d2", "d1", "v"])).toMatchObject(expected);
        });
        it("should check static rules cache", () => {
            const deltaCache = new RepoClientCache(true);
            deltaCache.addBundleForDimension({ static: "3" })({
                entryPointName: EP_STATIC,
                entryPointBundle: MockBundleBuilder.builder()
                    .addRule(rule("STATIC1"))
                    .addRule(rule("STATIC2"))
                    .build()
            });
            expect(deltaCache.areCachedStaticRules(EP_STATIC)).toBe(true);
            expect(deltaCache.areCachedStaticRules("not_existing")).toBe(false);
        });
    });
    it("should get bundle for empty dimensions", () => {
        const getBundleForEmptyDimension = cache.getBundleForDimension({});
        expect(getBundleForEmptyDimension("OneCat").evaluation.rules[0].name).toBe("Default.OneCat");
        expect(getBundleForEmptyDimension("TwoCat").evaluation.rules[0].name).toBe("Default.TwoCat");
    });
    it("should get bundle for state dimensions", () => {
        const getBundleForEmptyDimension = cache.getBundleForDimension({ state: "CA", code: "000" });
        expect(getBundleForEmptyDimension("OneCat").evaluation.rules[0].name).toBe("State:CA.OneCat");
        expect(getBundleForEmptyDimension("TwoCat").evaluation.rules[0].name).toBe("State:CA.TwoCat");
        expect(getBundleForEmptyDimension("ThreeCat").evaluation.rules[0].name).toBe("State:CA.ThreeCat");
    });
    it("should throw on non existing EntryPoint name", () => {
        const getBundleForEmptyDimension = cache.getBundleForDimension({});
        expect(() => getBundleForEmptyDimension("NonExistingName")).toThrowError("NonExistingName");
    });
    it("should throw on non existing dimensions name", () => {
        expect(() => cache.getBundleForDimension({ non: "existing" })).toThrowError("existing");
    });
    it("should check if some artifacts are present in cache", () => {
        expect(cache.isCached("OneCat", { state: "CA", code: "000" })).toBeTruthy();
        expect(cache.isCached("TwoCat", { state: "CA", code: "000" })).toBeTruthy();
        expect(cache.isCached("OneCat", { code: "000", state: "CA" })).toBeTruthy();
        expect(cache.isCached("TwoCat", { code: "000", state: "CA" })).toBeTruthy();
        expect(cache.isCached("OneCat", { state: "NO" })).toBeFalsy();
    });
    it("should add bundle to existing cache if non closure function was called", () => {
        cache.addBundleForDimension({ a: "a" })({
            entryPointName: "EP1",
            entryPointBundle: MockBundleBuilder.builder()
                .addRule(rule("EP1.ThreeCat"))
                .build()
        });
        cache.addBundleForDimension({ a: "a" })({
            entryPointName: "EP2",
            entryPointBundle: MockBundleBuilder.builder()
                .addRule(rule("EP2.ThreeCat"))
                .build()
        });
        expect(cache.getBundleForDimension({ a: "a" })("EP2")).toBeDefined();
    });
});
