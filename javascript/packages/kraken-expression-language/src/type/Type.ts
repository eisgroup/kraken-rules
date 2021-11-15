import { ClassName } from "../factory/ClassName";
import { FactoryInstance } from "../factory/FactoryInstance";
import { Objects } from "../factory/Objects";
import { SymbolTable } from "../symbol/SymbolTable";
import { isPrimitiveTypeName, PrimitiveTypeName } from "./PrimitiveTypeName";

export interface TypeData {
    name: string;
    known: boolean;
    primitive: boolean;
    properties: SymbolTable;
    extendedTypes: Type[];
}

export class Type extends FactoryInstance implements TypeData {

    static create(data: TypeData): Type {
        return Objects.withPrototype(data, new Type({ ...data, __type: "kraken.el.scope.type.Type" }, {}));
    }

    static createPrimitive(typeName: PrimitiveTypeName): Type {
        if (!isPrimitiveTypeName(typeName)) {
            throw new Error(`Type name '${typeName}' is not a primitive type name`);
        }
        return this.getNativeTypes()[typeName];
    }

    private static _createPrimitive(name: PrimitiveTypeName): Type {
        return Type._create(name, true, true);
    }

    private static _create(name: string, known: boolean, primitive: boolean): Type {
        const data: TypeData = {
            name, known, primitive, extendedTypes: [], properties: SymbolTable.EMPTY
        };
        return Objects.withPrototype(data, new Type({ ...data, __type: "kraken.el.scope.type.Type" }, {}));
    }

    static readonly BOOLEAN = Type._createPrimitive("Boolean");
    static readonly STRING = Type._createPrimitive("String");
    static readonly NUMBER = Type._createPrimitive("Number");
    static readonly MONEY = Type._createPrimitive("Money");
    static readonly DATE = Type._createPrimitive("Date");
    static readonly DATETIME = Type._createPrimitive("DateTime");
    static readonly TYPE = Type._createPrimitive("Type");
    static readonly UNKNOWN = Type._create("Unknown", false, false);
    static readonly ANY = Type._create("Any", false, true);

    static getNativeTypes(): Record<string, Type> {
        return {
            [Type.BOOLEAN.name]: Type.BOOLEAN,
            [Type.STRING.name]: Type.STRING,
            [Type.NUMBER.name]: Type.NUMBER,
            [Type.MONEY.name]: Type.MONEY,
            [Type.DATE.name]: Type.DATE,
            [Type.DATETIME.name]: Type.DATETIME,
            [Type.ANY.name]: Type.ANY,
            [Type.UNKNOWN.name]: Type.UNKNOWN,
            [Type.TYPE.name]: Type.TYPE
        };
    }

    name!: string;
    known!: boolean;
    primitive!: boolean;
    properties!: SymbolTable;
    extendedTypes!: Type[];
    // tslint:disable-next-line: variable-name
    __type!: ClassName;

    isAssignableFrom(other: Type): boolean {
        return this.equals(Type.ANY)
            || other.equals(Type.ANY)
            || this.equals(Type.NUMBER) && other.equals(Type.MONEY)
            || this.equals(Type.MONEY) && other.equals(Type.NUMBER)
            || this.equals(other)
            || other.extendedTypes.some(extended => this.isAssignableFrom(extended));
    }

    /**
     * Compare amount of values for types
     *
     * @param {Type} other
     * @returns {boolean}
     * @memberof Type
     */
    isComparableWith(other: Type): boolean {
        const { ANY, DATE, DATETIME } = Type;
        return this.equals(ANY)
            || other.equals(ANY)
            || this.areNumbers(this, other)
            || this.areAll(this, other, DATETIME)
            || this.areAll(this, other, DATE);
    }

    resolveCommonTypeOf(other: Type): Type | undefined {
        if (this.isAssignableFrom(other)) {
            return this;
        }
        return this.extendedTypes.find(extended => extended.isAssignableFrom(other));
    }

    equals(other: Type): boolean {
        if (this === other) {
            return true;
        }
        return this.name === other.name;
    }

    stringify(): string {
        return this.name;
    }

    private areNumbers(typeA: Type, typeB: Type): boolean {
        const { NUMBER, MONEY } = Type;
        return (NUMBER.isAssignableFrom(typeA) || MONEY.isAssignableFrom(typeA))
            && (NUMBER.isAssignableFrom(typeB) || MONEY.isAssignableFrom(typeB));
    }

    private areAll(typeA: Type, typeB: Type, expected: Type): boolean {
        return expected.isAssignableFrom(typeA) && expected.isAssignableFrom(typeB);
    }
}
