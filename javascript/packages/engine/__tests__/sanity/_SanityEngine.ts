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

import { mock } from "../mock";
import { readdirSync } from "fs";
import { RepoClientCache } from "../../src/repository/RepoClientCache";
import { SyncEngine, EvaluationConfig } from "../../src/engine/executer/SyncEngine";
import { EntryPointName } from "./_SanityEntryPointNames";
import { sanityMocks } from "./_AutoPolicyObject.mocks";
import { EntryPointBundle } from "../../src/models/EntryPointBundle";
import { EntryPointResult } from "../../src/dto/EntryPointResult";
import { TestProduct } from "kraken-test-product";
import { registry } from "../../src/engine/runtime/expressions/ExpressionEvaluator";

import AutoPolicySummary = TestProduct.kraken.testproduct.domain.Policy;
import { SANITY_DIMENSIONS } from "./_SanityDimensions";
import { ContextModelTree } from "../../src";

function isJsonName(fileName: string): boolean {
    return fileName.indexOf(".json") !== -1;
}

const sanityBundles = readdirSync("__tests__/sanity/__bundles__")
    .filter(isJsonName)
    .map(x => require("./__bundles__/" + x)) as EntryPointBundle.EntryPointBundle[];

const cache = (() => {
    const repoCache = new RepoClientCache(true);
    sanityBundles
        .sort((b1, b2) => (b1.evaluation.delta === b2.evaluation.delta) ? 0 : !b1.evaluation.delta ? -1 : 1)
        .forEach(entryPointBundle => {
            return repoCache.addBundleForDimension(entryPointBundle.expressionContext)({
                entryPointBundle,
                entryPointName: entryPointBundle.evaluation.entryPointName
            });
        });
    return repoCache;
})();

function createSanityEngine(namespace: string, modelTree: ContextModelTree.ContextModelTree): SanityEngine {
    const engine = new SyncEngine({
        contextInstanceInfoResolver: mock.spi.instance,
        cache: cache,
        dataInfoResolver: mock.spi.dataResolver,
        modelTree
    });
    return new SanityEngine(namespace, engine);
}

class SanityEngine {
    constructor(
        private readonly namespace: string,
        private readonly engine: SyncEngine
    ) { }

    private createEvaluationConfig(useCaseName: keyof typeof SANITY_DIMENSIONS): EvaluationConfig {
        return {
            context: { dimensions: SANITY_DIMENSIONS[useCaseName] },
            currencyCd: "EUR"
        };
    }

    evaluate(
        data: AutoPolicySummary,
        entryPoint: EntryPointName,
        useCaseName?: keyof typeof SANITY_DIMENSIONS
    ): EntryPointResult {
        return this.engine.evaluate(
            data,
            this.resolveEntryPointName(entryPoint),
            useCaseName ? this.createEvaluationConfig(useCaseName) : sanityMocks.evalConf()
        );
    }

    evaluateSubTree(
        data: object,
        node: object,
        entryPoint: EntryPointName,
        useCaseName?: keyof typeof SANITY_DIMENSIONS,
        options?: { evaluationId: string }
    ): EntryPointResult {
        const evaluationConfig = useCaseName
            ? this.createEvaluationConfig(useCaseName)
            : sanityMocks.evalConf();
        return this.engine.evaluateSubTree(
            data,
            node,
            this.resolveEntryPointName(entryPoint),
            { ...evaluationConfig, evaluationId: options?.evaluationId }
        );
    }

    private resolveEntryPointName(entryPointName: string): string {
        return `${this.namespace}:${entryPointName}`;
    }
}

registry.add({
    name: "GetCarCoverage",
    function: function GetCarCoverage(policySummary: AutoPolicySummary): object | undefined {
        return policySummary.coverage;
    }
});

export const sanityEngine = createSanityEngine("Policy", mock.modelTree);
export const sanityEngineExtendedPolicy = createSanityEngine("PolicyExtended", mock.extendedModelTree);
