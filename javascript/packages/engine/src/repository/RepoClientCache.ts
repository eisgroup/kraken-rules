/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
import equal from "fast-deep-equal";

import { EntryPointBundle as Bundle } from "../models/EntryPointBundle";
import EntryPointBundle = Bundle.EntryPointBundle;
import EntryPointEvaluation = Bundle.EntryPointEvaluation;
import { Rule } from "kraken-model";
import { ErrorCode, KrakenRuntimeError } from "../error/KrakenRuntimeError";

export type BundleMap = { [entryPointName: string]: EntryPointBundle };

export interface Bundle {
    entryPointName: string;
    entryPointBundle: EntryPointBundle;
}

export interface CachedDimensionArtifacts {
    dimensions: Record<string, unknown>;
    bundles: BundleMap;
}

/**
 * Class to cache {@link EntryPointBundle}s by dimension values and entrypoint name.
 */
export class RepoClientCache {

    #cache: CachedDimensionArtifacts[] = [];
    #staticRulesCache: Map<string, Rule[]> = new Map();

    /**
     * Creates an instance of RepoClientCache.
     * @param {boolean} [isDeltaEnabled=false]
     * @since 1.0.41
     * if *true*, then
     * cache will save static rules for each entrypoint.
     * Later these static rules will be appended to the {@link EntryPointBundle.EntryPointBundle}
     * Static and dimensional rules will be sorted after concatenation.
     * if *false*, which is default, bundles will be saved as is and not appended.
     * @memberof RepoClientCache
     */
    constructor(private readonly isDeltaEnabled: boolean = false) { }

    /**
     * In case when repository created with 'delta' feature, when static rules are cached,
     * this method lets to check are rules cached by entryPoint name.
     *
     * @param {string} entryPointName   to check for static rules presence
     * @returns {boolean}               if *true* static rules are cached for this entrypoint
     * @memberof RepoClientCache
     * @since 1.0.41
     */
    areCachedStaticRules(entryPointName: string): boolean {
        return this.#staticRulesCache.has(entryPointName);
    }

    /**
     * Adds {@link EntryPointBundle} for dimensions from params. Returns closure with
     * dimensions to reuse it, while adding bundles for every EntryPoint.
     *
     * @param {MapString<string>} dimension     dimensions values
     * @return {(bundle: Bundle) => void}       function to add bundle with EntryPoint name
     *
     * @see {@link RepoClientCache#addBundleForDimension}
     */
    addBundleForDimension(dimension: Record<string, unknown>): (bundle: Bundle) => void {
        const cachedArtifact = this.findArtifactWithDimension(dimension);
        if (cachedArtifact) {
            return this.addBundle(cachedArtifact);
        }
        const dimensionArtifacts = { dimensions: dimension, bundles: {} };
        this.#cache.push(dimensionArtifacts);
        return this.addBundle(dimensionArtifacts);
    }

    /**
     * Finds {@link EntryPointBundle} by dimensions and EntryPoint name. Returns closure with
     * dimensions to get bundles by EntryPoint name.
     * @param {Record<string, unknown>} dimension     dimension values
     * @return {(entryPointName: string) => RepositoryApi.Bundle.EntryPointBundle}
     *                                          function to find bundle by EntryPoint name
     */
    getBundleForDimension(dimension: Record<string, unknown>): (entryPointName: string) => EntryPointBundle {
        const cachedArtifact = this.findArtifactWithDimension(dimension);
        if (cachedArtifact) {
            return this.getBundle(cachedArtifact);
        }
        throw new KrakenRuntimeError(
            ErrorCode.NO_BUNDLE_BY_DIMENSIONS,
            "No Entry point bundles found with dimensions:\n " + JSON.stringify(dimension, undefined, 2)
        );
    }

    /**
     * Checks does some artifacts with dimensions from parameters are present.
     * @param {MapString<string>} dimensions    dimension values
     * @return {boolean}                        does contain cache for dimensions from parameters
     */
    isCached(entryPointName: string, dimensions: Record<string, unknown>): boolean {
        const cached = this.findArtifactWithDimension(dimensions);
        return Boolean(cached && cached.bundles && cached.bundles[entryPointName]);
    }

    /**
     * Invalidates cache by all dimensions and for all entrypoints.
     *
     * @since 1.0.39
     */
    clearCache(): void {
        this.#cache = [];
        this.#staticRulesCache.clear();
    }

    private getBundle(
        cachedDimensionArtifacts: CachedDimensionArtifacts
    ): (entryPointName: string) => EntryPointBundle {
        return function _getBundle(entryPointName: string): EntryPointBundle {
            const bundle = cachedDimensionArtifacts.bundles[entryPointName];
            if (bundle) {
                return bundle;
            }
            throw new KrakenRuntimeError(
                ErrorCode.NO_BUNDLE_BY_ENTRYPOINT,
                "No Entry point bundle found with Entry point name: " + entryPointName
            );
        };
    }

    private addBundle(cachedDimensionArtifacts: CachedDimensionArtifacts): (bundle: Bundle) => void {
        function _addBundle(this: RepoClientCache, bundle: Bundle): void {
            let entryPointBundle = bundle.entryPointBundle;
            if (this.isDeltaEnabled) {
                if (this.#staticRulesCache.has(bundle.entryPointName)) {
                    const staticRules =
                        this.#staticRulesCache.get(bundle.entryPointName)!;
                    const dimensionalRules =
                        bundle.entryPointBundle.evaluation.rules.filter(rule => rule.dimensional);
                    entryPointBundle = {
                        engineVersion: bundle.entryPointBundle.engineVersion,
                        evaluation: {
                            delta: entryPointBundle.evaluation.delta,
                            rules: dimensionalRules
                                .concat(staticRules)
                                .sort(getOrderComparator(bundle.entryPointBundle.evaluation.rulesOrder)),
                            entryPointName: bundle.entryPointName,
                            rulesOrder: bundle.entryPointBundle.evaluation.rulesOrder
                        },
                        expressionContext: bundle.entryPointBundle.expressionContext
                    };
                } else {
                    const staticRules =
                        bundle.entryPointBundle.evaluation.rules.filter(rule => !rule.dimensional);
                    this.#staticRulesCache.set(bundle.entryPointName, staticRules);
                }
            }
            cachedDimensionArtifacts.bundles[bundle.entryPointName] = entryPointBundle;
        }
        return _addBundle.bind(this);
    }

    private findArtifactWithDimension(dimension: Record<string, unknown>): CachedDimensionArtifacts | undefined {
        return this.#cache.find(a => equal(a.dimensions, dimension));
    }

}

function getOrderComparator(order: EntryPointEvaluation["rulesOrder"]): (r1: Rule, r2: Rule) => number {
    return function _compareRulesOrder(r1: Rule, r2: Rule): number {
        const order1 = order[r1.name];
        const order2 = order[r2.name];
        if (order1 === undefined && order2 === undefined) {
            return 0;
        }
        if (order1 === undefined) {
            return 1;
        }
        if (order2 === undefined) {
            return -1;
        }
        return order1 < order2 ? -1 : 1;
    };
}
