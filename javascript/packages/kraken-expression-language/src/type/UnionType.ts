import { ClassName } from "../factory/ClassName";
import { ClassNameMarker, isClassNameMarker } from "../factory/ClassNameMarker";
import { Json } from "../factory/Json";
import { SymbolTable } from "../symbol/SymbolTable";
import { Type, TypeData } from "./Type";

export interface UnionTypeData extends TypeData, Json {
    leftType: Type;
    rightType: Type;
}

export class UnionType extends Type implements ClassNameMarker {

    static createUnion(leftType: Type, rightType: Type): UnionType {
        const data: UnionTypeData & ClassNameMarker = {
            leftType,
            rightType,
            __type: "kraken.el.scope.type.UnionType",
            name: "OR",
            extendedTypes: [leftType, rightType],
            known: true,
            primitive: false,
            properties: SymbolTable.create({
                functions: [...leftType.properties.functions, ...rightType.properties.functions],
                references: {
                    ...leftType.properties.references,
                    ...rightType.properties.references
                }
            })
        };
        return new UnionType(data, {});
    }

    static typeOf(o: unknown): o is UnionType {
        return isClassNameMarker(o)
            && o.__type === "kraken.el.scope.type.UnionType"
            && o instanceof UnionType;
    }

    isAssignableFrom(other: Type): boolean {
        if (other instanceof UnionType) {
            return this.isAssignableFrom(other.leftType) || this.isAssignableFrom(other.rightType);
        }
        return this.leftType.isAssignableFrom(other) || this.rightType.isAssignableFrom(other);
    }

    isComparableWith(other: Type): boolean {
        if (other instanceof UnionType) {
            return this.isComparableWith(other.leftType) || this.isComparableWith(other.rightType);
        }
        return this.leftType.isComparableWith(other) || this.rightType.isComparableWith(other);
    }

    /** @override */
    stringify(): string {
        return `${this.leftType.stringify()} | ${this.rightType.stringify()}`;
    }

    leftType!: Type;
    rightType!: Type;
    // tslint:disable-next-line: variable-name
    __type!: ClassName;
}
