import { Rule } from 'kraken-model'
import {
    SystemMessageBuilder,
    KrakenRuntimeError,
    NO_BUNDLE_CACHE_BY_ENTRYPOINT_AND_DIMENSIONS,
    NO_BUNDLE_CACHE_BY_ENTRYPOINT,
} from '../../error/KrakenRuntimeError'
import { EntryPointBundle } from '../../models/EntryPointBundle'
import { DimensionSetCache } from './DimensionSetCache'
import { ExpressionContext, ExpressionContextManager } from '../expression-context-manager/ExpressionContextManager'
import { Dimensions, DimensionSet, EntryPointBundleCache } from '../EntryPointBundleCache'
import { Cache } from '../Cache'
import { DimensionSetCacheLogger } from './DimensionSetCacheLogger'
import { Reducer as r } from 'declarative-js'

type EntryPointName = string

export class DimensionSetBundleCache implements EntryPointBundleCache, ExpressionContextManager, Cache {
    constructor(logger: DimensionSetCacheLogger, expressionContextManager: ExpressionContextManager) {
        this.#logger = logger
        this.#expressionContextManager = expressionContextManager
    }

    #bundleCache = new BundleCache()
    #rulesCache = new Map<EntryPointName, DimensionSetCache>()
    #logger: DimensionSetCacheLogger
    #expressionContextManager: ExpressionContextManager

    clearCache(): void {
        this.#expressionContextManager.clearExpressionContext()
        this.#bundleCache.clear()
        this.#rulesCache.clear()
    }

    add(entryPointName: string, dimensions: Dimensions, bundle: EntryPointBundle.EntryPointBundle): void {
        const entryPointCache = this.#rulesCache.get(entryPointName) ?? new DimensionSetCache(this.#logger)
        entryPointCache.add(dimensions, bundle.evaluation.rules)
        this.#rulesCache.set(entryPointName, entryPointCache)
        this.#bundleCache.set(entryPointName, bundle, dimensions)
    }
    get(entryPointName: string, dimensions: Dimensions): EntryPointBundle.EntryPointBundle {
        const entryPointCache = this.#rulesCache.get(entryPointName)
        if (!entryPointCache) {
            const m = new SystemMessageBuilder(NO_BUNDLE_CACHE_BY_ENTRYPOINT).parameters(entryPointName).build()
            throw new KrakenRuntimeError(m)
        }

        if (!entryPointCache.isCached(dimensions)) {
            const m = new SystemMessageBuilder(NO_BUNDLE_CACHE_BY_ENTRYPOINT_AND_DIMENSIONS)
                .parameters(entryPointName, dimensions)
                .build()
            throw new KrakenRuntimeError(m)
        }

        const rules = entryPointCache.get(dimensions)
        const expressionContext = this.#expressionContextManager.getExpressionContext()
        return this.#bundleCache.get(entryPointName, rules, expressionContext, dimensions)
    }
    calculateExcludes(entryPointName: string, dimensions: Dimensions): DimensionSet[] {
        const cache = this.#rulesCache.get(entryPointName)
        if (!cache) {
            return []
        }
        return cache.calculateExcludes(dimensions)
    }
    isCached(entryPointName: string, dimensions: Dimensions): boolean {
        return (
            this.#bundleCache.has(entryPointName, dimensions) &&
            this.#rulesCache.has(entryPointName) &&
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            this.#rulesCache.get(entryPointName)!.isCached(dimensions)
        )
    }
    clearRulesCache(): void {
        this.#bundleCache.clear()
        for (const cache of this.#rulesCache.values()) {
            cache.clear()
        }
        this.#rulesCache.clear()
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
    getExpressionContext(): ExpressionContext {
        return this.#expressionContextManager.getExpressionContext()
    }
}

class BundleCache {
    // key is: entrypoint name-sorted dimension values
    private cache = new Map<string, EntryPointBundle.EntryPointBundle>()

    set(entryPointName: string, bundle: EntryPointBundle.EntryPointBundle, dimensions: Record<string, unknown>): void {
        this.cache.set(this.getKey(entryPointName, dimensions), bundle)
    }

    get(
        entryPointName: string,
        rules: Rule[],
        expressionContext: ExpressionContext,
        dimensions: Record<string, unknown>,
    ): EntryPointBundle.EntryPointBundle {
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        const bundle = this.cache.get(this.getKey(entryPointName, dimensions))!
        return {
            ...bundle,
            expressionContext,
            evaluation: {
                ...bundle.evaluation,
                rules,
            },
        }
    }

    has(entryPointName: string, dimensions: Record<string, unknown>): boolean {
        return this.cache.has(this.getKey(entryPointName, dimensions))
    }
    clear(): void {
        this.cache.clear()
    }

    private getKey(entryPointName: string, dimensions: Record<string, unknown>): string {
        const allDimensionKeys = Object.keys(dimensions)
        const sorted = allDimensionKeys.sort().reduce(
            r.toObject(
                x => x,
                x => dimensions[x],
            ),
            {},
        )
        return `${entryPointName}-${JSON.stringify(sorted)}`
    }
}
