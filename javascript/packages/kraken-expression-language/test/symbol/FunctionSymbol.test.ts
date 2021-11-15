import { FunctionSymbol } from "../../src/symbol/FunctionSymbol";
import { ArrayType } from "../../src/type/ArrayType";
import { GenericType } from "../../src/type/GenericType";
import { instance } from "../test-data/test-data";

describe("FunctionSymbol", () => {
    describe("findGenericParameter", () => {
        it("should fail to find generic parameter", () => {
            const FromMoney = find("FromMoney");
            expect(() => FromMoney.findGenericParameter(
                GenericType.createGeneric("T")
            )).toThrow("T");
        });
        it("should find generic array type", () => {
            const SymmetricDifference = find("SymmetricDifference");
            const fp = SymmetricDifference.findGenericParameter(
                GenericType.createGeneric("T")
            );
            expect(fp).toBeDefined();
            expect(fp.parameterIndex).toBe(0);
            expect(fp.type).toBeInstanceOf(ArrayType);
            expect(fp.type.name).toBe("T[]");
        });
    });
});

function find(fnName: string): FunctionSymbol {
    const { functions } = instance.policy.parentScope!.type.properties;
    const fn = functions.find(fx => fx.name === fnName);
    if (!fn) {
        throw new Error(`Function '${fnName}' did not found`);
    }
    return fn;
}
