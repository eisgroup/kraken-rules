import { Objects } from '../factory/Objects'
import { SymbolTable } from '../symbol/SymbolTable'
import { hasClassNameMarker } from '../factory/ClassNameMarker'
import { FactoryInstance } from '../factory/FactoryInstance'
import { ClassName } from '../factory/ClassName'

export interface TypeData {
    name: string
    properties: SymbolTable
    extendedTypes: Type[]
}

export class Type extends FactoryInstance implements TypeData {
    static readonly BOOLEAN = Type.create('Boolean')
    static readonly STRING = Type.create('String')
    static readonly NUMBER = Type.create('Number')
    static readonly MONEY = Type.create('Money')
    static readonly DATE = Type.create('Date')
    static readonly DATETIME = Type.create('DateTime')
    static readonly TYPE = Type.create('Type')
    static readonly UNKNOWN = Type.create('Unknown')
    static readonly ANY = Type.create('Any')

    static readonly PRIMITIVE_TYPES: Record<string, Type> = {
        [Type.BOOLEAN.name]: Type.BOOLEAN,
        [Type.STRING.name]: Type.STRING,
        [Type.NUMBER.name]: Type.NUMBER,
        [Type.MONEY.name]: Type.MONEY,
        [Type.DATE.name]: Type.DATE,
        [Type.DATETIME.name]: Type.DATETIME,
        [Type.TYPE.name]: Type.TYPE,
    }

    static readonly NATIVE_TYPES: Record<string, Type> = {
        ...Type.PRIMITIVE_TYPES,
        [Type.ANY.name]: Type.ANY,
        [Type.UNKNOWN.name]: Type.UNKNOWN,
    }

    static create(name: string): Type {
        return Type.createType({
            name,
            extendedTypes: [],
            properties: SymbolTable.EMPTY,
        })
    }

    static createType(data: TypeData): Type {
        return Objects.withPrototype(data, new Type({ ...data, __type: 'kraken.el.scope.type.Type' }, {}))
    }

    name!: string
    properties!: SymbolTable
    extendedTypes!: Type[]

    __type!: ClassName

    isAssignableFrom(other: Type): boolean {
        return (
            this.isDynamic() ||
            other.isDynamic() ||
            (GenericType.typeOf(other) && other.bound && this.isAssignableFrom(other.bound)) ||
            (this.equals(Type.NUMBER) && other.equals(Type.MONEY)) ||
            this.equals(other) ||
            (other.extendedTypes ?? []).some(extended => this.isAssignableFrom(extended))
        )
    }

    /**
     * Compare amount of values for types
     *
     * @param {Type} other
     * @returns {boolean}
     * @memberof Type
     */
    isComparableWith(other: Type): boolean {
        return (
            this.isDynamic() ||
            other.isDynamic() ||
            (GenericType.typeOf(other) && other.bound && this.isComparableWith(other.bound)) ||
            this.areNumbers(this, other) ||
            this.areAll(this, other, Type.DATETIME) ||
            this.areAll(this, other, Type.DATE)
        )
    }

    isKnown(): boolean {
        return !this.equals(Type.UNKNOWN)
    }

    isDynamic(): boolean {
        return this.equals(Type.ANY)
    }

    isAssignableToArray(): boolean {
        return this.isDynamic()
    }

    isPrimitive(): boolean {
        return !!Type.PRIMITIVE_TYPES[this.name]
    }

    isGeneric(): boolean {
        return false
    }

    isUnion(): boolean {
        return false
    }

    /**
     * @param genericTypeRewrites
     * @return a type with generics rewritten or UNKNOWN iof rewrite not provided
     */
    rewriteGenericTypes(_genericTypeRewrites: Record<string, Type>): Type {
        return this
    }

    /**
     *
     * @param argumentType
     * @return resolves type rewrites based on actual argument type provided for a generic type parameter.<p>
     *         If argumentType is String[] and this type is T[], then T rewrite is String.<p>
     *         If argumentType is String[][] and this type is T[], then T rewrite is String[].<p>
     *         If argumentType is String[] and this type is T, then T rewrite is String[].
     *         If argumentType is String[] and this type is not a generic, or it cannot resolve to compatible generic,
     *         then rewrite type is not resolved
     */
    resolveGenericTypeRewrites(_argumentType: Type): Record<string, Type> {
        return {}
    }

    /**
     * @return a type with generics rewritten to their bounds or ANY if unbounded
     */
    rewriteGenericBounds(): Type {
        return this
    }

