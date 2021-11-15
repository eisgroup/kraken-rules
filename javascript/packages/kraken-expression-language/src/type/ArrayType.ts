import { ClassNameMarker, isClassNameMarker } from "../factory/ClassNameMarker";
import { Objects } from "../factory/Objects";
import { Type, TypeData } from "./Type";

export type ArrayTypeData = TypeData & {
    elementType: Type
};

export class ArrayType extends Type implements ArrayTypeData, ClassNameMarker {

    static createFromType(t: Type): ArrayType {
        const data: ArrayTypeData = {
            ...t,
            elementType: t,
            name: t.name + "[]"
        };
        return Objects.withPrototype(data, new ArrayType({ ...data, __type: "kraken.el.scope.type.Type" }, {}));
    }

    static typeOf(o: unknown): o is ArrayType {
        return isClassNameMarker(o)
            && o.__type === "kraken.el.scope.type.ArrayType"
            && o instanceof ArrayType;
    }

    elementType!: Type;

    isAssignableFrom(other: Type): boolean {
        if (other instanceof ArrayType) {
            return this.elementType.isAssignableFrom(other.elementType);
        }
        return super.isAssignableFrom(other);
    }
}
