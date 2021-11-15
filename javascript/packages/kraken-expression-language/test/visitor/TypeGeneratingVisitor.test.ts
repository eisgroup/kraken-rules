import { createParser } from "kraken-expression-language-visitor";
import { SymbolTable } from "../../src/symbol/SymbolTable";
import { ArrayType } from "../../src/type/ArrayType";
import { GenericType } from "../../src/type/GenericType";
import { Type } from "../../src/type/Type";
import { UnionType } from "../../src/type/UnionType";
import { TypeGeneratingVisitor } from "../../src/visitor/TypeGeneratingVisitor";
import { instance } from "../test-data/test-data";

describe("TypeGeneratingVisitor", () => {
    it("should find primitive type", () => {
        const type = parseType("String");
        expect(type).toBeInstanceOf(Type);
        expect(type.name).toBe("String");
        expect(type.primitive).toBe(true);
    });
    it("should parse existing type", () => {
        const type = parseType("Policy");
        expect(type).toBeInstanceOf(Type);
        expect(type.name).toBe("Policy");
        expect(type.properties.references).toHaveProperty("versionDescription");
    });
    it("should parse non existing type", () => {
        const type = parseType("NoneExisting");
        expect(type).toBeInstanceOf(Type);
        expect(type.name).toBe("NoneExisting");
        expect(type.properties).toBe(SymbolTable.EMPTY);
    });
    it("should parse union type", () => {
        const type = parseType("Policy | Info");
        expect(type).toBeInstanceOf(UnionType);
        if (!(type instanceof UnionType)) throw new Error();
        expect(type.name).toBe("OR");
        expect(type.leftType.name).toBe("Policy");
        expect(type.rightType.name).toBe("Info");
    });
    it("should parse array type", () => {
        const type = parseType("(Policy | Info)[]");
        expect(type).toBeInstanceOf(ArrayType);
        if (!(type instanceof ArrayType)) throw new Error();
        expect(type.name).toBe("OR[]");
        expect(type.elementType.name).toBe("OR");
        if (!(type.elementType instanceof UnionType)) throw new Error();
        expect(type.elementType.leftType.name).toBe("Policy");
        expect(type.elementType.rightType.name).toBe("Info");
    });
    it("should parse generic type", () => {
        const type = parseType("<T>");
        expect(type).toBeInstanceOf(GenericType);
        if (!(type instanceof GenericType)) throw new Error();
        expect(type.name).toBe("T");
    });
});

function parseType(typeExpression: string): Type {
    const visitor = new TypeGeneratingVisitor(instance.policy.allTypes);
    const parse = (ex: string) => createParser(ex).type();
    const tree = parse(typeExpression);
    const type = visitor.visit(tree);
    return type;
}
