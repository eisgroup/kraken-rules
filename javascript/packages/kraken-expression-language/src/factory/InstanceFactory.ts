import { SymbolTable } from '../symbol/SymbolTable'
import { ClassName } from './ClassName'
import { ReferenceMarker } from './ReferenceMarker'
import { VariableSymbol } from '../symbol/VariableSymbol'
import { TypeRegistry } from '../TypeRegistry'
import { ClassNameMarker } from './ClassNameMarker'
import { FunctionSymbol } from '../symbol/FunctionSymbol'
import { FunctionParameter } from '../symbol/FunctionParameter'
import { Type, GenericType, ArrayType, TypeRef, UnionType } from '../type/Types'
import { Scope } from '../scope/Scope'
import { Json } from './Json'
import { FactoryInstance } from './FactoryInstance'

type ClassMetadata = { '@class': ClassName }
type FactoryRegistry = Record<
    ClassName,
    new (json: Json & ClassNameMarker, typeRegistry: TypeRegistry) => FactoryInstance
>

export class InstanceFactory {
    static registry: FactoryRegistry = {
        'kraken.el.serialization.TypeRegistry': class T extends FactoryInstance {},
        'kraken.el.scope.type.Type': Type,
        'kraken.el.serialization.RootType': Type,
        'kraken.el.scope.SymbolTable': SymbolTable,
        'kraken.el.scope.type.GenericType': GenericType,
        'kraken.el.scope.type.ArrayType': ArrayType,
        'kraken.el.scope.symbol.VariableSymbol': VariableSymbol,
        'kraken.el.scope.type.TypeRef': TypeRef,
        'kraken.el.scope.type.UnionType': UnionType,
        'kraken.el.scope.symbol.FunctionSymbol': FunctionSymbol,
        'kraken.el.scope.symbol.FunctionParameter': FunctionParameter,
        'kraken.el.scope.Scope': Scope,
    }

    private readonly factoryRegistry: FactoryRegistry
    private readonly typeRegistry: TypeRegistry
    private typeRegistryPresent: boolean

    constructor(factoryRegistry: FactoryRegistry, typeRegistry?: TypeRegistry) {
        this.factoryRegistry = factoryRegistry
        this.typeRegistry = typeRegistry ?? {}
        this.typeRegistryPresent = false
    }

    initTypeRegistry(json: unknown): TypeRegistry {
        this.createInstance(json)
        this.typeRegistryPresent = true
        return this.typeRegistry
    }

    create<T = unknown>(json: unknown): T {
        if (Object.keys(this.typeRegistry).length === 0) {
            throw new Error('Type registry is empty')
        }
        return this.createInstance(json)
    }

    private createInstance<T>(json: unknown): T {
        if (!isJson(json)) throw new Error(`Cannot create instance from non object`)

        const instance = this.initData(json)

        if (isReference(json)) return this.createProxy(json, instance)

        for (const key in json) {
            if (hasProp(json, key)) {
                const value = json[key]
                if (Array.isArray(value)) {
                    // json array
                    instance[key] = value.map(el => this.createInstance(el))
                } else if (isJson(value)) {
                    //json object
                    instance[key] = this.createInstance(value)
                }
            }
        }

        if (!this.typeRegistryPresent && isTypeInstance(instance)) this.addToTypeRegistry(instance)
        return instance
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private initData(json: Json): any {
        if (hasMetadata(json)) {
            const className = json['@class']
            if (!this.factoryRegistry[className]) throw new Error(`Factory is absent for type '${className}'`)
            const Clazz = this.factoryRegistry[className]
            json.__type = className
            return new Clazz(json as unknown as ClassNameMarker & Json, this.typeRegistry)
        } else {
            // key value plain object
            return json
        }
    }

    private createProxy<T>(json: Json & ReferenceMarker, protoSource: object): T {
        const { typeRegistry } = this
        Object.setPrototypeOf(json, protoSource)
        const proxy = new Proxy(json, {
            get(target: unknown, prop: string): unknown {
                if (Object.prototype.hasOwnProperty.call(target, prop)) return (target as Record<string, unknown>)[prop]
                if (!typeRegistry[json.name])
                    throw new Error(`TypeRegistry does not have defined type for ${json.name}`)
                const type = typeRegistry[json.name]
                return type[prop as keyof Type]
            },
        })
        return proxy as unknown as T
    }

    private addToTypeRegistry(type: Type): void {
        if (this.typeRegistry[type.name]) return
        this.typeRegistry[type.name] = type
    }
}

function hasProp(data: unknown, key: string): boolean {
    return Object.prototype.hasOwnProperty.call(data, key)
}

function hasMetadata(json: Json): json is Json & ClassMetadata {
    return Object.prototype.hasOwnProperty.call(json, '@class')
}

function isJson(json: unknown): json is Json {
    return typeof json === 'object' && json !== null
}

function isReference(json: unknown): json is ReferenceMarker {
    return Object.prototype.hasOwnProperty.call(json, '__proxy')
}

function hasClassName(o: unknown): o is ClassNameMarker {
    return Object.prototype.hasOwnProperty.call(o, '__type')
}

function isTypeInstance(o: unknown): o is Type {
    return !isReference(o) && hasClassName(o) && o instanceof Type
}