    resolveCommonTypeOf(other: Type): Type | undefined {
        if (this.isDynamic() || other.isDynamic()) {
            return Type.ANY
        }
        if (!this.isKnown() || !other.isKnown()) {
            return Type.UNKNOWN
        }
        if (this.isAssignableFrom(other)) {
            return this
        }
        if (other.isAssignableFrom(this)) {
            return other
        }
        return this.extendedTypes.find(extended => extended.isAssignableFrom(other))
    }

    /**
     * @return wraps type to array if it is not array already.
     * If type is String, then array type is String[].
     * If type is already array then the type itself is returned. If type is dynamic then dynamic is returned.
     */
    wrapArrayType(): Type {
        if (this.isDynamic()) {
            return Type.ANY
        }
        if (!this.isKnown()) {
            return Type.UNKNOWN
        }
        return ArrayType.createArray(this)
    }

    /**
     * @return unwraps type of array.
     * If type is String[][], then array type is String[].
     * If type is already singular then the type itself is returned.
     */
    unwrapArrayType(): Type {
        return this
    }

    /**
     *
     * @param target type to flat map to
     * @return type mapped from this type to target type.
     * If type is Coverage[] and target is Money, then mapped type is Money[].
     * If type is Coverage[] and target is Money[], then mapped type is Money[][].
     * If type is Coverage[] and target is (Money | Money[]), then mapped type is (Money | Money[])[].
     */
    mapTo(target: Type): Type {
        return target
    }

    equals(other: Type): boolean {
        if (this === other) {
            return true
        }
        return this.name === other.name
    }

    stringify(): string {
        return this.name
    }

    private areNumbers(typeA: Type, typeB: Type): boolean {
        return (
            (Type.NUMBER.isAssignableFrom(typeA) || Type.MONEY.isAssignableFrom(typeA)) &&
            (Type.NUMBER.isAssignableFrom(typeB) || Type.MONEY.isAssignableFrom(typeB))
        )
    }

    private areAll(typeA: Type, typeB: Type, expected: Type): boolean {
        return expected.isAssignableFrom(typeA) && expected.isAssignableFrom(typeB)
    }
}

export type ArrayTypeData = TypeData & {
    elementType: Type
}

export class ArrayType extends Type implements ArrayTypeData {
    static typeOf(o: unknown): o is ArrayType {
        return hasClassNameMarker(o) && o.__type === 'kraken.el.scope.type.ArrayType' && o instanceof ArrayType
    }

    static createArray(t: Type): ArrayType {
        const et = UnionType.typeOf(t) ? `(${t.stringify()})` : t.stringify()

        const data: ArrayTypeData = {
            extendedTypes: [],
            properties: SymbolTable.EMPTY,
            elementType: t,
            name: `${et}[]`,
        }
        return Objects.withPrototype(data, new ArrayType({ ...data, __type: 'kraken.el.scope.type.ArrayType' }, {}))
    }

    elementType!: Type

    isKnown(): boolean {
        return this.elementType.isKnown()
    }

    isDynamic(): boolean {
        return false
    }

    isGeneric(): boolean {
        return this.elementType.isGeneric()
    }

    isUnion(): boolean {
        return this.elementType.isUnion()
    }

    rewriteGenericTypes(genericTypeRewrites: Record<string, Type>): Type {
        return ArrayType.createArray(this.elementType.rewriteGenericTypes(genericTypeRewrites))
    }

    resolveGenericTypeRewrites(argumentType: Type): Record<string, Type> {
        if (ArrayType.typeOf(argumentType)) {
            return this.elementType.resolveGenericTypeRewrites(argumentType.elementType)
        }
        if (argumentType.equals(Type.ANY)) {
            return this.elementType.resolveGenericTypeRewrites(Type.ANY)
        }
        return {}
    }

    rewriteGenericBounds(): Type {
        return ArrayType.createArray(this.elementType.rewriteGenericBounds())
    }

    isAssignableToArray(): boolean {
        return true
    }

    isAssignableFrom(other: Type): boolean {
        let otherType = other
        if (GenericType.typeOf(otherType) && otherType.bound) {
            otherType = otherType.bound
        }
        if (otherType.isAssignableToArray()) {
            return this.elementType.isAssignableFrom(otherType.unwrapArrayType())
        }
        return super.isAssignableFrom(otherType)
    }

    resolveCommonTypeOf(other: Type): Type | undefined {
        let otherType = other
        if (GenericType.typeOf(otherType) && otherType.bound) {
            otherType = otherType.bound
        }
        if (otherType.isAssignableToArray()) {
            const commonElementType = this.elementType.resolveCommonTypeOf(otherType.unwrapArrayType())
            return commonElementType ? ArrayType.createArray(commonElementType) : undefined
        }
        return super.resolveCommonTypeOf(otherType)
    }

