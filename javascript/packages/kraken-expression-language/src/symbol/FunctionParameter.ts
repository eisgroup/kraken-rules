import { FactoryInstance } from "../factory/FactoryInstance";
import { Type } from "../type/Type";

export class FunctionParameter extends FactoryInstance {
    parameterIndex!: number;
    type!: Type;
}
