import {
    AbstractParseTreeVisitor,
    ArrayTypeContext, GenericTypeContext, PlainTypeContext,
    PlainTypePrecedenceContext, UnionTypeContext
} from "kraken-expression-language-visitor";

import { SymbolTable } from "../symbol/SymbolTable";
import { ArrayType } from "../type/ArrayType";
import { GenericType } from "../type/GenericType";
import { Type } from "../type/Type";
import { UnionType } from "../type/UnionType";
import { TypeRegistry } from "../TypeRegistry";

const nativeTypes = Type.getNativeTypes();

export class TypeGeneratingVisitor extends AbstractParseTreeVisitor<Type> {

    constructor(private readonly globalTypes: TypeRegistry) {
        super();
    }

    visitPlainType(context: PlainTypeContext): Type {
        const typeToken = context.identifier().text;
        const nativeType = nativeTypes[typeToken];
        if (nativeType) {
            return nativeType;
        }
        const globalType = this.globalTypes[typeToken];
        if (globalType) {
            return globalType;
        }
        return Type.create({
            extendedTypes: [],
            known: false,
            primitive: false,
            name: typeToken,
            properties: SymbolTable.EMPTY
        });
    }

    visitArrayType(context: ArrayTypeContext): Type {
        return ArrayType.createFromType(this.visit(context.type()));
    }

    visitGenericType(context: GenericTypeContext): Type {
        return GenericType.createGeneric(context.identifier().text);
    }

    visitUnionType(context: UnionTypeContext): Type {
        return UnionType.createUnion(
            this.visit(context.type(0)),
            this.visit(context.type(1))
        );
    }

    visitPlainTypePrecedence(context: PlainTypePrecedenceContext): Type {
        return this.visit(context.type());
    }

    protected defaultResult(): Type {
        return Type.ANY;
    }

}