    wrapArrayType(): Type {
        return this
    }

    unwrapArrayType(): Type {
        return this.elementType
    }

    /**
     *
     * @param target type to flat map to
     * @return type mapped from this type to target type.
     * If type is Coverage[] and target is Money, then mapped type is Money[].
     * If type is Coverage[] and target is Money[], then mapped type is Money[][].
     * If type is Coverage[] and target is (Money | Money[]), then mapped type is (Money | Money[])[].
     */
    mapTo(target: Type): Type {
        if (target.isDynamic()) {
            return Type.ANY
        }
        return ArrayType.createArray(this.elementType.mapTo(target))
    }
}

export interface GenericTypeData extends TypeData {
    bound: Type | undefined
}

export class GenericType extends Type implements GenericTypeData {
    static typeOf(o: unknown): o is GenericType {
        return hasClassNameMarker(o) && o.__type === 'kraken.el.scope.type.GenericType' && o instanceof GenericType
    }

    static createGeneric(typeName: string, bound?: Type): GenericType {
        const data: GenericTypeData = {
            name: '<' + typeName + '>',
            bound: bound,
            extendedTypes: bound ? bound.extendedTypes : [],
            properties: bound ? bound.properties : SymbolTable.EMPTY,
        }
        return Objects.withPrototype(data, new GenericType({ ...data, __type: 'kraken.el.scope.type.GenericType' }, {}))
    }

    bound: Type | undefined

    isKnown(): boolean {
        if (this.bound) {
            return this.bound.isKnown()
        }
        return super.isKnown()
    }

    isDynamic(): boolean {
        if (this.bound) {
            return this.bound.isDynamic()
        }
        return super.isDynamic()
    }

    isGeneric(): boolean {
        return true
    }

    isPrimitive(): boolean {
        if (this.bound) {
            return this.bound.isPrimitive()
        }
        return super.isPrimitive()
    }

    isUnion(): boolean {
        if (this.bound) {
            return this.bound.isUnion()
        }
        return super.isUnion()
    }

    rewriteGenericTypes(genericTypeRewrites: Record<string, Type>): Type {
        if (genericTypeRewrites[this.name]) {
            return genericTypeRewrites[this.name]
        }
        return Type.UNKNOWN
    }

    resolveGenericTypeRewrites(argumentType: Type): Record<string, Type> {
        const rewrites: Record<string, Type> = {}
        rewrites[this.name] = argumentType
        return rewrites
    }

    rewriteGenericBounds(): Type {
        if (this.bound) {
            return this.bound.rewriteGenericBounds()
        }
        return Type.ANY
    }

    isAssignableToArray(): boolean {
        if (this.bound) {
            return this.bound.isAssignableToArray()
        }
        return super.isAssignableToArray()
    }

    isAssignableFrom(other: Type): boolean {
        if (GenericType.typeOf(other)) {
            return this.equals(other)
        }
        if (this.bound) {
            return this.bound.isAssignableFrom(other)
        }
        return super.isAssignableFrom(other)
    }

    resolveCommonTypeOf(other: Type): Type | undefined {
        if (GenericType.typeOf(other) && this.equals(other)) {
            return this
        }
        if (this.bound) {
            return this.bound.resolveCommonTypeOf(other)
        }
        return super.resolveCommonTypeOf(other)
    }
}

export interface UnionTypeData extends TypeData {
    leftType: Type
    rightType: Type
}

export class UnionType extends Type implements UnionTypeData {
    static typeOf(o: unknown): o is UnionType {
        return hasClassNameMarker(o) && o.__type === 'kraken.el.scope.type.UnionType' && o instanceof UnionType
    }

    static createUnion(leftType: Type, rightType: Type): UnionType {
        const data: UnionTypeData = {
            leftType,
            rightType,
            name: leftType.stringify() + ' | ' + rightType.stringify(),
            extendedTypes: [leftType, rightType],
            properties: SymbolTable.create({
                functions: [...leftType.properties.functions, ...rightType.properties.functions],
                references: {
                    ...leftType.properties.references,
                    ...rightType.properties.references,
                },
            }),
        }
        return Objects.withPrototype(data, new UnionType({ ...data, __type: 'kraken.el.scope.type.UnionType' }, {}))
    }

