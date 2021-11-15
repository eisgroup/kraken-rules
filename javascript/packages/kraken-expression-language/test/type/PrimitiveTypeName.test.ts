import { convertToPrimitiveTypeName } from "../../src/type/PrimitiveTypeName";

describe("PrimitiveTypeName", () => {
    it("should test convertToPrimitiveTypeName", () => {
        expect(convertToPrimitiveTypeName("string")).toBe("String");
        expect(convertToPrimitiveTypeName("STRing")).toBe("String");
        expect(convertToPrimitiveTypeName("STRING")).toBe("String");
        expect(convertToPrimitiveTypeName("STRING")).toBe("String");
        expect(convertToPrimitiveTypeName("S")).toBe(undefined);
    });
});
