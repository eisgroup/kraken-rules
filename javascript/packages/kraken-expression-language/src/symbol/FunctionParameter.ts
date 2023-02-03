import { FactoryInstance } from '../factory/FactoryInstance'
import { Type } from '../type/Types'

export interface FunctionParameterData {
    parameterIndex: number
    type: Type
}

export class FunctionParameter extends FactoryInstance {
    parameterIndex!: number
    type!: Type
}
