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

import { SyncEngine } from "../../../src/engine/executer/SyncEngine";
import { mock } from "../../mock";
import { RepoClientCache } from "../../../src/repository/RepoClientCache";
import { registry } from "../../../src/engine/runtime/expressions/ExpressionEvaluator";
import { EntryPointBundle } from "../../../src/models/EntryPointBundle";
import { RulesBuilder, PayloadBuilder } from "kraken-model-builder";
import { AssertionPayloadResult } from "../../../src/engine/results/PayloadResult";
import { RuleEvaluationResults } from "../../../src/dto/RuleEvaluationResults";

let engine: SyncEngine;

const customFunctionName = "Log";
const customFunction = jest.fn();
const Policy = mock.modelTreeJson.contexts.Policy;
const CreditCardInfo = mock.modelTreeJson.contexts.CreditCardInfo;

beforeEach(() => {
    const cache = new RepoClientCache();
    const addBundleForDefaultDimension = cache.addBundleForDimension({});
    addBundleForDefaultDimension({
        entryPointName: "Empty",
        entryPointBundle: {
            engineVersion: "1",
            "evaluation": {
                delta: false,
                rules: [], entryPointName: "CreditCardInfo", rulesOrder: {}
            },
            "expressionContext": {}
        } as EntryPointBundle.EntryPointBundle
    });
    addBundleForDefaultDimension({
        entryPointName: "CreditCardInfo",
        entryPointBundle: {
            engineVersion: "1",
            "evaluation": {
                delta: false,
                rulesOrder: {
                    r01: 0,
                    r02: 1
                },
                rules: [
                    new RulesBuilder()
                        .setContext(CreditCardInfo.name)
                        .setName("r01")
                        .setTargetPath(CreditCardInfo.fields.cardNumber.name)
                        .setPayload(PayloadBuilder.accessibility().notAccessible())
                        .build(),
                    new RulesBuilder()
                        .setContext(CreditCardInfo.name)
                        .setName("r02")
                        .setTargetPath(CreditCardInfo.fields.cardNumber.name)
                        .setPayload(PayloadBuilder.visibility().notVisible())
                        .build()
                ],
                entryPointName: "CreditCardInfo"
            },
            "expressionContext": {}
        } as EntryPointBundle.EntryPointBundle
    });
    addBundleForDefaultDimension({
        entryPointName: "CustomFunction",
        entryPointBundle: {
            engineVersion: "1",
            evaluation: {
                delta: false,
                rulesOrder: {
                    customFunction: 0
                },
                entryPointName: "CustomFunction",
                rules: [
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName("customFunction")
                        .setTargetPath(Policy.fields.state.name)
                        .setPayload(PayloadBuilder.asserts().that(customFunctionName + "()"))
                        .build()
                ]
            },
            expressionContext: {}
        }
    });
    const instanceExpression = "_t(__dataObject__, 'Policy') " +
        "&& _i(__dataObject__, 'Policy') " +
        "&& GetType(__dataObject__) == 'Policy'";
    addBundleForDefaultDimension({
        entryPointName: "InstanceFunctions",
        entryPointBundle: {
            engineVersion: "1",
            evaluation: {
                delta: false,
                entryPointName: "InstanceFunctions",
                rules: [
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName("R-InstanceFunctions")
                        .setTargetPath(Policy.fields.state.name)
                        .setPayload(PayloadBuilder.asserts().that(instanceExpression))
                        .build()
                ],
                rulesOrder: {
                    "R-InstanceFunctions": 0
                }
            },
            expressionContext: {}
        }
    });
    addBundleForDefaultDimension({
        entryPointName: "ExternalContext",
        entryPointBundle: {
            engineVersion: "1",
            evaluation: {
                delta: false,
                rulesOrder: {
                    "ExternalContext": 0
                },
                entryPointName: "ExternalContext",
                rules: [
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName("ExternalContext")
                        .setTargetPath(Policy.fields.state.name)
                        .setPayload(PayloadBuilder.asserts().that("context.external.key == 'value'"))
                        .build()
                ]
            },
            expressionContext: {}
        }
    });
    engine = new SyncEngine({
        cache: cache,
        dataInfoResolver: mock.spi.dataResolver,
        contextInstanceInfoResolver: mock.spi.instance,
        modelTree: mock.modelTree
    });
});

describe("SyncEngine", () => {
    it("should evaluate rule with external data", () => {
        const results = engine.evaluate(
            mock.data.empty(),
            "ExternalContext",
            {
                currencyCd: "USD",
                context: {
                    externalData: {
                        key: "value"
                    }
                }
            });
        const payloadResult = (
            (
                results.getAllRuleResults()[0] as RuleEvaluationResults.ValidationRuleEvaluationResult
            )
                .payloadResult as AssertionPayloadResult
        );
        expect(payloadResult.error).toBeUndefined();
        expect(payloadResult.success).toBeTruthy();
    });
    it("should evaluate instance functions", () => {
        const results = engine.evaluate(
            mock.data.empty(),
            "InstanceFunctions",
            mock.evaluationConfig
        );
        expect(results).k_toHaveExpressionsFailures(0);
    });
    it("should add custom function to registry and execute it", () => {
        registry.add({
            name: customFunctionName,
            function: customFunction
        });
        engine.evaluate(
            mock.data.empty(),
            "CustomFunction",
            mock.evaluationConfig
        );
        expect(customFunction).toHaveBeenCalledTimes(1);
    });
    it("should throw an error when no currency code is provided", () => {
        expect(() => engine.evaluate({}, "", { currencyCd: "", context: {} })).toThrow();
    });
    it("should when restriction is invalid Context instance", () => {
        expect(() => engine.evaluateSubTree({}, { cd: "", noid: 0 }, "", mock.evaluationConfig))
            .toThrow("Restriction node is invalid Context instance");
    });
    it("should evaluate with empty entryPoint bundle", () => {
        const result = engine.evaluate(mock.data.empty(),
            "Empty",
            mock.evaluationConfig);
        expect(result.getAllRuleResults().length).toBe(0);
        expect(Object.keys(result.getFieldResults()).length).toBe(0);
    });
    it("should evaluate 2 rules on same context", () => {
        const data = mock.data.dataContextCustom({
            billingInfo: {
                creditCardInfo: {
                    cd: mock.modelTreeJson.contexts.CreditCardInfo.name,
                    id: "cci1"
                }
            }
        });
        const results = engine.evaluate(data.dataObject, "CreditCardInfo", mock.evaluationConfig);
        expect(Object.keys(results.getFieldResults())).toHaveLength(1);
        const id = `${CreditCardInfo.name}:cci1:${CreditCardInfo.fields.cardNumber.name}`;
        expect(results.getFieldResults()[id]).toBeDefined();
        expect(results.getFieldResults()[id].ruleResults).toHaveLength(2);
    });
    // run this test when console.setup.js configuration is disabled
    // this feature can be tested only with logs ;\
    it("evaluation id usage", () => {
        const data = mock.data.dataContextCustom({
            billingInfo: {
                creditCardInfo: {
                    cd: mock.modelTreeJson.contexts.CreditCardInfo.name,
                    id: "cci1"
                }
            }
        });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig, evaluationId: "11" });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig, evaluationId: "11" });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig, evaluationId: "12" });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig, evaluationId: "12" });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig, evaluationId: "11" });
        engine.evaluate(data.dataObject, "CreditCardInfo", { ...mock.evaluationConfig, evaluationId: "11" });
    });
});
