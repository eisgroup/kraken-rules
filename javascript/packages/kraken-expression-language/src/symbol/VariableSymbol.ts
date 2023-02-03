import { KelSymbol, KelSymbolData } from './KelSymbol'

export type VariableSymbolData = KelSymbolData

export class VariableSymbol extends KelSymbol {
    static create(data: VariableSymbolData): VariableSymbol {
        return new VariableSymbol({ ...data, __type: 'kraken.el.scope.symbol.VariableSymbol' }, {})
    }
}
