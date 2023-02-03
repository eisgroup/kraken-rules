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
import equal from 'fast-deep-equal'

import { EntryPointBundle as Bundle } from '../../models/EntryPointBundle'
import EntryPointBundle = Bundle.EntryPointBundle
import EntryPointEvaluation = Bundle.EntryPointEvaluation
import { Rule } from 'kraken-model'
import { ErrorCode, KrakenRuntimeError } from '../../error/KrakenRuntimeError'
import { ExpressionContextManagerImpl } from '../expression-context-manager/ExpressionContextManagerImpl'
import { ExpressionContext, ExpressionContextManager } from '../expression-context-manager/ExpressionContextManager'
import { Cache } from '../Cache'

export type BundleMap = { [entryPointName: string]: Bundle }

export interface Bundle {
    entryPointName: string
    engineVersion?: string
    evaluation: EntryPointEvaluation
}

export interface CachedDimensionArtifacts {
    dimensions: Record<string, unknown>
    bundles: BundleMap
}

/**
 * Class to cache {@link EntryPointBundle}s by dimension values and entrypoint name.
 */
export class RepoClientCache implements ExpressionContextManager, Cache {
    #cache: CachedDimensionArtifacts[] = []
    #staticRulesCache: Map<string, Rule[]> = new Map()
    #expressionContextManager = new ExpressionContextManagerImpl()
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
    constructor(private readonly isDeltaEnabled: boolean = false) {}

    getExpressionContext(): ExpressionContext {
        return this.#expressionContextManager.getExpressionContext()
    }

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
        return this.#staticRulesCache.has(entryPointName)
    }

    setExpressionContext(expressionContext: Record<string, unknown>): void {
        this.#expressionContextManager.setExpressionContext(expressionContext)
    }
    clearExpressionContext(): void {
        this.#expressionContextManager.clearExpressionContext()
    }
    isExpressionContextPresent(): boolean {
        return this.#expressionContextManager.isExpressionContextPresent()
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
        const cachedArtifact = this.findArtifactWithDimension(dimension)
        if (cachedArtifact) {
            return this.addBundle(cachedArtifact)
        }
        const dimensionArtifacts = { dimensions: dimension, bundles: {} }
        this.#cache.push(dimensionArtifacts)
        return this.addBundle(dimensionArtifacts)
    }

    /**
     * Finds {@link EntryPointBundle} by dimensions and EntryPoint name. Returns closure with
     * dimensions to get bundles by EntryPoint name.
     * @param {Record<string, unknown>} dimension     dimension values
     * @return {(entryPointName: string) => RepositoryApi.Bundle.EntryPointBundle}
     *                                          function to find bundle by EntryPoint name
     */
    getBundleForDimension(dimension: Record<string, unknown>): (entryPointName: string) => EntryPointBundle {
        const cachedArtifact = this.findArtifactWithDimension(dimension)
        if (cachedArtifact) {
            return this.getBundle(cachedArtifact, this.#expressionContextManager.getExpressionContext())
        }
        throw new KrakenRuntimeError(
            ErrorCode.NO_BUNDLE_BY_DIMENSIONS,
            'No Entry point bundles found with dimensions:\n ' + JSON.stringify(dimension, undefined, 2),
        )
    }

    /**
     * Checks does some artifacts with dimensions from parameters are present.
     * @param {MapString<string>} dimensions    dimension values
     * @return {boolean}                        does contain cache for dimensions from parameters
     */
    isCached(entryPointName: string, dimensions: Record<string, unknown>): boolean {
        const cached = this.findArtifactWithDimension(dimensions)
        return Boolean(cached && cached.bundles && cached.bundles[entryPointName])
    }

    /**
     * Invalidates rules cache by all dimensions and for all entrypoints.
     * @since 1.38.0
     */
    clearRulesCache(): void {
        this.#cache = []
        this.#staticRulesCache.clear()
    }

    /**
     * Invalidates all kraken caches.
     * To have better control you can use clearRulesCache() or clearExpressionContext() instead.
     *
     * @since 1.0.39
     */
    clearCache(): void {
        this.clearExpressionContext()
        this.clearRulesCache()
    }

    private getBundle(
        cachedDimensionArtifacts: CachedDimensionArtifacts,
        expressionContext: Record<string, unknown> | undefined,
    ): (entryPointName: string) => EntryPointBundle {
        return function _getBundle(entryPointName: string): EntryPointBundle {
            const bundle = cachedDimensionArtifacts.bundles[entryPointName]
            if (bundle) {
                if (!expressionContext) {
                    throw new KrakenRuntimeError(ErrorCode.NO_EXPRESSION_CONTEXT, 'No expression context found')
                }
                return {
                    engineVersion: bundle.engineVersion,
                    evaluation: bundle.evaluation,
                    expressionContext: expressionContext,
                }
            }
            throw new KrakenRuntimeError(
                ErrorCode.NO_BUNDLE_BY_ENTRYPOINT,
                'No Entry point bundle found with Entry point name: ' + entryPointName,
            )
        }
    }

    private addBundle(cachedDimensionArtifacts: CachedDimensionArtifacts): (bundle: Bundle) => void {
        function _addBundle(this: RepoClientCache, bundle: Bundle): void {
            if (this.isDeltaEnabled) {
                if (this.#staticRulesCache.has(bundle.entryPointName)) {
                    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                    const staticRules = this.#staticRulesCache.get(bundle.entryPointName)!
                    const dimensionalRules = bundle.evaluation.rules.filter(
                        rule => rule.dimensionSet.variability !== 'STATIC',
                    )
                    bundle = {
                        engineVersion: bundle.engineVersion,
                        entryPointName: bundle.entryPointName,
                        evaluation: {
                            delta: bundle.evaluation.delta,
                            rules: dimensionalRules.concat(staticRules),
                            entryPointName: bundle.entryPointName,
                            fieldOrder: bundle.evaluation.fieldOrder,
                        },
                    }
                } else {
                    const staticRules = bundle.evaluation.rules.filter(
                        rule => rule.dimensionSet.variability === 'STATIC',
                    )
                    this.#staticRulesCache.set(bundle.entryPointName, staticRules)
                }
            }
            cachedDimensionArtifacts.bundles[bundle.entryPointName] = bundle
        }
        return _addBundle.bind(this)
    }

    private findArtifactWithDimension(dimension: Record<string, unknown>): CachedDimensionArtifacts | undefined {
        return this.#cache.find(a => equal(a.dimensions, dimension))
    }
}
