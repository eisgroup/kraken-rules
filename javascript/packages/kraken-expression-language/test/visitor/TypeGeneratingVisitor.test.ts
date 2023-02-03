import { KelParser } from 'kraken-expression-language-visitor'
import { Type, UnionType, ArrayType, GenericType } from '../../src/type/Types'
import { TypeGeneratingVisitor } from '../../src/visitor/TypeGeneratingVisitor'
import { instance } from '../test-data/test-data'

describe('TypeGeneratingVisitor', () => {
    it('should find primitive type', () => {
        const type = parseType('String')
        expect(type).toStrictEqual(Type.STRING)
    })
    it('should parse existing type', () => {
        const type = parseType('Policy')
        expect(type).toBeInstanceOf(Type)
        expect(type.name).toBe('Policy')
        expect(type.properties.references).toHaveProperty('versionDescription')
    })
    it('should parse non existing type', () => {
        const type = parseType('NoneExisting')
        expect(type).toStrictEqual(Type.UNKNOWN)
    })
    it('should parse union type', () => {
        const type = parseType('Policy | Info')
        expect(type).toBeInstanceOf(UnionType)
        if (!(type instanceof UnionType)) throw new Error()
        expect(type.name).toBe('Policy | Info')
        expect(type.leftType.name).toBe('Policy')
        expect(type.rightType.name).toBe('Info')
    })
    it('should parse array type', () => {
        const type = parseType('(Policy | Info)[]')
        expect(type).toBeInstanceOf(ArrayType)
        if (!(type instanceof ArrayType)) throw new Error()
        expect(type.name).toBe('(Policy | Info)[]')
        expect(type.elementType.name).toBe('Policy | Info')
        if (!(type.elementType instanceof UnionType)) throw new Error()
        expect(type.elementType.leftType.name).toBe('Policy')
        expect(type.elementType.rightType.name).toBe('Info')
    })
    it('should parse generic type', () => {
        const type = parseType('<T>')
        expect(type).toBeInstanceOf(GenericType)
        if (!(type instanceof GenericType)) throw new Error()
        expect(type.name).toBe('<T>')
    })
    it('should parse bounded generic type', () => {
        const type = parseType('<T>', { T: Type.STRING })
        expect(type).toBeInstanceOf(GenericType)
        if (!(type instanceof GenericType)) throw new Error()
        expect(type.name).toBe('<T>')
        expect(type.bound).toBe(Type.STRING)
    })
})

function parseType(typeExpression: string, bounds?: Record<string, Type>): Type {
    const visitor = new TypeGeneratingVisitor(instance.policy.allTypes, bounds || {})
    const parse = (ex: string) => new KelParser(ex).parseType()
    const tree = parse(typeExpression)
    const type = visitor.visit(tree)
    return type
}
