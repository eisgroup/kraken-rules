import { Reducer } from "declarative-js";
import { readFileSync } from "fs";
import path from "path";
import { InstanceFactory } from "../../src/factory/InstanceFactory";
import { Objects } from "../../src/factory/Objects";
import { Scope } from "../../src/scope/Scope";
import { FunctionParameter } from "../../src/symbol/FunctionParameter";
import { FunctionSymbol, FunctionSymbolData } from "../../src/symbol/FunctionSymbol";
import { SymbolTable } from "../../src/symbol/SymbolTable";
import { VariableSymbol } from "../../src/symbol/VariableSymbol";
import { ArrayType, ArrayTypeData } from "../../src/type/ArrayType";
import { GenericType } from "../../src/type/GenericType";
import { Type, TypeData } from "../../src/type/Type";
import { TypeRef } from "../../src/type/TypeRef";
import { UnionType } from "../../src/type/UnionType";

export const create = {
    union(left: Type, right: Type): UnionType {
        const type = new UnionType({
            leftType: left,
            rightType: right,
            __type: "kraken.el.scope.type.UnionType",
            name: "OR",
            extendedTypes: [left, right],
            properties: SymbolTable.create({
                functions: [...left.properties.functions, ...right.properties.functions],
                references: {
                    ...left.properties.references,
                    ...right.properties.references
                }
            }),
            primitive: false,
            known: true
        }, {});
        return type;
    },
    typeRef(type: Type): TypeRef {
        return Objects.withPrototype(type, new TypeRef({ ...type }, {}));
    },
    array(type: Type): ArrayType {
        const data: ArrayTypeData = {
            name: type.name + "[]",
            extendedTypes: [],
            known: true,
            primitive: false,
            elementType: type,
            properties: SymbolTable.EMPTY
        };
        return Objects.withPrototype(
            data,
            new ArrayType(
                { ...data, __type: "kraken.el.scope.type.ArrayType" },
                {}
            )
        );
    },
    type(data: TypeData): Type {
        const type = new Type({
            name: data.name,
            known: data.known,
            primitive: data.primitive,
            extendedTypes: data.extendedTypes,
            properties: data.properties,
            __type: "kraken.el.scope.type.Type"
        }, {});
        return type;
    },
    generic(): Type {
        const type = create.type({
            name: "T",
            extendedTypes: [],
            properties: SymbolTable.EMPTY,
            known: true,
            primitive: false
        });
        const generic = Objects.withPrototype(type, new GenericType({
            ...type,
            __type: "kraken.el.scope.type.GenericType"
        }, {}));
        return generic;
    },
    variableSymbol(data: Omit<VariableSymbol, "__type">): VariableSymbol {
        return new VariableSymbol({
            ...data,
            __type: "kraken.el.scope.symbol.VariableSymbol"
        }, {});
    },
    functionSymbol(data: FunctionSymbolData): FunctionSymbol {
        return new FunctionSymbol({
            __type: "kraken.el.scope.symbol.FunctionSymbol",
            ...data
        }, {});
    },
    functionParameter(data: Omit<FunctionParameter, "__type">): FunctionParameter {
        return new FunctionParameter({
            __type: "kraken.el.scope.symbol.FunctionParameter",
            ...data
        }, {});
    }

};

const pathToData = path.join(__dirname, "generated");
function readFile(fileName: string): string {
    return readFileSync(
        path.join(pathToData, fileName),
        { encoding: "utf-8" }
    );
}
function read(filePath: string): unknown {
    return JSON.parse(readFile(filePath));
}

export const json = {
    typeRegistry: read("type-registry.json"),
    scope: {
        creditCardInfo: read("scope_CreditCardInfo_.json"),
        anubisCoverage: read("scope_AnubisCoverage_.json"),
        COLLCoverage: read("scope_COLLCoverage_.json"),

        policy: read("scope_Policy_.json"),
        vehicle: read("scope_Vehicle_.json"),
        insured: read("scope_Insured_.json"),
        superReferer: read("scope_SuperReferer_.json"),
        billingAddress: read("scope_BillingAddress_.json"),
        address: read("scope_AddressInfo_.json")
    }
};

const factory = new InstanceFactory(InstanceFactory.registry);
factory.initTypeRegistry(json.typeRegistry);

function instantiate(o: unknown): Scope {
    return factory.create(o);
}

export const instance = Object
    .entries(json.scope)
    .reduce(Reducer.toObject(x => x[0], x => instantiate(x[1])), {}) as Record<keyof typeof json.scope, Scope>;
