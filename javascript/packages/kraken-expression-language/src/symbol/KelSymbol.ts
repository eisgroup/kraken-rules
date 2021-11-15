import { FactoryInstance } from "../factory/FactoryInstance";
import { Type } from "../type/Type";

export interface KelSymbolData {
    name: string;
    type: Type;
}

export abstract class KelSymbol extends FactoryInstance {
    name!: string;
    type!: Type;
}
