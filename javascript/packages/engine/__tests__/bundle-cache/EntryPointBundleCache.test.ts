import { Rule } from 'kraken-model'
import { RegExpPayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { DeltaBundleCache } from '../../src/bundle-cache/delta-cache/DeltaBundleCache'
import { RepoClientCache } from '../../src/bundle-cache/delta-cache/RepoClientCache'
import { DimensionSetBundleCache } from '../../src/bundle-cache/dimension-set-cache/DimensionSetBundleCache'
import { DimensionValueSet } from '../../src/bundle-cache/dimension-set-cache/DimensionSetCache'
import { EntryPointBundleCache } from '../../src/bundle-cache/EntryPointBundleCache'
import { ExpressionContextManagerImpl } from '../../src/bundle-cache/expression-context-manager/ExpressionContextManagerImpl'

const delta = { name: 'delta' as const, getCache: () => new DeltaBundleCache(new RepoClientCache(true)) }
const set = {
    name: 'dimension set' as const,
    getCache: () => new DimensionSetBundleCache({ logWarning: () => void 0 }, new ExpressionContextManagerImpl()),
}

type TestVariation = { name: 'delta' | 'dimension set'; getCache: () => EntryPointBundleCache }
const testVariations: TestVariation[] = [delta, set]

describe('EntryPointBundleCache', () => {
    it(`delta should fail on non cached rules by dimensions`, () => {
        const entryPointName = 'ep'
        const cache = delta.getCache()
        const bundle = getBundle({ entryPointName })
        cache.add(entryPointName, {}, bundle)
        expect(() => cache.get(entryPointName, { a: 'a' })).toThrowError('kus003')
    })
    it(`dimension set should fail on non cached rules by dimensions`, () => {
        const entryPointName = 'ep'
        const cache = set.getCache()
        const bundle = getBundle({ entryPointName })
        cache.add(entryPointName, {}, bundle)
        expect(() => cache.get(entryPointName, { a: 'a' })).toThrowError('kus001')
    })
})

// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
describe.each(testVariations!)('EntryPointBundleCache', ({ name, getCache }) => {
    it(`[${name}] should pass sanity test with most common functionality`, () => {
        const entryPointName = 'ep'
        const cache = getCache()
        const dimensions = { plan: 'basic' }
        const bundle = getBundle({ entryPointName })
        cache.setExpressionContext({})
        cache.add(entryPointName, dimensions, bundle)

        expect(cache.get(entryPointName, dimensions)).toStrictEqual(bundle)
        expect(cache.isCached(entryPointName, dimensions)).toBe(true)
        expect(cache.calculateExcludes(entryPointName, dimensions)).toStrictEqual([[]])
        expect(cache.isExpressionContextPresent()).toBe(true)
        cache.clearExpressionContext()
        expect(cache.isExpressionContextPresent()).toBe(false)
        cache.clearRulesCache()
        expect(cache.isCached(entryPointName, dimensions)).toBe(false)
    })
    it(`[${name}] should not loose bundle order info for the same entry point name`, () => {
        const entryPointName = 'ep'
        const cache = getCache()
        cache.setExpressionContext({})
        cache.add(entryPointName, {}, getBundle({ entryPointName, fieldOrder: ['e.a'] }))
        cache.add(entryPointName, { state: 'ca' }, getBundle({ entryPointName, fieldOrder: ['e.a', 'state.ca'] }))
        const bundleState = cache.get(entryPointName, { state: 'ca' })
        const bundleStatic = cache.get(entryPointName, {})

        expect(bundleState.evaluation.fieldOrder).toContain('e.a')
        expect(bundleState.evaluation.fieldOrder).toContain('state.ca')
        expect(bundleStatic.evaluation.fieldOrder).toContain('e.a')
        expect(bundleStatic.evaluation.fieldOrder).not.toContain('state.ca')
    })
    it(`[${name}] should fail on non cached rules by entrypoint name`, () => {
        const entryPointName = 'ep'
        const cache = getCache()
        const bundle = getBundle({ entryPointName })
        cache.setExpressionContext({})
        cache.add(entryPointName, {}, bundle)
        expect(() => cache.get('noop', {})).toThrowError('kus002')
    })
    it(`[${name}] should fail on non cached expression context`, () => {
        const entryPointName = 'ep'
        const cache = getCache()
        const bundle = getBundle({ entryPointName })
        cache.add(entryPointName, {}, bundle)
        expect(() => cache.get(entryPointName, {})).toThrowError('kus013')
    })
    it(`[${name}] should calculate no excludes`, () => {
        const cache = getCache()
        const excludes = cache.calculateExcludes('noop', {})
        expect(excludes).toHaveLength(0)
    })
    it(`[${name}] clear all caches`, () => {
        const entryPointName = 'ep'
        const cache = getCache()
        cache.add(entryPointName, {}, getBundle({ entryPointName, rules: [r()] }))
        cache.add(entryPointName, { plan: 'basic' }, getBundle({ entryPointName, rules: [r('plan')] }))
        cache.setExpressionContext({})

        cache.clearCache()

        expect(cache.isCached(entryPointName, {})).toBe(false)
        expect(cache.isExpressionContextPresent()).toBe(false)
        expect(() => cache.get(entryPointName, {})).toThrow()
        expect(() => cache.getExpressionContext()).toThrow()
    })
    it(`[${name}] should calculate excludes`, () => {
        const entryPointName = 'ep'
        const cache = getCache()
        cache.add(entryPointName, {}, getBundle({ entryPointName, rules: [r()] }))
        cache.add(entryPointName, { plan: 'basic' }, getBundle({ entryPointName, rules: [r('plan')] }))
        const excludes = cache.calculateExcludes(entryPointName, { plan: 'basic', state: 'ca' })

        // this is what different in implementations
        // dimension set cache calculates more accurate excludes
        switch (name) {
            case 'delta':
                expect(excludes).toHaveLength(1)
                expect(excludes).toContainEqual([])
                break
            case 'dimension set':
                expect(excludes).toHaveLength(2)
                expect(excludes).toContainEqual([])
                expect(excludes).toContainEqual(['plan'])
                break
        }
    })
    // remove '.skip' to compare values in the console
    describe.skip.each([
        [10, 1],
        [100, 10],
        [1000, 100],
    ])(`[${name}] performance (bundle count - %d, rules per bundle - %d)`, (bundleCount, rulesCount) => {
        it(`add`, () => {
            const entryPointName = 'ep'
            const cache = getCache()

            function arr(length: number): unknown[] {
                return Array.from({ length })
            }
            const sets = arr(bundleCount).map(perf.dimension.generateDimensionValueSet)
            for (const set of sets) {
                const rules = arr(rulesCount).map(() => perf.rule.generateRule(set))
                cache.add(entryPointName, set, getBundle({ entryPointName, rules }))
            }
        })
        it(`add/calculateExcludes`, () => {
            const entryPointName = 'ep'
            const cache = getCache()

            function arr(length: number): unknown[] {
                return Array.from({ length })
            }
            const sets = arr(bundleCount).map(perf.dimension.generateDimensionValueSet)
            for (const set of sets) {
                const rules = arr(rulesCount).map(() => perf.rule.generateRule(set))
                cache.add(entryPointName, set, getBundle({ entryPointName, rules }))
            }
            cache.setExpressionContext({})

            for (const set of sets) {
                cache.calculateExcludes(entryPointName, set)
            }
        })
    })
})

function r(...dimensions: string[]): Rule {
    const name = dimensions.length ? dimensions.join('-') + Math.random() : String(Math.random())

    const rule = RulesBuilder.create()
        .setName(name)
        .setContext('Policy')
        .setTargetPath('country')
        .setDimensionSet(dimensions)
        .setPayload(new RegExpPayloadBuilder().match('.*'))
        .build()
    return rule
}

function getBundle({
    entryPointName,
    rules,
    fieldOrder,
}: {
    entryPointName: string
    rules?: Rule[]
    fieldOrder?: string[]
}) {
    const rule = r()
    const bundle = {
        evaluation: {
            delta: true,
            entryPointName,
            rules: rules ?? [rule],
            fieldOrder: fieldOrder ?? [],
        },
        expressionContext: {},
        engineVersion: '1',
    }
    return bundle
}

function any(rule: Rule): Rule {
    return {
        ...rule,
        dimensionSet: {
            variability: 'UNKNOWN',
        },
    }
}

namespace perf {
    export namespace rule {
        export function generateRule(dimensions: DimensionValueSet): Rule {
            const entries = Object.entries(dimensions).sort(() => Math.random())
            const length = perf.random.randomTo(entries.length - 1)
            const sliced = entries.slice(length - 1)

            if (perf.random.randomBool(0.15)) {
                return any(r())
            }

            if (perf.random.randomBool(0.15)) {
                return r()
            }

            return r(...sliced.map(e => e[0]))
        }
    }

    export namespace dimension {
        const values = {
            state: [
                'AL',
                'AK',
                'AZ',
                'AR',
                'CA',
                'CO',
                'CT',
                'DE',
                'FL',
                'GA',
                'HI',
                'ID',
                'IL',
                'IN',
                'IA',
                'KS',
                'KY',
                'LA',
                'ME',
                'MD',
                'MA',
                'MI',
                'MN',
                'MS',
                'MO',
                'MT',
                'NE',
                'NV',
                'NH',
                'NJ',
                'NM',
                'NY',
                'NC',
                'ND',
                'OH',
                'OK',
                'OR',
                'PA',
                'RI',
                'SC',
                'SD',
                'TN',
                'TX',
                'UT',
                'VT',
                'VA',
                'WA',
                'WV',
                'WI',
                'WY',
            ],
            plan: ['low', 'mid', 'high'],
            package: ['basic', 'standard', 'premium'],
            effectiveDate: Array.from({ length: 24 }).map((_, index) => addDate(index)),
        }

        export function generateDimensionValueSet() {
            const dimensions: Record<string, unknown> = {}

            const length = perf.random.randomTo(Object.keys(values).length)
            for (let index = 0; index < length; index++) {
                const dimensionName = Object.keys(values)[index]
                const dimensionValues = Object.values(values)[index]
                const dimensionValue = dimensionValues[perf.random.randomTo(dimensionValues.length - 1)]

                dimensions[dimensionName] = dimensionValue
            }

            return dimensions
        }

        export function addDate(index: number) {
            const date = new Date()
            if (index > 11) {
                date.setFullYear(date.getFullYear() + 1)
            }
            date.setMonth(index % 12)
            return date
        }
    }

    export namespace random {
        export function randomTo(number: number) {
            return Math.ceil(Math.random() * number)
        }
        export function randomBool(trueBound?: number) {
            return Math.random() < (trueBound || 0.5)
        }
    }
}