    isAssignableFrom(other: Type): boolean {
        let otherType = other
        if (GenericType.typeOf(otherType) && otherType.bound) {
            otherType = otherType.bound
        }
        if (UnionType.typeOf(otherType)) {
            return this.isAssignableFrom(otherType.leftType) || this.isAssignableFrom(otherType.rightType)
        }
        return this.leftType.isAssignableFrom(otherType) || this.rightType.isAssignableFrom(otherType)
    }

    isComparableWith(other: Type): boolean {
        let otherType = other
        if (GenericType.typeOf(otherType) && otherType.bound) {
            otherType = otherType.bound
        }
        if (UnionType.typeOf(otherType)) {
            return this.isComparableWith(otherType.leftType) || this.isComparableWith(otherType.rightType)
        }
        return this.leftType.isComparableWith(otherType) || this.rightType.isComparableWith(otherType)
    }

    resolveCommonTypeOf(otherType: Type): Type | undefined {
        if (UnionType.typeOf(otherType)) {
            const leftToLeft = this.leftType.resolveCommonTypeOf(otherType.leftType)
            const leftToRight = this.leftType.resolveCommonTypeOf(otherType.rightType)
            const rightToLeft = this.rightType.resolveCommonTypeOf(otherType.leftType)
            const rightToRight = this.rightType.resolveCommonTypeOf(otherType.rightType)
            if ((!leftToLeft && !leftToRight) || (!rightToLeft && !rightToRight)) {
                return undefined
            }
            return this.simplified(leftToLeft ?? leftToRight ?? Type.ANY, rightToRight ?? rightToLeft ?? Type.ANY)
        }
        const left = this.leftType.resolveCommonTypeOf(otherType)
        const right = this.rightType.resolveCommonTypeOf(otherType)
        if (!left || !right) {
            return undefined
        }
        return this.simplified(left, right)
    }

    isGeneric(): boolean {
        return this.leftType.isGeneric() || this.rightType.isGeneric()
    }

    isUnion(): boolean {
        return true
    }

    rewriteGenericTypes(genericTypeRewrites: Record<string, Type>): Type {
        return UnionType.createUnion(
            this.leftType.rewriteGenericTypes(genericTypeRewrites),
            this.rightType.rewriteGenericTypes(genericTypeRewrites),
        )
    }

    resolveGenericTypeRewrites(argumentType: Type): Record<string, Type> {
        const rewrites: Record<string, Type> = {}
        Object.assign(rewrites, this.leftType.resolveGenericTypeRewrites(argumentType))
        Object.assign(rewrites, this.rightType.resolveGenericTypeRewrites(argumentType))
        return rewrites
    }

    rewriteGenericBounds(): Type {
        return UnionType.createUnion(this.leftType.rewriteGenericBounds(), this.rightType.rewriteGenericBounds())
    }

    isAssignableToArray(): boolean {
        return this.leftType.isAssignableToArray() || this.rightType.isAssignableToArray()
    }

    isPrimitive(): boolean {
        return this.leftType.isPrimitive() && this.rightType.isPrimitive()
    }

    isKnown(): boolean {
        return this.leftType.isKnown() && this.rightType.isKnown()
    }

    isDynamic(): boolean {
        return this.leftType.isDynamic() || this.rightType.isDynamic()
    }

    wrapArrayType(): Type {
        const left = this.leftType.wrapArrayType()
        const right = this.rightType.wrapArrayType()
        return this.simplified(left, right)
    }

    unwrapArrayType(): Type {
        const left = this.leftType.unwrapArrayType()
        const right = this.rightType.unwrapArrayType()
        return this.simplified(left, right)
    }

    mapTo(target: Type): Type {
        const left = this.leftType.mapTo(target)
        const right = this.rightType.mapTo(target)
        return this.simplified(left, right)
    }

    private simplified(left: Type, right: Type): Type {
        if (left.equals(right)) {
            return left
        }
        if (!left.isKnown() || !right.isKnown()) {
            return Type.UNKNOWN
        }
        if (!left.isDynamic() && left.isAssignableFrom(right)) {
            return right
        }
        if (!right.isDynamic() && right.isAssignableFrom(left)) {
            return left
        }
        return UnionType.createUnion(left, right)
    }

    leftType!: Type
    rightType!: Type

    __type!: ClassName
}

export type TypeRefData = TypeData

export class TypeRef extends Type implements TypeRefData {
    static typeOf(o: unknown): o is TypeRef {
        return hasClassNameMarker(o) && o.__type === 'kraken.el.scope.type.TypeRef' && o instanceof TypeRef
    }
}
