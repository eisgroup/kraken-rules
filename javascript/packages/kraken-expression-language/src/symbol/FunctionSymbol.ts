import { Type } from '../type/Types'
import { FunctionParameter } from './FunctionParameter'
import { KelSymbol, KelSymbolData } from './KelSymbol'

export interface FunctionSymbolData extends KelSymbolData {
    parameters: FunctionParameter[]
}

export class FunctionSymbol extends KelSymbol {
    parameters!: FunctionParameter[]

    resolveGenericRewrites(args: Type[]): Record<string, Type> {
        return this.parameters
            .map(p => p.type.resolveGenericTypeRewrites(args[p.parameterIndex]))
            .reduce((accum, obj) => {
                for (const key of Object.keys(obj)) {
                    const v1 = accum[key]
                    const v2 = obj[key]
                    accum[key] = v1 ? v1.resolveCommonTypeOf(v2) ?? Type.UNKNOWN : v2
                }
                return accum
            }, {})
    }
}
