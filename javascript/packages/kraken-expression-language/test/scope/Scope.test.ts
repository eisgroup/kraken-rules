import { Scope } from '../../src/scope/Scope'
import { ScopeType } from '../../src/scope/ScopeType'
import { FunctionSymbol } from '../../src/symbol/FunctionSymbol'
import { SymbolTable } from '../../src/symbol/SymbolTable'
import { create, instance } from '../test-data/test-data'
import { Type } from '../../src/type/Types'

describe('Scope', () => {
    describe('isReferenceInCurrentScope', () => {
        it('should find reference in scope', () => {
            const { type: createType, variableSymbol } = create
            const scope = createLocalScope(
                createType({
                    name: 'Vehicle',
                    extendedTypes: [],
                    properties: SymbolTable.create({
                        functions: [],
                        references: {
                            code: variableSymbol({
                                name: 'code',
                                type: Type.STRING,
                            }),
                        },
                    }),
                }),
            )
            expect(scope.isReferenceInCurrentScope('code')).toBe(true)
        })
        it('should not search reference in scope of type', () => {
            const scope = createLocalScope(Type.ANY)
            scope.parentScope = createGlobalScope(instance.billingAddress.type)
            expect(scope.isReferenceInCurrentScope('code')).toBe(true)
        })
        it('should find reference in current scope and return true', () => {
            expect(instance.billingAddress.isReferenceInCurrentScope('billingNote')).toBe(true)
        })
        it('should find reference in parent scope and return false', () => {
            const anyScope = createLocalScope(Type.ANY)
            anyScope.parentScope = createGlobalScope(instance.billingAddress.type)
            expect(anyScope.isReferenceInCurrentScope('billingNote')).toBe(false)
        })
    })
    describe('resolveFunctionSymbol', () => {
        it('should throw on global scope any type', () => {
            const scope = createGlobalScope(Type.ANY)
            expect(() => scope.resolveFunctionSymbol('FromMoney', 1)).toThrow(
                'Global scope with type any is not supported',
            )
        })
        it('should resolve from global scope in mock instance', () => {
            const scope = createGlobalScope(
                create.type({
                    name: 'global',
                    extendedTypes: [],
                    properties: SymbolTable.create({
                        functions: [
                            new FunctionSymbol(
                                {
                                    __type: 'kraken.el.scope.symbol.FunctionSymbol',
                                    name: 'FromMoney',
                                    type: Type.NUMBER,
                                    parameters: [
                                        create.functionParameter({
                                            type: Type.MONEY,
                                            parameterIndex: 0,
                                        }),
                                    ],
                                },
                                {},
                            ),
                            new FunctionSymbol(
                                {
                                    __type: 'kraken.el.scope.symbol.FunctionSymbol',
                                    name: 'FromMoney',
                                    type: Type.NUMBER,
                                    parameters: [
                                        create.functionParameter({
                                            type: Type.MONEY,
                                            parameterIndex: 0,
                                        }),
                                        create.functionParameter({
                                            type: Type.STRING,
                                            parameterIndex: 1,
                                        }),
                                    ],
                                },
                                {},
                            ),
                        ],
                        references: {},
                    }),
                }),
            )
            const functionSymbol = scope.resolveFunctionSymbol('FromMoney', 1)
            expect(functionSymbol).toBeDefined()
            expect(functionSymbol?.name).toBe('FromMoney')
            expect(functionSymbol?.type).toBe(Type.NUMBER)
            expect(functionSymbol?.parameters).toHaveLength(1)
            expect(scope.resolveFunctionSymbol('FromMoney', 3)).toBeUndefined()
            expect(scope.resolveFunctionSymbol('none', 0)).toBeUndefined()
        })
        it('should resolve from global scope in generated instance', () => {
            expect(instance.insured.resolveFunctionSymbol('FromMoney', 1)).toBeDefined()
        })
        it('should resolve from local scope which is Global, in generated instance', () => {
            expect(instance.policy.resolveFunctionSymbol('FromMoney', 1)).toBeDefined()
        })
    })
    describe('resolveReferenceSymbol', () => {
        it('should resolve reference in local properties', () => {
            expect(instance.billingAddress.resolveReferenceSymbol('billingNote')).toBeDefined()
            expect(instance.billingAddress.resolveReferenceSymbol('none')).toBeUndefined()
        })
        it('should resolve reference scope in parent properties', () => {
            expect(instance.billingAddress.resolveReferenceSymbol('Policy')).toBeDefined()
        })
        it('should not resolve reference which do not exist', () => {
            expect(instance.billingAddress.resolveReferenceSymbol('none')).toBeUndefined()
        })
        it('should resolve reference from static scope if filter is dynamic', () => {
            const filterScope = createFilterScope(Type.ANY)
            filterScope.parentScope = createGlobalScope(instance.policy.type)

            // policy symbol from static GLOBAL scope (Policy)
            expect(filterScope.resolveReferenceSymbol('policyNumber')?.type.name).toBe(Type.STRING.name)

            // symbol from dynamic FOR scope because Policy does not have it
            expect(filterScope.resolveReferenceSymbol('model')?.type).toBe(Type.ANY)
        })
        it('should resolve reference from dynamic filter if static GLOBAL does not have it', () => {
            const filterScope = createFilterScope(Type.ANY)
            filterScope.parentScope = createFilterScope(instance.vehicle.type)
            filterScope.parentScope.parentScope = createGlobalScope(instance.policy.type)

            // symbol from dynamic FOR scope because static GLOBAL does not have it
            expect(filterScope.resolveReferenceSymbol('make')?.type).toBe(Type.ANY)

            // policy symbol from GLOBAL scope because static GLOBAL has it
            expect(filterScope.resolveReferenceSymbol('policyNumber')?.type.name).toBe(Type.STRING.name)

            // symbol from dynamic FOR scope because Policy does not have it
            expect(filterScope.resolveReferenceSymbol('model')?.type).toBe(Type.ANY)
        })
        it('should resolve reference from dynamic filter even if nested static filter has it', () => {
            const filterScope = createFilterScope(Type.ANY)
            filterScope.parentScope = createFilterScope(instance.vehicle.type)
            filterScope.parentScope.parentScope = createGlobalScope(instance.vehicle.type)

            // symbol from dynamic FOR scope, because filters are nested
            expect(filterScope.resolveReferenceSymbol('make')?.type).toBe(Type.ANY)
        })
    })
    describe('resolveTypeOf', () => {
        it('should resolve typeof vehicle', () => {
            const type = instance.address.resolveTypeOf('Vehicle')
            expect(type).toBeDefined()
            expect(type.name).toBe('Vehicle')
            expect(type.properties.references).toHaveProperty('newValue')
        })
        it('should resolve to unknown type when type registry has no type for dynamic scope', () => {
            const type = new Scope({ __type: 'kraken.el.scope.Scope', type: Type.ANY }, {}).resolveTypeOf('Vehicle')
            expect(Type.UNKNOWN.equals(type)).toBeTruthy()
        })
        it('should resolve to type when type registry has type for dynamic scope', () => {
            const type = new Scope(
                { __type: 'kraken.el.scope.Scope', type: Type.ANY },
                { ['Vehicle']: instance.vehicle.type },
            ).resolveTypeOf('Vehicle')
            expect(type.name).toBe('Vehicle')
        })
    })
})

function createFilterScope(type: Type): Scope {
    return createScope(type, 'FILTER')
}

function createLocalScope(type: Type): Scope {
    return createScope(type, 'LOCAL')
}

function createGlobalScope(type: Type): Scope {
    return createScope(type, 'GLOBAL')
}

function createScope(type: Type, scopeType: ScopeType): Scope {
    return Scope.createScope(scopeType, undefined, type)
}
