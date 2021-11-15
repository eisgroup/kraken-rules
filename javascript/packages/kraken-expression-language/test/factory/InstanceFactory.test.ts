import { ClassName } from "../../src/factory/ClassName";
import { InstanceFactory } from "../../src/factory/InstanceFactory";
import { ReferenceMarker } from "../../src/factory/ReferenceMarker";
import { Scope } from "../../src/scope/Scope";
import { SymbolTable } from "../../src/symbol/SymbolTable";
import { VariableSymbol } from "../../src/symbol/VariableSymbol";
import { ArrayType } from "../../src/type/ArrayType";
import { GenericType } from "../../src/type/GenericType";
import { Type } from "../../src/type/Type";
import { TypeRef } from "../../src/type/TypeRef";
import { UnionType } from "../../src/type/UnionType";
import { json } from "../test-data/test-data";

describe("InstanceFactory", () => {
    it("should fail when type registry is empty", () => {
        const factory = new InstanceFactory(InstanceFactory.registry);
        expect(() => factory.create(json.scope.insured)).toThrow("Type registry is empty");

    });
    it("should fail when non object passed", () => {
        const factory = new InstanceFactory(InstanceFactory.registry, { "ANY": Type.ANY });
        expect(() => factory.create(1)).toThrow("non object");
    });
    it("should fail when factory is absent", () => {
        // @ts-expect-error
        const factory = new InstanceFactory({}, { "ANY": Type.ANY });
        expect(() => factory.create({ "@class": "kraken.el.scope.type.Type" }))
            .toThrow("kraken.el.scope.type.Type");
    });
    it("should create instance", () => {
        const factory = new InstanceFactory(InstanceFactory.registry, { "ANY": Type.ANY });
        const data = {
            "@class": "kraken.el.scope.type.Type",
            "extendedTypes": [],
            "known": true,
            "name": "Policy:GLOBAL",
            "primitive": false,
            "properties": {
                "@class": "kraken.el.scope.SymbolTable",
                functions: [],
                references: {}
            }
        };
        const instance = factory.create<Type>(data);
        expect(instance.__type).toBe("kraken.el.scope.type.Type");
        expect(instance).toBeInstanceOf(Type);
        expect(instance.properties).toBeInstanceOf(SymbolTable);
        expect(instance.properties.__type).toBe("kraken.el.scope.SymbolTable");
        expect(instance.properties.functions).toHaveLength(0);
    });
    it("should create type registry from serialized json", () => {
        const factory = new InstanceFactory(InstanceFactory.registry);
        const registry = factory.initTypeRegistry(json.typeRegistry);
        expect(registry).toHaveProperty("Policy");
        expect(registry["Policy"]).toBeInstanceOf(Type);
    });
    it("should have all factories defined", () => {
        const factory = new InstanceFactory(InstanceFactory.registry);
        const typeRegistry = factory.initTypeRegistry(json.typeRegistry);

        const vs = typeRegistry["Policy"].properties.references["riskItems"];
        expect(vs.__type).toBe("kraken.el.scope.symbol.VariableSymbol");
        expect(vs).toBeInstanceOf(VariableSymbol);
        expect(vs.name).toBe("riskItems");
        expect(vs.type).toBeInstanceOf(ArrayType);
        expect(vs.type).toHaveProperty("elementType");

        if (!ArrayType.typeOf(vs.type)) throw new Error();
        expect(vs.type.elementType).toBeInstanceOf(TypeRef);

        if (!TypeRef.typeOf(vs.type.elementType)) throw new Error();
        expect(vs.type.elementType.name).toBe("Vehicle");
        expect(vs.type.elementType.known).toBe(true);
        expect(vs.type.elementType.primitive).toBe(false);
        expect(vs.type.elementType.__type).toBe("kraken.el.scope.type.TypeRef");
        expect(vs.type.elementType.properties).toBeInstanceOf(SymbolTable);

        const policyScope = factory.create<Scope>(json.scope.policy);
        const { type } = policyScope;
        expect(policyScope).toBeInstanceOf(Scope);
        expect(type.properties).toBeInstanceOf(SymbolTable);
        const FromMoney = policyScope.parentScope!.type.properties.functions[0];
        expect(FromMoney.name).toBe("FromMoney");
        expect(FromMoney.type).toBeInstanceOf(Type);
        expect(FromMoney.type.known).toBe(true);
        expect(FromMoney.type.primitive).toBe(true);

        const unionType = policyScope.parentScope!.type.properties.references["AddressInfo"].type;
        if (!UnionType.typeOf(unionType)) throw new Error();

        expect(unionType).toBeInstanceOf(UnionType);
        expect(unionType.__type).toBe("kraken.el.scope.type.UnionType");
        expect(unionType.known).toBe(true);
        expect(unionType.name).toBe("OR");
        expect(unionType.primitive).toBe(false);
        expect(unionType.extendedTypes).toHaveLength(2);
        expect(unionType.extendedTypes[0]).toBeInstanceOf(Type);
        expect(unionType.extendedTypes[1]).toBeInstanceOf(ArrayType);
        expect(unionType.leftType).toBeInstanceOf(Type);
        expect(unionType.rightType).toBeInstanceOf(ArrayType);

        const arrayType = unionType.rightType;
        if (!ArrayType.typeOf(arrayType)) throw new Error();
        expect(arrayType.elementType).toBeInstanceOf(Type);
        expect(arrayType.known).toBe(true);
        expect(arrayType.name).toBe("AddressInfo[]");
        expect(arrayType.primitive).toBe(false);

        const parentType = policyScope.parentScope!.type;
        if (!ArrayType.typeOf(parentType.properties.functions[7].parameters[0].type)) throw new Error();
        if (!GenericType.typeOf(parentType.properties.functions[7].parameters[0].type.elementType)) throw new Error();
        const generic = parentType.properties.functions[7].parameters[0].type.elementType;
        expect(generic.known).toBe(true);
        expect(generic.primitive).toBe(false);
        expect(generic.name).toBe("T");
    });
    it("should throw when no factory is defined", () => {
        const factory = new InstanceFactory(InstanceFactory.registry, { "ANY": Type.ANY });
        const clazz: ClassName = "kraken.el.scope.type.Type";
        const data = { "@class": clazz, __proxy: true, name: "none" } as ReferenceMarker;
        expect(() => factory.create<Type>(data).known).toThrow("none");
    });
    it("should create scope [Insured]", () => {
        const factory = new InstanceFactory(InstanceFactory.registry);
        factory.initTypeRegistry(json.typeRegistry);
        const scope = factory.create<Scope>(json.scope.insured);

        expect(scope.__type).toBe("kraken.el.scope.Scope");
        expect(scope.name).toBe("Policy:GLOBAL->Insured");
        expect(scope.scopeType).toBe("LOCAL");
        expect(scope.allTypes).toBeInstanceOf(Object);
        expect(scope.parentScope).toBeDefined();
        expect(scope.parentScope).toBeInstanceOf(Scope);
        expect(scope.resolveFunctionSymbol("FromMoney", 1)).toBeDefined();
        expect(scope.type).toBeInstanceOf(Type);
        expect(scope.type.name).toBe("Insured");
        expect(scope.type.known).toBe(true);
        expect(scope.type.primitive).toBe(false);
        expect(scope.type.properties).toBeInstanceOf(SymbolTable);
        expect(scope.type.properties.references).toHaveProperty("addressInfo");
    });
    it("should create scope [SuperReferer]", () => {
        const factory = new InstanceFactory(InstanceFactory.registry);
        factory.initTypeRegistry(json.typeRegistry);
        const scope = factory.create<Scope>(json.scope.superReferer);

        expect(scope.__type).toBe("kraken.el.scope.Scope");
        expect(scope.name).toBe("Policy:GLOBAL->SuperReferer");
        expect(scope.scopeType).toBe("LOCAL");
        expect(scope.allTypes).toBeInstanceOf(Object);
        expect(scope.type).toBeInstanceOf(Type);
        expect(scope.type.name).toBe("SuperReferer");
        expect(scope.type.known).toBe(true);
        expect(scope.type.primitive).toBe(false);
        expect(scope.type.properties).toBeInstanceOf(SymbolTable);
        expect(scope.type.extendedTypes).toHaveLength(1);
        expect(scope.type.extendedTypes[0].name).toBe("Referer");
        expect(scope.type.extendedTypes[0].known).toBe(true);
        expect(scope.type.extendedTypes[0]).toBeInstanceOf(Type);
        // TODO functions
    });
});
