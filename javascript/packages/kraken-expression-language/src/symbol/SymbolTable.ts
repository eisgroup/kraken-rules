import { ClassName } from '../factory/ClassName'
import { FactoryInstance } from '../factory/FactoryInstance'
import { FunctionSymbol } from './FunctionSymbol'
import { VariableSymbol } from './VariableSymbol'

export class SymbolTable extends FactoryInstance {
    static create(data: { functions: FunctionSymbol[]; references: Record<string, VariableSymbol> }): SymbolTable {
        const st = new SymbolTable(
            {
                __type: 'kraken.el.scope.SymbolTable',
                ...data,
            },
            {},
        )
        return st
    }

    static readonly EMPTY = (() => {
        const table = new SymbolTable(
            {
                __type: 'kraken.el.scope.SymbolTable',
                functions: [],
                references: {},
            },
            {},
        )
        return table
    })()

    __type!: ClassName
    functions!: FunctionSymbol[]
    references!: Record<string, VariableSymbol>
}
