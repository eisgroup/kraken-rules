import { Reducer } from 'declarative-js'
import { readFileSync } from 'fs'
import path from 'path'
import { InstanceFactory } from '../../src/factory/InstanceFactory'
import { Objects } from '../../src/factory/Objects'
import { Scope } from '../../src/scope/Scope'
import { FunctionParameter, FunctionParameterData } from '../../src/symbol/FunctionParameter'
import { FunctionSymbol, FunctionSymbolData } from '../../src/symbol/FunctionSymbol'
import { VariableSymbol } from '../../src/symbol/VariableSymbol'
import { Type, UnionType, TypeRef, GenericType, TypeData, ArrayType } from '../../src/type/Types'

export const create = {
    union(left: Type, right: Type): UnionType {
        return UnionType.createUnion(left, right)
    },
    typeRef(type: Type): TypeRef {
        return Objects.withPrototype(type, new TypeRef({ ...type }, {}))
    },
    array(type: Type): ArrayType {
        return ArrayType.createArray(type)
    },
    type(data: TypeData): Type {
        return Type.createType(data)
    },
    generic(bound?: Type, name?: string): Type {
        return GenericType.createGeneric(name ?? 'T', bound)
    },
    variableSymbol(data: Omit<VariableSymbol, '__type'>): VariableSymbol {
        return new VariableSymbol(
            {
                ...data,
                __type: 'kraken.el.scope.symbol.VariableSymbol',
            },
            {},
        )
    },
    functionSymbol(data: FunctionSymbolData): FunctionSymbol {
        return new FunctionSymbol(
            {
                __type: 'kraken.el.scope.symbol.FunctionSymbol',
                ...data,
            },
            {},
        )
    },
    functionParameter(data: FunctionParameterData): FunctionParameter {
        return new FunctionParameter(
            {
                __type: 'kraken.el.scope.symbol.FunctionParameter',
                ...data,
            },
            {},
        )
    },
}

const pathToData = path.join(__dirname, 'generated')
function readFile(fileName: string): string {
    return readFileSync(path.join(pathToData, fileName), { encoding: 'utf-8' })
}
function read(filePath: string): unknown {
    return JSON.parse(readFile(filePath))
}

export const json = {
    typeRegistry: read('type-registry.json'),
    scope: {
        creditCardInfo: read('scope_CreditCardInfo_.json'),
        anubisCoverage: read('scope_AnubisCoverage_.json'),
        COLLCoverage: read('scope_COLLCoverage_.json'),

        policy: read('scope_Policy_.json'),
        vehicle: read('scope_Vehicle_.json'),
        insured: read('scope_Insured_.json'),
        superReferer: read('scope_SuperReferer_.json'),
        billingAddress: read('scope_BillingAddress_.json'),
        address: read('scope_AddressInfo_.json'),
    },
}

const factory = new InstanceFactory(InstanceFactory.registry)
factory.initTypeRegistry(json.typeRegistry)

function instantiate(o: unknown): Scope {
    return factory.create(o)
}

export const instance = Object.entries(json.scope).reduce(
    Reducer.toObject(
        x => x[0],
        x => instantiate(x[1]),
    ),
    {},
) as Record<keyof typeof json.scope, Scope>
