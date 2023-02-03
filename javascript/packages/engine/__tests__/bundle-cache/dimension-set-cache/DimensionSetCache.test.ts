import { DimensionSetCache } from '../../../src/bundle-cache/dimension-set-cache/DimensionSetCache'
import { RegExpPayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { Rule } from 'kraken-model'

let cache: DimensionSetCache
beforeEach(() => {
    cache = new DimensionSetCache({ logWarning: () => void 0 })
})

describe('DimensionSetCache', () => {
    describe('add/get', () => {
        it('should find one added rule', () => {
            const rule = r('plan')
            cache.add({ plan: 'basic' }, [rule])

            const rules = cache.get({ plan: 'basic' })

            expect(rules).toHaveLength(1)
        })
        it('should not find one added rule different dimension value', () => {
            const rule = r('plan')
            cache.add({ plan: 'premium' }, [rule])

            const rules = cache.get({ plan: 'basic' })

            expect(rules).toHaveLength(0)
        })
        it('should not find one added rule different dimension name', () => {
            const rule = r('premium')
            cache.add({ package: 'premium' }, [rule])

            const rules = cache.get({ plan: 'basic' })

            expect(rules).toHaveLength(0)
        })
        it('should find one and omit other by different dimension value', () => {
            cache.add({ plan: 'low' }, [r('plan')])
            const high = r('plan')
            cache.add({ plan: 'high' }, [high])

            const rules = cache.get({ plan: 'high' })

            expect(rules).toHaveLength(1)
            expect(rules).toContain(high)
        })
        it('should find one and omit other by different dimension name', () => {
            cache.add({ plan: 'low' }, [r('plan')])
            const pizza = r('package')
            cache.add({ package: 'pizza' }, [pizza])

            const rules = cache.get({ package: 'pizza' })

            expect(rules).toHaveLength(1)
            expect(rules).toContain(pizza)
        })
        it('should find non dimensional rule always', () => {
            const nonDimensional = r()
            cache.add({ plan: 'low' }, [r('plan'), nonDimensional])
            const high = r('plan')
            cache.add({ plan: 'high' }, [high])

            const rules = cache.get({ plan: 'high' })

            expect(rules).toHaveLength(2)
            expect(rules).toContain(nonDimensional)
            expect(rules).toContain(high)
        })
        it('should log warning on overriding non dimensional rules', () => {
            const spy = jest.fn()
            const cache = new DimensionSetCache({ logWarning: spy })

            cache.add({ plan: 'low' }, [r('plan'), r()])
            cache.add({ plan: 'high' }, [r('plan'), r()])

            expect(spy).toHaveBeenCalledTimes(1)
        })
        it('should handle multiple rle versions', () => {
            // @Dimension(Plan, Basic)
            const planRule = r('plan', 'package')
            planRule.name = 'r'
            cache.add({ plan: 'basic' }, [planRule])

            // @Dimension(Plan, Basic)
            const planPackageRule = r('package', 'plan')
            planPackageRule.name = 'r'
            cache.add({ plan: 'basic', package: 'low' }, [planPackageRule])

            const rules = cache.get({ plan: 'basic', package: 'low' })

            expect(rules).toHaveLength(1)
            expect(rules).toContain(planPackageRule)

            const rulesP = cache.get({ plan: 'basic' })

            expect(rulesP).toHaveLength(1)
            expect(rulesP).toContain(planRule)

            const ex = cache.calculateExcludes({ plan: 'basic', package: 'high' })
            expect(ex).toHaveLength(0)

            cache.add({ plan: 'basic', package: 'high' }, [planRule])
            const rulesPPH = cache.get({ plan: 'basic', package: 'high' })

            expect(rulesPPH).toHaveLength(1)
            expect(rulesPPH).toContain(planRule)
        })
        it('should find varied by any dimensions', () => {
            const planRule = r('plan')
            cache.add({ plan: 'basic', package: 'low' }, [planRule])
            const packageRule = r('package')
            cache.add({ plan: 'basic', package: 'low', state: 'ca' }, [packageRule])
            const allDimensions = any(r())
            cache.add({ plan: 'basic', package: 'low' }, [allDimensions])

            const rules = cache.get({ plan: 'basic', package: 'low' })

            expect(rules).toHaveLength(3)
            expect(rules).toContain(planRule)
            expect(rules).toContain(packageRule)
            expect(rules).toContain(allDimensions)
        })
        it('should add with no dimensions', () => {
            cache.add({}, [any(r())])

            const rules = cache.get({})

            expect(rules).toHaveLength(1)
        })
        it('should not add rules with unknown dimensions as static rules', () => {
            cache.add({ state: 'ca', package: 'low' }, [any(r())])
            cache.add({ state: 'ca' }, [any(r())])
            cache.add({}, [any(r())])

            const rules = cache.get({ state: 'ca' })

            expect(rules).toHaveLength(1)
        })
        it('should not accumulate rules for unknown dimensions', () => {
            cache.add({ state: 'ca', package: 'low', riskState: 'AZ' }, [any(r())])
            cache.add({ state: 'ca', package: 'low' }, [any(r())])
            cache.add({ state: 'ca' }, [any(r())])
            cache.add({}, [any(r())])

            const rules = cache.get({ state: 'ca', package: 'low', riskState: 'AZ' })

            expect(rules).toHaveLength(1)
        })
    })
    describe('isCached', () => {
        it('should check to be cached with no dimensions', () => {
            cache.add({}, [any(r())])

            const cached = cache.isCached({})

            expect(cached).toBeTruthy()
        })
        it('should check to be not cached with no dimensions', () => {
            cache.add({}, [any(r())])

            const cached = cache.isCached({ a: 'a' })

            expect(cached).toBeFalsy()
        })
        it('should check to be cached with dimensions', () => {
            cache.add({ plan: 'basic' }, [any(r())])

            const cached = cache.isCached({ plan: 'basic' })

            expect(cached).toBeTruthy()
        })
        it('should check to be not cached with dimensions', () => {
            cache.add({ plan: 'basic' }, [any(r())])

            const cached = cache.isCached({ plan: 'premium' })

            expect(cached).toBeFalsy()
        })
    })
    describe('calculateExcludes', () => {
        it('should calculate excludes for all cached dimension sets', () => {
            cache.add({ plan: 'basic', package: 'low' }, [r('plan'), r('package'), r('plan', 'package')])

            const excludes = cache.calculateExcludes({ plan: 'basic', package: 'low' })

            expect(excludes).toHaveLength(3)
            expect(excludes).toContainEqual(['plan'])
            expect(excludes).toContainEqual(['plan', 'package'])
            expect(excludes).toContainEqual(['package'])
        })
        it('should not calculate excludes for ANY dimension set, when dimensions does not match', () => {
            cache.add({ plan: 'basic', package: 'low' }, [any(r())])

            const excludes = cache.calculateExcludes({ plan: 'basic', package: 'high' })

            expect(excludes).toHaveLength(0)
        })
        it('should calculate excludes for some cached dimension sets', () => {
            cache.add({ plan: 'basic', package: 'low' }, [r('plan'), r('package')])

            const excludes = cache.calculateExcludes({ plan: 'basic', package: 'low' })

            expect(excludes).toHaveLength(2)
            expect(excludes).toContainEqual(['plan'])
            expect(excludes).toContainEqual(['package'])
        })
        it('should calculate excludes dimensions, with cached more, that requested', () => {
            cache.add({ plan: 'basic', package: 'low' }, [r('plan'), r('package'), r('plan', 'package')])

            const excludes = cache.calculateExcludes({ plan: 'basic' })

            expect(excludes).toHaveLength(1)
            expect(excludes).toContainEqual(['plan'])
        })
        it('should find only matching excludes for new dimensions', () => {
            cache.add({ plan: 'basic', package: 'low' }, [r('plan'), r('package'), r('plan', 'package')])

            const excludes = cache.calculateExcludes({ plan: 'premium', package: 'low' })

            expect(excludes).toHaveLength(1)
            expect(excludes).toContainEqual(['package'])
        })
        it('should not find matching excludes for dimensions', () => {
            cache.add({ plan: 'basic', package: 'low' }, [r('plan'), r('package'), r('plan', 'package')])

            const excludes = cache.calculateExcludes({ plan: 'premium', package: 'high' })

            expect(excludes).toHaveLength(0)
        })
        it('should not find matching excludes for rule with any dimensions', () => {
            const anyDimensionsRule = any(r())
            const dimensions = { plan: 'basic', package: 'low' }
            cache.add(dimensions, [anyDimensionsRule])

            const excludes = cache.calculateExcludes(dimensions)

            expect(excludes).toHaveLength(0)
        })
        it('should exclude static dimensions', () => {
            const dimensions = { plan: 'basic', package: 'low' }
            cache.add(dimensions, [r()])

            const excludes = cache.calculateExcludes(dimensions)

            expect(excludes).toHaveLength(1)
            expect(excludes).toContainEqual([])
        })
    })
    describe('clear', () => {
        it('should clear cache', () => {
            cache.add({}, [r()])
            const dimensions = { plan: 'basic' }
            cache.add(dimensions, [r('basic')])

            cache.clear()

            expect(cache.isCached({})).toBeFalsy()
            expect(cache.isCached(dimensions)).toBeFalsy()

            expect(cache.get({})).toHaveLength(0)
            expect(cache.get(dimensions)).toHaveLength(0)
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

function any(rule: Rule): Rule {
    return {
        ...rule,
        dimensionSet: {
            variability: 'UNKNOWN',
        },
    }
}
