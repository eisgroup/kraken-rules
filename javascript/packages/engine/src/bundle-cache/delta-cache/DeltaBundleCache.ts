import { EntryPointBundle } from '../../models/EntryPointBundle'
import { Dimensions, DimensionSet, EntryPointBundleCache } from '../EntryPointBundleCache'
import { RepoClientCache } from './RepoClientCache'
import { ExpressionContext } from '../expression-context-manager/ExpressionContextManager'
import { Cache } from '../Cache'

export class DeltaBundleCache implements EntryPointBundleCache, Cache {
    constructor(private readonly deltaCache: RepoClientCache) {}
    add(entryPointName: string, dimensions: Dimensions, bundle: EntryPointBundle.EntryPointBundle): void {
        this.deltaCache.addBundleForDimension(dimensions)({
            entryPointName,
            evaluation: bundle.evaluation,
            engineVersion: bundle.engineVersion,
        })
    }
    get(entryPointName: string, dimensions: Dimensions): EntryPointBundle.EntryPointBundle {
        return this.deltaCache.getBundleForDimension(dimensions)(entryPointName)
    }
    clearRulesCache(): void {
        this.deltaCache.clearRulesCache()
    }
    clearCache(): void {
        this.deltaCache.clearCache()
    }
    isCached(entryPointName: string, dimensions: Dimensions): boolean {
        return this.deltaCache.isCached(entryPointName, dimensions)
    }
    calculateExcludes(entryPointName: string, _dimensions: Dimensions): DimensionSet[] {
        if (this.deltaCache.areCachedStaticRules(entryPointName)) {
            return [[]]
        }
        return []
    }
    clearExpressionContext(): void {
        this.deltaCache.clearExpressionContext()
    }
    isExpressionContextPresent(): boolean {
        return this.deltaCache.isExpressionContextPresent()
    }
    getExpressionContext(): ExpressionContext {
        return this.deltaCache.getExpressionContext()
    }
    setExpressionContext(expressionContext: ExpressionContext): void {
        this.deltaCache.setExpressionContext(expressionContext)
    }
}
