import { ArrayType } from "../type/ArrayType";
import { GenericType } from "../type/GenericType";
import { Type } from "../type/Type";
import { FunctionParameter } from "./FunctionParameter";
import { KelSymbol, KelSymbolData } from "./KelSymbol";

export interface FunctionSymbolData extends KelSymbolData {
    parameters: FunctionParameter[];
}

export class FunctionSymbol extends KelSymbol {
    parameters!: FunctionParameter[];

    findGenericParameter(genericType: GenericType): FunctionParameter {
        const parameter = this.parameters.find(
            param => this.unwrapScalarType(param.type).name === genericType.name
        );
        if (!parameter) {
            throw new Error(`Failed to find parameter with generic type '${genericType.name}'`);
        }
        return parameter;

    }
    private unwrapScalarType(type: Type): Type {
        if (type instanceof ArrayType) {
            return this.unwrapScalarType(type.elementType);
        }
        return type;
    }
}
