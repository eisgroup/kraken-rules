import { isClassNameMarker } from "../factory/ClassNameMarker";
import { Objects } from "../factory/Objects";
import { SymbolTable } from "../symbol/SymbolTable";
import { Type, TypeData } from "./Type";

export type GenericTypeData = TypeData;

export class GenericType extends Type implements GenericTypeData {

    static createGeneric(typeName: string): GenericType {
        return Objects.withPrototype(
            Type.create({
                name: typeName,
                primitive: false,
                known: true,
                extendedTypes: [],
                properties: SymbolTable.EMPTY
            }),
            new GenericType({ __type: "kraken.el.scope.type.GenericType" }, {})
        );
    }

    static typeOf(o: unknown): o is GenericType {
        return isClassNameMarker(o)
            && o.__type === "kraken.el.scope.type.GenericType"
            && o instanceof GenericType;
    }

    /** @override */
    isAssignableFrom(): boolean {
        return true;
    }

    /** @override */
    isComparableWith(): boolean {
        return true;
    }
}
