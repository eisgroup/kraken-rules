import { EntryPointBundle } from '../models/EntryPointBundle'
import { Cache } from './Cache'
import { ExpressionContextManager } from './expression-context-manager/ExpressionContextManager'

export type DimensionSet = string[]
export type Dimensions = Record<string, unknown>
export const ruleTimeZoneIdDimension = '__ruleTimeZoneId__'

/**
 * Cache to manage {@link EntryPointBundle}
 * @private currently is not released
 */
export interface EntryPointBundleCache extends ExpressionContextManager, Cache {
    add(entryPointName: string, dimensions: Dimensions, bundle: EntryPointBundle.EntryPointBundle): void
    get(entryPointName: string, dimensions: Dimensions): EntryPointBundle.EntryPointBundle
    calculateExcludes(entryPointName: string, dimensions: Dimensions): DimensionSet[]
    isCached(entryPointName: string, dimensions: Dimensions): boolean
    clearRulesCache(): void
}
