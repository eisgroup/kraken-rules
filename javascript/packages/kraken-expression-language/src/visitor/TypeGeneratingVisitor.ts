import {
    AbstractParseTreeVisitor,
    ArrayTypeContext,
    GenericTypeContext,
    PlainTypeContext,
    PlainTypePrecedenceContext,
    UnionTypeContext,
} from 'kraken-expression-language-visitor'

import { GenericType, Type, ArrayType, UnionType } from '../type/Types'
import { TypeRegistry } from '../TypeRegistry'

const nativeTypes = Type.NATIVE_TYPES

export class TypeGeneratingVisitor extends AbstractParseTreeVisitor<Type> {
    constructor(private readonly globalTypes: TypeRegistry, private readonly bounds: Record<string, Type>) {
        super()
    }

    visitPlainType(context: PlainTypeContext): Type {
        const typeToken = context.identifier().text
        const nativeType = nativeTypes[typeToken]
        if (nativeType) {
            return nativeType
        }
        const globalType = this.globalTypes[typeToken]
        if (globalType) {
            return globalType
        }
        return Type.UNKNOWN
    }

    visitArrayType(context: ArrayTypeContext): Type {
        return ArrayType.createArray(this.visit(context.type()))
    }

    visitGenericType(context: GenericTypeContext): Type {
        const generic = context.identifier().text
        return GenericType.createGeneric(generic, this.bounds[generic])
    }

    visitUnionType(context: UnionTypeContext): Type {
        return UnionType.createUnion(this.visit(context.type(0)), this.visit(context.type(1)))
    }

    visitPlainTypePrecedence(context: PlainTypePrecedenceContext): Type {
        return this.visit(context.type())
    }

    protected defaultResult(): Type {
        return Type.ANY
    }
}
