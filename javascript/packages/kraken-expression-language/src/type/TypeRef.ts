import { ClassNameMarker, isClassNameMarker } from "../factory/ClassNameMarker";
import { Type, TypeData } from "./Type";

export type TypeRefData = TypeData;

export class TypeRef extends Type implements TypeRefData, ClassNameMarker {

    static typeOf(o: unknown): o is TypeRef {
        return isClassNameMarker(o)
            && (o.__type === "kraken.el.scope.type.TypeRef")
            && (o instanceof TypeRef);
    }
}
