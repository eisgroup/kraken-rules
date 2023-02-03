import { SymbolTable } from '../../src/symbol/SymbolTable'
import { create, instance } from '../test-data/test-data'
import { Type } from '../../src/type/Types'

const { UNKNOWN, ANY, STRING, BOOLEAN, NUMBER, MONEY, DATE, DATETIME } = Type

describe('Type', () => {
    describe('isAssignableFrom', () => {
        it('should be assignable', () => {
            expect(NUMBER.isAssignableFrom(NUMBER)).toBeTruthy()
            expect(BOOLEAN.isAssignableFrom(BOOLEAN)).toBeTruthy()
            expect(DATE.isAssignableFrom(DATE)).toBeTruthy()
            expect(DATETIME.isAssignableFrom(DATETIME)).toBeTruthy()
            expect(MONEY.isAssignableFrom(MONEY)).toBeTruthy()
            expect(STRING.isAssignableFrom(STRING)).toBeTruthy()

            expect(MONEY.isAssignableFrom(NUMBER)).toBeFalsy()
            expect(NUMBER.isAssignableFrom(MONEY)).toBeTruthy()

            expect(DATE.isAssignableFrom(DATETIME)).toBe(false)
            expect(DATETIME.isAssignableFrom(DATE)).toBe(false)
        })
        it('should be assignable from bounded generic type', () => {
            const { array, generic, union: or } = create

            expect(STRING.isAssignableFrom(generic(STRING))).toBe(true)
            expect(array(STRING).isAssignableFrom(generic(array(STRING)))).toBe(true)
            expect(STRING.isAssignableFrom(generic(array(STRING)))).toBe(false)
            expect(or(DATE, DATETIME).isAssignableFrom(generic(DATE))).toBe(true)

            expect(MONEY.isAssignableFrom(generic(NUMBER))).toBe(false)
            expect(NUMBER.isAssignableFrom(generic(MONEY))).toBe(true)
            expect(ANY.isAssignableFrom(generic(STRING))).toBe(true)
        })
        it('should be assignable to bounded generic type', () => {
            const { array, generic, union: or } = create

            expect(generic(STRING).isAssignableFrom(STRING)).toBe(true)
            expect(generic(array(STRING)).isAssignableFrom(array(STRING))).toBe(true)
            expect(generic(STRING).isAssignableFrom(array(STRING))).toBe(false)
            expect(generic(or(DATE, DATETIME)).isAssignableFrom(DATE)).toBe(true)

            expect(generic(MONEY).isAssignableFrom(NUMBER)).toBe(false)
            expect(generic(NUMBER).isAssignableFrom(MONEY)).toBe(true)

            expect(generic(STRING).isAssignableFrom(ANY)).toBe(true)
        })
        it('should not be assignable from unbounded generic type', () => {
            const { array, generic } = create

            expect(STRING.isAssignableFrom(generic())).toBe(false)
            expect(array(STRING).isAssignableFrom(generic())).toBe(false)
            expect(UNKNOWN.isAssignableFrom(generic())).toBe(false)
            expect(ANY.isAssignableFrom(generic())).toBe(true)
        })
        it('should not be assignable to unbounded generic type', () => {
            const { array, generic } = create

            expect(generic().isAssignableFrom(STRING)).toBe(false)
            expect(generic().isAssignableFrom(array(STRING))).toBe(false)
            expect(generic().isAssignableFrom(UNKNOWN)).toBe(false)
            expect(generic().isAssignableFrom(ANY)).toBe(true)
        })
        it('generics should be assignable to each other', () => {
            const { generic } = create

            expect(generic().isAssignableFrom(generic())).toBe(true)
            expect(generic(STRING).isAssignableFrom(generic(STRING))).toBe(true)
            expect(generic(STRING, 'T').isAssignableFrom(generic(STRING, 'S'))).toBe(false)
        })
        it('common type between generics', () => {
            const { generic } = create

            expect(generic().resolveCommonTypeOf(generic())).toStrictEqual(generic())
            expect(generic(STRING).resolveCommonTypeOf(generic(STRING))).toStrictEqual(generic(STRING))
            expect(generic(STRING, 'T').resolveCommonTypeOf(generic(STRING, 'S'))).toBe(STRING)
            expect(generic().resolveCommonTypeOf(generic(STRING, 'S'))).toBeUndefined()
            expect(generic(STRING, 'S').resolveCommonTypeOf(generic())).toBeUndefined()
        })
        it('unknown type should not be assignable', () => {
            expect(UNKNOWN.isAssignableFrom(UNKNOWN)).toBe(true)
            expect(STRING.isAssignableFrom(UNKNOWN)).toBe(false)
            expect(UNKNOWN.isAssignableFrom(STRING)).toBe(false)
            expect(UNKNOWN.isAssignableFrom(ANY)).toBe(true)
        })
        it('any type should be assignable ', () => {
            expect(ANY.isAssignableFrom(ANY)).toBe(true)
            expect(ANY.isAssignableFrom(UNKNOWN)).toBe(true)
            expect(STRING.isAssignableFrom(ANY)).toBe(true)
            expect(ANY.isAssignableFrom(STRING)).toBe(true)
        })
        it('array type should be assignable to an array type ', () => {
            const { array } = create

            expect(STRING.isAssignableFrom(array(STRING))).toBe(false)
            expect(array(STRING).isAssignableFrom(STRING)).toBe(false)

            expect(array(STRING).isAssignableFrom(array(STRING))).toBe(true)
            expect(array(STRING).isAssignableFrom(array(ANY))).toBe(true)
            expect(array(STRING).isAssignableFrom(array(UNKNOWN))).toBe(false)

            expect(array(ANY).isAssignableFrom(array(STRING))).toBe(true)

            expect(array(ANY).isAssignableFrom(array(ANY))).toBe(true)
            expect(array(ANY).isAssignableFrom(array(UNKNOWN))).toBe(true)
            expect(array(ANY).isAssignableFrom(array(STRING))).toBe(true)

            expect(array(UNKNOWN).isAssignableFrom(array(ANY))).toBe(true)
            expect(array(UNKNOWN).isAssignableFrom(array(UNKNOWN))).toBe(true)
            expect(array(UNKNOWN).isAssignableFrom(array(STRING))).toBe(false)

            expect(ANY.isAssignableFrom(array(ANY))).toBe(true)
            expect(array(ANY).isAssignableFrom(ANY)).toBe(true)
            expect(array(ANY).isAssignableFrom(STRING)).toBe(false)
            expect(STRING.isAssignableFrom(array(ANY))).toBe(false)
            expect(array(STRING).isAssignableFrom(array(ANY))).toBe(true)
        })
        it('type reference should be equivalent to type', () => {
            const { array, typeRef } = create

            expect(array(typeRef(STRING)).isAssignableFrom(array(STRING))).toBe(true)
            expect(array(typeRef(STRING)).isAssignableFrom(array(ANY))).toBe(true)

            expect(array(typeRef(STRING)).isAssignableFrom(STRING)).toBe(false)
            expect(typeRef(STRING).isAssignableFrom(array(STRING))).toBe(false)

            expect(array(typeRef(STRING)).isAssignableFrom(array(UNKNOWN))).toBe(false)
            expect(array(ANY).isAssignableFrom(array(typeRef(STRING)))).toBe(true)
            expect(array(UNKNOWN).isAssignableFrom(array(typeRef(STRING)))).toBe(false)
        })
        it('union type should be assignable', () => {
            const { union: or, array } = create

            expect(NUMBER.isAssignableFrom(or(NUMBER, STRING))).toBe(true)
            expect(NUMBER.isAssignableFrom(or(BOOLEAN, STRING))).toBe(false)

            expect(or(BOOLEAN, STRING).isAssignableFrom(or(STRING, BOOLEAN))).toBe(true)
            expect(or(BOOLEAN, or(STRING, NUMBER)).isAssignableFrom(or(MONEY, DATE))).toBe(true)

            expect(ANY.isAssignableFrom(or(STRING, BOOLEAN))).toBe(true)
            expect(or(BOOLEAN, STRING).isAssignableFrom(ANY)).toBe(true)

            expect(UNKNOWN.isAssignableFrom(or(STRING, BOOLEAN))).toBe(false)
            expect(or(STRING, BOOLEAN).isAssignableFrom(UNKNOWN)).toBe(false)

            expect(or(STRING, array(STRING)).isAssignableFrom(STRING)).toBe(true)
        })
    })
    describe('isGeneric, isUnion, isPrimitive', () => {
        it('generic type should be generic', () => {
            const { array, generic, union } = create

            expect(STRING.isGeneric()).toBe(false)
            expect(generic().isGeneric()).toBe(true)
            expect(array(generic()).isGeneric()).toBe(true)
            expect(union(generic(), STRING).isGeneric()).toBe(true)
        })
        it('primitive type should be primitive', () => {
            const { array, generic, union } = create

            expect(STRING.isPrimitive()).toBe(true)
            expect(generic().isPrimitive()).toBe(false)
            expect(generic(STRING).isPrimitive()).toBe(true)
            expect(array(STRING).isPrimitive()).toBe(false)
            expect(union(STRING, STRING).isPrimitive()).toBe(true)
        })
        it('union type should be union', () => {
            const { array, generic, union } = create

            expect(STRING.isUnion()).toBe(false)
            expect(generic().isUnion()).toBe(false)
            expect(generic(STRING).isUnion()).toBe(false)
            expect(array(STRING).isUnion()).toBe(false)
            expect(union(STRING, STRING).isUnion()).toBe(true)
            expect(array(union(STRING, STRING)).isUnion()).toBe(true)
        })
    })
    describe('isComparableWith', () => {
        it('should throw on creating type with non primitive type name', () => {
            // @ts-expect-error testing negative cases
            expect(() => Types.createPrimitive('Policy')).toThrow()
        })
        it('should be comparable if any of types is any', () => {
            const { array, generic, union } = create

            expect(ANY.isComparableWith(BOOLEAN)).toBe(true)
            expect(ANY.isComparableWith(array(BOOLEAN))).toBe(true)
            expect(ANY.isComparableWith(union(BOOLEAN, STRING))).toBe(true)
            expect(ANY.isComparableWith(generic())).toBe(true)
        })
        it('should be comparable with number like types', () => {
            const { array, union } = create

            expect(NUMBER.isComparableWith(NUMBER)).toBe(true)
            expect(NUMBER.isComparableWith(MONEY)).toBe(true)
            expect(MONEY.isComparableWith(NUMBER)).toBe(true)

            expect(NUMBER.isComparableWith(array(NUMBER))).toBe(false)
            expect(NUMBER.isComparableWith(union(NUMBER, STRING))).toBe(true)

            expect(STRING.isComparableWith(NUMBER)).toBe(false)
            expect(STRING.isComparableWith(MONEY)).toBe(false)
            expect(NUMBER.isComparableWith(STRING)).toBe(false)
            expect(MONEY.isComparableWith(STRING)).toBe(false)

            expect(union(NUMBER, STRING).isComparableWith(NUMBER)).toBe(true)
            expect(NUMBER.isComparableWith(union(NUMBER, STRING))).toBe(true)
            expect(union(NUMBER, STRING).isComparableWith(union(MONEY, STRING))).toBe(true)
            expect(union(STRING, NUMBER).isComparableWith(union(MONEY, STRING))).toBe(true)
            expect(union(STRING, NUMBER).isComparableWith(union(STRING, MONEY))).toBe(true)
        })
        it('should be comparable with date-time like types', () => {
            const { array, union } = create

            expect(DATETIME.isComparableWith(DATETIME)).toBe(true)
            expect(DATETIME.isComparableWith(array(DATETIME))).toBe(false)
            expect(DATETIME.isComparableWith(union(DATETIME, STRING))).toBe(true)

            expect(STRING.isComparableWith(DATETIME)).toBe(false)
            expect(DATETIME.isComparableWith(STRING)).toBe(false)
        })
        it('should be comparable with date like types', () => {
            const { array, union } = create

            expect(DATE.isComparableWith(DATE)).toBe(true)
            expect(DATE.isComparableWith(array(DATE))).toBe(false)
            expect(DATE.isComparableWith(union(DATE, STRING))).toBe(true)

            expect(STRING.isComparableWith(DATE)).toBe(false)
            expect(DATE.isComparableWith(STRING)).toBe(false)
        })
        it('should be assignable from extended types', () => {
            const { type } = create

            const info = type({
                name: 'Info',
                extendedTypes: [STRING],
                properties: SymbolTable.EMPTY,
            })
            expect(STRING.isComparableWith(info)).toBe(false)
        })
    })
    describe('resolveCommonTypeOf', () => {
        it('should resolve assignable primitives', () => {
            expect(MONEY.resolveCommonTypeOf(MONEY)).toBe(MONEY)
            expect(MONEY.resolveCommonTypeOf(NUMBER)).toBe(NUMBER)
            expect(NUMBER.resolveCommonTypeOf(MONEY)).toBe(NUMBER)
            expect(MONEY.resolveCommonTypeOf(ANY)).toBe(ANY)
            expect(MONEY.resolveCommonTypeOf(BOOLEAN)).toBe(undefined)
        })
        it('should resolve common type in extended types', () => {
            const PersonInfo = findType('PersonInfo')
            const Info = findType('Info')
            expect(PersonInfo.resolveCommonTypeOf(Info)?.name).toBe(Info.name)
            expect(Info.resolveCommonTypeOf(PersonInfo)?.name).toBe(Info.name)
        })
        it('should resolve common type in extended types', () => {
            const PersonInfo = findType('PersonInfo')
            const Info = findType('Info')
            expect(PersonInfo.resolveCommonTypeOf(Info)?.name).toBe(Info.name)
            expect(Info.resolveCommonTypeOf(PersonInfo)?.name).toBe(Info.name)
        })
        it('should resolve complex', () => {
            const { array, union } = create

            expect(array(STRING).resolveCommonTypeOf(array(STRING))).toStrictEqual(array(STRING))
            expect(array(STRING).resolveCommonTypeOf(STRING)).toBeUndefined()
            expect(STRING.resolveCommonTypeOf(array(STRING))).toBeUndefined()
            expect(ANY.resolveCommonTypeOf(array(ANY))).toStrictEqual(ANY)
            expect(union(STRING, NUMBER).resolveCommonTypeOf(union(NUMBER, STRING))).toStrictEqual(
                union(STRING, NUMBER),
            )
        })
    })
    describe('should wrap types', () => {
        const { array, union, generic } = create
        it('should unwrap array type', () => {
            expect(STRING.unwrapArrayType()).toStrictEqual(STRING)
            expect(ANY.unwrapArrayType()).toStrictEqual(ANY)
            expect(UNKNOWN.unwrapArrayType()).toStrictEqual(UNKNOWN)
            expect(array(STRING).unwrapArrayType()).toStrictEqual(STRING)
            expect(array(array(STRING)).unwrapArrayType()).toStrictEqual(array(STRING))
            expect(union(STRING, NUMBER).unwrapArrayType()).toStrictEqual(union(STRING, NUMBER))
            expect(union(array(STRING), array(NUMBER)).unwrapArrayType()).toStrictEqual(union(STRING, NUMBER))
            expect(union(ANY, STRING).unwrapArrayType()).toStrictEqual(ANY)
            expect(union(array(STRING), STRING).unwrapArrayType()).toStrictEqual(STRING)
            expect(array(union(STRING, array(STRING))).unwrapArrayType()).toStrictEqual(union(STRING, array(STRING)))
        })
        it('should wrap array type', () => {
            expect(STRING.wrapArrayType()).toStrictEqual(array(STRING))
            expect(ANY.wrapArrayType()).toStrictEqual(ANY)
            expect(UNKNOWN.wrapArrayType()).toStrictEqual(UNKNOWN)
            expect(array(STRING).wrapArrayType()).toStrictEqual(array(STRING))
            expect(union(STRING, NUMBER).wrapArrayType()).toStrictEqual(union(array(STRING), array(NUMBER)))
            expect(union(STRING, array(NUMBER)).wrapArrayType()).toStrictEqual(union(array(STRING), array(NUMBER)))
            expect(union(array(STRING), array(NUMBER)).wrapArrayType()).toStrictEqual(
                union(array(STRING), array(NUMBER)),
            )
            expect(union(ANY, STRING).wrapArrayType()).toStrictEqual(ANY)
        })
        it('should calculate if type is assignable to array', () => {
            expect(STRING.isAssignableToArray()).toBeFalsy()
            expect(ANY.isAssignableToArray()).toBeTruthy()
            expect(UNKNOWN.isAssignableToArray()).toBeFalsy()
            expect(generic().isAssignableToArray()).toBeFalsy()
            expect(generic(array(STRING)).isAssignableToArray()).toBeTruthy()
            expect(array(STRING).isAssignableToArray()).toBeTruthy()
            expect(union(STRING, NUMBER).isAssignableToArray()).toBeFalsy()
            expect(union(STRING, array(NUMBER)).isAssignableToArray()).toBeTruthy()
            expect(union(array(STRING), array(NUMBER)).isAssignableToArray()).toBeTruthy()
            expect(union(ANY, STRING).isAssignableToArray()).toBeTruthy()
            expect(array(union(STRING, array(STRING))).isAssignableToArray()).toBeTruthy()
        })
        it('should map', () => {
            expect(ANY.mapTo(STRING)).toStrictEqual(STRING)
            expect(UNKNOWN.mapTo(STRING)).toStrictEqual(STRING)
            expect(NUMBER.mapTo(STRING)).toStrictEqual(STRING)
            expect(NUMBER.mapTo(array(STRING))).toStrictEqual(array(STRING))
            expect(NUMBER.mapTo(array(array(STRING)))).toStrictEqual(array(array(STRING)))
            expect(array(NUMBER).mapTo(STRING)).toStrictEqual(array(STRING))
            expect(array(NUMBER).mapTo(array(STRING))).toStrictEqual(array(array(STRING)))
            expect(array(array(NUMBER)).mapTo(STRING)).toStrictEqual(array(array(STRING)))

            expect(array(NUMBER).mapTo(union(STRING, array(STRING)))).toStrictEqual(array(union(STRING, array(STRING))))
            expect(union(NUMBER, array(NUMBER)).mapTo(union(STRING, array(STRING)))).toStrictEqual(
                array(union(STRING, array(STRING))),
            )
            expect(array(union(NUMBER, BOOLEAN)).mapTo(STRING)).toStrictEqual(array(STRING))
            expect(array(union(NUMBER, array(BOOLEAN))).mapTo(STRING)).toStrictEqual(
                array(union(STRING, array(STRING))),
            )
            expect(union(NUMBER, BOOLEAN).mapTo(STRING)).toStrictEqual(STRING)
            expect(union(NUMBER, array(BOOLEAN)).mapTo(STRING)).toStrictEqual(union(STRING, array(STRING)))

            expect(array(NUMBER).mapTo(ANY)).toStrictEqual(ANY)

            expect(NUMBER.mapTo(array(UNKNOWN))).toStrictEqual(array(UNKNOWN))
        })
    })
    it('should rewrite generics', () => {
        const { array, generic, union } = create

        expect(generic().rewriteGenericBounds()).toBe(ANY)
        expect(generic(DATE).rewriteGenericBounds()).toBe(DATE)
        expect(array(generic()).rewriteGenericBounds()).toStrictEqual(array(ANY))
        expect(union(generic(STRING), DATE).rewriteGenericBounds()).toStrictEqual(union(STRING, DATE))

        expect(generic().rewriteGenericTypes({})).toBe(UNKNOWN)
        expect(generic().rewriteGenericTypes({ '<T>': STRING })).toBe(STRING)
        expect(array(generic()).rewriteGenericTypes({ '<T>': STRING })).toStrictEqual(array(STRING))
        expect(union(generic(), DATE).rewriteGenericTypes({ '<T>': STRING })).toStrictEqual(union(STRING, DATE))
    })
})

function findType(name: string): Type | never {
    const { allTypes } = instance.policy
    const type = allTypes[name]
    if (!type) {
        throw new Error(`Failed to find type '${name}'`)
    }
    return type
}
