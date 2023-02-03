import { FunctionSymbol } from '../../src/symbol/FunctionSymbol'
import { instance } from '../test-data/test-data'
import { ArrayType, Type, UnionType } from '../../src/type/Types'

describe('FunctionSymbol', () => {
    describe('resolveGenericRewrites', () => {
        it('should resolve generic rewrites', () => {
            const SymmetricDifference = find('SymmetricDifference')
            const rewrites = SymmetricDifference.resolveGenericRewrites([
                ArrayType.createArray(Type.STRING),
                ArrayType.createArray(Type.STRING),
            ])
            expect(rewrites['<T>']).toBe(Type.STRING)
        })
        it('should resolve nested generic rewrites', () => {
            const SymmetricDifference = find('SymmetricDifference')
            const rewrites = SymmetricDifference.resolveGenericRewrites([
                ArrayType.createArray(ArrayType.createArray(Type.STRING)),
                ArrayType.createArray(ArrayType.createArray(Type.STRING)),
            ])
            expect(rewrites['<T>']).toStrictEqual(ArrayType.createArray(Type.STRING))
        })
        it('should resolve dynamic generic rewrites', () => {
            const SymmetricDifference = find('SymmetricDifference')
            const rewrites = SymmetricDifference.resolveGenericRewrites([Type.ANY, Type.ANY])
            expect(rewrites['<T>']).toBe(Type.ANY)
        })
        it('should resolve union generic rewrites', () => {
            const SymmetricDifference = find('SymmetricDifference')
            const rewrites = SymmetricDifference.resolveGenericRewrites([
                ArrayType.createArray(UnionType.createUnion(Type.DATE, Type.DATETIME)),
                ArrayType.createArray(UnionType.createUnion(Type.DATE, Type.DATETIME)),
            ])
            expect(rewrites['<T>']).toStrictEqual(UnionType.createUnion(Type.DATE, Type.DATETIME))
        })
    })
})

function find(fnName: string): FunctionSymbol {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const { functions } = instance.policy.parentScope!.type.properties
    const fn = functions.find(fx => fx.name === fnName)
    if (!fn) {
        throw new Error(`Function '${fnName}' did not found`)
    }
    return fn
}
