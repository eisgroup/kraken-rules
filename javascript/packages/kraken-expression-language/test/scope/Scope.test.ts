import { Scope } from "../../src/scope/Scope";
import { FunctionSymbol } from "../../src/symbol/FunctionSymbol";
import { SymbolTable } from "../../src/symbol/SymbolTable";
import { Type } from "../../src/type/Type";
import { create, instance } from "../test-data/test-data";

describe("Scope", () => {
    describe("isReferenceInCurrentScope", () => {

        it("should find reference in scope", () => {
            const { type: createType, variableSymbol } = create;
            const scope = createLocalScope(
                createType({
                    name: "Vehicle",
                    extendedTypes: [],
                    known: true,
                    primitive: false,
                    properties: SymbolTable.create({
                        functions: [],
                        references: {
                            "code": variableSymbol({
                                name: "code",
                                type: Type.STRING
                            })
                        }
                    })
                })
            );
            expect(scope.isReferenceInCurrentScope("code")).toBe(true);
        });
        it("should not search reference in scope of type", () => {
            const scope = createLocalScope(Type.ANY);
            expect(scope.isReferenceInCurrentScope("code")).toBe(true);
        });
        it("should find reference in current scope and return true", () => {
            expect(instance.billingAddress.isReferenceInCurrentScope("billingNote")).toBe(true);
        });
        it("should find reference in parent scope and return false", () => {
            const anyScope = createLocalScope(Type.ANY);
            anyScope.parentScope = createGlobalScope(instance.billingAddress.type);
            expect(anyScope.isReferenceInCurrentScope("billingNote")).toBe(false);
        });
    });
    describe("resolveFunctionSymbol", () => {
        it("should throw on global scope any type", () => {
            const scope = createGlobalScope(Type.ANY);
            expect(() => scope.resolveFunctionSymbol("FromMoney", 1))
                .toThrow("Global scope with type any is not supported");
        });
        it("should resolve from global scope in mock instance", () => {
            const scope = createGlobalScope(
                create.type({
                    name: "global",
                    known: true,
                    primitive: false,
                    extendedTypes: [],
                    properties: SymbolTable.create({
                        functions: [
                            new FunctionSymbol({
                                __type: "kraken.el.scope.symbol.FunctionSymbol",
                                name: "FromMoney",
                                type: Type.NUMBER,
                                parameters: [
                                    create.functionParameter({
                                        type: Type.MONEY,
                                        parameterIndex: 0
                                    })
                                ]
                            }, {}),
                            new FunctionSymbol({
                                __type: "kraken.el.scope.symbol.FunctionSymbol",
                                name: "FromMoney",
                                type: Type.NUMBER,
                                parameters: [
                                    create.functionParameter({
                                        type: Type.MONEY,
                                        parameterIndex: 0
                                    }),
                                    create.functionParameter({
                                        type: Type.STRING,
                                        parameterIndex: 1
                                    })
                                ]
                            }, {})
                        ],
                        references: {}
                    })
                })
            );
            const functionSymbol = scope.resolveFunctionSymbol("FromMoney", 1);
            expect(functionSymbol).toBeDefined();
            expect(functionSymbol!.name).toBe("FromMoney");
            expect(functionSymbol!.type).toBe(Type.NUMBER);
            expect(functionSymbol!.parameters).toHaveLength(1);
            expect(scope.resolveFunctionSymbol("FromMoney", 3)).toBeUndefined();
            expect(scope.resolveFunctionSymbol("none", 0)).toBeUndefined();
        });
        it("should resolve from global scope in generated instance", () => {
            expect(instance.insured.resolveFunctionSymbol("FromMoney", 1)).toBeDefined();
        });
        it("should resolve from local scope which is Global, in generated instance", () => {
            expect(instance.policy.resolveFunctionSymbol("FromMoney", 1)).toBeDefined();
        });
    });
    describe("resolveReferenceSymbol", () => {
        it("should resolve reference in local properties", () => {
            expect(instance.billingAddress.resolveReferenceSymbol("billingNote")).toBeDefined();
            expect(instance.billingAddress.resolveReferenceSymbol("none")).toBeUndefined();
        });
        it("should resolve reference scope in parent properties", () => {
            expect(instance.billingAddress.resolveReferenceSymbol("Policy")).toBeDefined();
        });
        it("should not resolve reference which do not exist", () => {
            expect(instance.billingAddress.resolveReferenceSymbol("none")).toBeUndefined();
        });
    });
    describe("resolveTypeOf", () => {
        it("should resolve typeof vehicle", () => {
            const type = instance.address.resolveTypeOf("Vehicle");
            expect(type).toBeDefined();
            expect(type.name).toBe("Vehicle");
            expect(type.properties.references).toHaveProperty("newValue");

        });
        it("should resolve typeof any", () => {
            const type = new Scope({ __type: "kraken.el.scope.Scope", type: Type.ANY }, {}).resolveTypeOf("Vehicle");
            expect(Type.ANY.equals(type)).toBeTruthy();
        });
    });
});

function createLocalScope(type: Type): Scope {
    return new Scope({
        __type: "kraken.el.scope.Scope",
        name: type.name,
        allTypes: {},
        scopeType: "LOCAL",
        type
    }, {});
}

function createGlobalScope(type: Type): Scope {
    return new Scope({
        __type: "kraken.el.scope.Scope",
        name: type.name,
        allTypes: {},
        scopeType: "GLOBAL",
        type
    }, {});
}
