import { isKnownDimensionSet, Rule } from 'kraken-model'
import { Reducer as r } from 'declarative-js'
import { DimensionSetCacheLogger } from './DimensionSetCacheLogger'

export type DimensionName = string
export type DimensionSet = DimensionName[]
export type DimensionValueSet = Record<DimensionName, unknown>

export class DimensionSetCache {
    constructor(logger: DimensionSetCacheLogger) {
        this.logger = logger
    }

    private logger: DimensionSetCacheLogger
    // number, {dvs -> rule[]}
    private cache = new Map<number, Map<string, Rule[]>>()
    // dimension value set stringified field
    private cachedDimensionValueSets = new Set<string>()
    private biteDimensions = new BitDimensions()
    private nonDimensionalRules: Rule[] = []
    private unknownDimensionsCache = new Map<string, Rule[]>()

    add(dimensions: DimensionValueSet, rules: Rule[]): void {
        const grouped = rules.reduce(
            r.groupBy(rule => {
                const dimensionSet = rule.dimensionSet
                if (isKnownDimensionSet(dimensionSet)) {
                    return String(this.getMask(dimensionSet.dimensions))
                }
                return 'Any dimensions'
            }),
            r.Map(),
        )
        for (const rules of grouped.values()) {
            const { dimensionSet } = rules[0]
            if (!isKnownDimensionSet(dimensionSet)) {
                this.unknownDimensionsCache.set(this.getSubCacheKey(dimensions), rules)
                continue
            }
            if (dimensionSet.variability === 'STATIC') {
                if (this.nonDimensionalRules.length) {
                    this.logger.logWarning('Overriding non static rules. This behavior is not expected.')
                }
                this.nonDimensionalRules = rules
                continue
            }
            // known dimensions
            const dimensionValueSet = this.getDimensionValueSet(dimensions, dimensionSet.dimensions)
            this.updateCache(dimensionValueSet, rules)
        }

        this.cachedDimensionValueSets.add(this.getSubCacheKey(dimensions))
    }

    get(dimensions: DimensionValueSet): Rule[] {
        const mask = this.getMask(Object.keys(dimensions))
        const rules = Array.from(this.cache.keys())
            .filter(cachedMask => BitDimensions.includes(cachedMask, mask))
            .map(cachedMask => {
                const dimensionSet = this.biteDimensions.getNames(cachedMask)
                const dimensionValueSet = this.getDimensionValueSet(dimensions, dimensionSet)
                const rulesByDimensions = this.cache.get(cachedMask)
                const cacheKey = this.getSubCacheKey(dimensionValueSet)
                return rulesByDimensions?.get(cacheKey) ?? []
            })
            .reduce(r.flat, [])

        const unknownRules = this.unknownDimensionsCache.get(this.getSubCacheKey(dimensions)) ?? []
        return rules.concat(this.nonDimensionalRules).concat(unknownRules)
    }

    isCached(dimensions: DimensionValueSet): boolean {
        return this.cachedDimensionValueSets.has(this.getSubCacheKey(dimensions))
    }

    calculateExcludes(dimensions: DimensionValueSet): DimensionSet[] {
        const mask = this.getMask(Object.keys(dimensions))
        const excludes = []
        for (const cachedMask of this.cache.keys()) {
            if (!BitDimensions.includes(cachedMask, mask) && !(BitDimensions.NO_DIMENSION === cachedMask)) {
                continue
            }
            const subCache = this.getSubCache(cachedMask)
            const dimensionSet = this.biteDimensions.getNames(cachedMask)
            const dimensionValueSet = this.getDimensionValueSet(dimensions, dimensionSet)
            const cacheKey = this.getSubCacheKey(dimensionValueSet)
            if (subCache.has(cacheKey)) {
                excludes.push(dimensionSet)
            }
        }
        if (this.nonDimensionalRules.length) {
            excludes.push([])
        }
        return excludes
    }

    clear(): void {
        this.cachedDimensionValueSets.clear()
        this.cache.clear()
        this.biteDimensions.clear()
        this.nonDimensionalRules = []
    }

    private updateCache(dimensions: DimensionValueSet, rules: Rule[]): void {
        const mask = this.getMask(Object.keys(dimensions))
        const subCache = this.getSubCache(mask)
        const cacheKey = this.getSubCacheKey(dimensions)
        if (subCache.has(cacheKey)) {
            this.logger.logWarning(`Overriding rules by key '${cacheKey}'. This behavior is not expected.`)
        }
        subCache.set(cacheKey, rules)
        this.cache.set(mask, subCache)
    }

    private getSubCache(mask: number): Map<string, Rule[]> {
        if (this.cache.has(mask)) {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            return this.cache.get(mask)!
        }
        return new Map()
    }

    private getDimensionValueSet(dimensions: DimensionValueSet, set: DimensionSet): DimensionValueSet {
        const valueSubset: DimensionValueSet = {}
        for (const name of set) {
            valueSubset[name] = dimensions[name]
        }
        return valueSubset
    }

    private getSubCacheKey(dimensions: DimensionValueSet) {
        return JSON.stringify(this.getSortedDimensions(dimensions))
    }

    private getSortedDimensions(dimensions: DimensionValueSet) {
        const allDimensionKeys = Object.keys(dimensions)
        return allDimensionKeys.sort().reduce(
            r.toObject(
                x => x,
                x => dimensions[x],
            ),
            {},
        )
    }

    private getMask(dimensionSet: DimensionSet): number {
        return dimensionSet
            .map(d => this.biteDimensions.getBit(d))
            .reduce(BitDimensions.buildMask, BitDimensions.NO_DIMENSION)
    }
}

class BitDimensions {
    static NO_DIMENSION = 0

    static includes(srcMask: number, inMask: number): boolean {
        return srcMask === inMask || !!(srcMask & inMask)
    }

    static buildMask(a: number, b: number): number {
        return a | b
    }

    private keys = new Set<string>()
    private nameToBit: Map<string, number> = new Map()

    getBit(key: string): number {
        if (this.keys.has(key)) {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            return this.nameToBit.get(key)!
        }
        const byte = 1 << this.keys.size
        this.nameToBit.set(key, byte)
        this.keys.add(key)
        return byte
    }

    getNames(mask: number) {
        const names: string[] = []
        this.keys.forEach(key => {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            if (BitDimensions.includes(this.nameToBit.get(key)!, mask)) {
                names.push(key)
            }
        })
        return names
    }

    clear(): void {
        this.keys.clear()
        this.nameToBit.clear()
    }
}
