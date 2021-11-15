import { SymbolTable } from "../../src/symbol/SymbolTable";
import { Type } from "../../src/type/Type";
import { create, instance } from "../test-data/test-data";

describe("Type", () => {
    describe("isAssignableFrom", () => {
        it("should be assignable", () => {
            expect(Type.NUMBER.isAssignableFrom(Type.NUMBER)).toBeTruthy();
            expect(Type.BOOLEAN.isAssignableFrom(Type.BOOLEAN)).toBeTruthy();
            expect(Type.DATE.isAssignableFrom(Type.DATE)).toBeTruthy();
            expect(Type.DATETIME.isAssignableFrom(Type.DATETIME)).toBeTruthy();
            expect(Type.MONEY.isAssignableFrom(Type.MONEY)).toBeTruthy();
            expect(Type.STRING.isAssignableFrom(Type.STRING)).toBeTruthy();

            expect(Type.MONEY.isAssignableFrom(Type.NUMBER)).toBeTruthy();
            expect(Type.NUMBER.isAssignableFrom(Type.MONEY)).toBeTruthy();

            expect(Type.DATE.isAssignableFrom(Type.DATETIME)).toBe(false);
            expect(Type.DATETIME.isAssignableFrom(Type.DATE)).toBe(false);
        });
        it("should be assignable to generic type", () => {
            const { array, generic } = create;
            expect(generic().isAssignableFrom(Type.STRING)).toBe(true);
            expect(Type.STRING.isAssignableFrom(generic())).toBe(false);

            expect(generic().isAssignableFrom(Type.UNKNOWN)).toBe(true);
            expect(Type.UNKNOWN.isAssignableFrom(generic())).toBe(false);

            expect(generic().isAssignableFrom(Type.ANY)).toBe(true);
            expect(generic().isAssignableFrom(array(Type.UNKNOWN))).toBe(true);
            expect(generic().isAssignableFrom(array(Type.STRING))).toBe(true);
            expect(generic().isAssignableFrom(array(generic()))).toBe(true);

            expect(array(generic()).isAssignableFrom(Type.ANY)).toBe(true);
            expect(array(generic()).isAssignableFrom(array(Type.STRING))).toBe(true);
            expect(array(generic()).isAssignableFrom(array(Type.UNKNOWN))).toBe(true);
            expect(array(generic()).isAssignableFrom(array(Type.ANY))).toBe(true);
        });
        it("unknown type should not be assignable", () => {
            expect(Type.UNKNOWN.isAssignableFrom(Type.UNKNOWN)).toBe(true);
            expect(Type.STRING.isAssignableFrom(Type.UNKNOWN)).toBe(false);
            expect(Type.UNKNOWN.isAssignableFrom(Type.STRING)).toBe(false);
            expect(Type.UNKNOWN.isAssignableFrom(Type.ANY)).toBe(true);
        });
        it("any type should be assignable ", () => {
            expect(Type.ANY.isAssignableFrom(Type.ANY)).toBe(true);
            expect(Type.ANY.isAssignableFrom(Type.UNKNOWN)).toBe(true);
            expect(Type.STRING.isAssignableFrom(Type.ANY)).toBe(true);
            expect(Type.ANY.isAssignableFrom(Type.STRING)).toBe(true);
        });
        it("array type should be assignable to an array type ", () => {
            const { array } = create;
            expect(Type.STRING.isAssignableFrom(array(Type.STRING))).toBe(false);
            expect(array(Type.STRING).isAssignableFrom(Type.STRING)).toBe(false);

            expect(array(Type.STRING).isAssignableFrom(array(Type.STRING))).toBe(true);
            expect(array(Type.STRING).isAssignableFrom(array(Type.ANY))).toBe(true);
            expect(array(Type.STRING).isAssignableFrom(array(Type.UNKNOWN))).toBe(false);

            expect(array(Type.ANY).isAssignableFrom(array(Type.STRING))).toBe(true);

            expect(array(Type.ANY).isAssignableFrom(array(Type.ANY))).toBe(true);
            expect(array(Type.ANY).isAssignableFrom(array(Type.UNKNOWN))).toBe(true);
            expect(array(Type.ANY).isAssignableFrom(array(Type.STRING))).toBe(true);

            expect(array(Type.UNKNOWN).isAssignableFrom(array(Type.ANY))).toBe(true);
            expect(array(Type.UNKNOWN).isAssignableFrom(array(Type.UNKNOWN))).toBe(true);
            expect(array(Type.UNKNOWN).isAssignableFrom(array(Type.STRING))).toBe(false);
        });
        it("type reference should be equivalent to type", () => {
            const { array, typeRef } = create;

            expect(array(typeRef(Type.STRING)).isAssignableFrom(array(Type.STRING))).toBe(true);
            expect(array(typeRef(Type.STRING)).isAssignableFrom(array(Type.ANY))).toBe(true);

            expect(array(typeRef(Type.STRING)).isAssignableFrom(Type.STRING)).toBe(false);
            expect(typeRef(Type.STRING).isAssignableFrom(array(Type.STRING))).toBe(false);

            expect(array(typeRef(Type.STRING)).isAssignableFrom(array(Type.UNKNOWN))).toBe(false);
            expect(array(Type.ANY).isAssignableFrom(array(typeRef(Type.STRING)))).toBe(true);
            expect(array(Type.UNKNOWN).isAssignableFrom(array(typeRef(Type.STRING)))).toBe(false);
        });
        it("union type should be assignable", () => {
            const { union: or, generic, array } = create;
            const { UNKNOWN, ANY, STRING, BOOLEAN, NUMBER, MONEY, DATE } = Type;

            expect(NUMBER.isAssignableFrom(or(NUMBER, STRING))).toBe(true);
            expect(NUMBER.isAssignableFrom(or(BOOLEAN, STRING))).toBe(false);

            expect(or(BOOLEAN, STRING).isAssignableFrom(or(STRING, BOOLEAN))).toBe(true);
            expect(or(BOOLEAN, or(STRING, NUMBER)).isAssignableFrom(or(MONEY, DATE))).toBe(true);

            expect(generic().isAssignableFrom(or(STRING, BOOLEAN))).toBe(true);
            expect(or(BOOLEAN, NUMBER).isAssignableFrom(generic())).toBe(false);

            expect(or(generic(), DATE).isAssignableFrom(or(STRING, BOOLEAN))).toBe(true);
            expect(or(BOOLEAN, DATE).isAssignableFrom(or(generic(), STRING))).toBe(false);

            expect(ANY.isAssignableFrom(or(STRING, BOOLEAN))).toBe(true);
            expect(or(BOOLEAN, STRING).isAssignableFrom(ANY)).toBe(true);

            expect(UNKNOWN.isAssignableFrom(or(STRING, BOOLEAN))).toBe(false);
            expect(or(STRING, BOOLEAN).isAssignableFrom(UNKNOWN)).toBe(false);

            expect(or(STRING, array(STRING)).isAssignableFrom(STRING)).toBe(true);
        });
    });

    describe("isComparableWith", () => {
        it("should throw on creating type with non primitive type name", () => {
            // @ts-expect-error
            expect(() => Type.createPrimitive("Policy")).toThrow();
        });
        it("should be comparable if any of types is any", () => {
            const { ANY, BOOLEAN, STRING } = Type;
            const { array, generic, union } = create;

            expect(ANY.isComparableWith(BOOLEAN)).toBe(true);
            expect(ANY.isComparableWith(array(BOOLEAN))).toBe(true);
            expect(ANY.isComparableWith(union(BOOLEAN, STRING))).toBe(true);
            expect(ANY.isComparableWith(generic())).toBe(true);

        });
        it("should be comparable with number like types", () => {
            const { NUMBER, MONEY, STRING } = Type;
            const { array, union } = create;

            expect(NUMBER.isComparableWith(NUMBER)).toBe(true);
            expect(NUMBER.isComparableWith(MONEY)).toBe(true);
            expect(MONEY.isComparableWith(NUMBER)).toBe(true);

            expect(NUMBER.isComparableWith(array(NUMBER))).toBe(false);
            expect(NUMBER.isComparableWith(union(NUMBER, STRING))).toBe(true);

            expect(STRING.isComparableWith(NUMBER)).toBe(false);
            expect(STRING.isComparableWith(MONEY)).toBe(false);
            expect(NUMBER.isComparableWith(STRING)).toBe(false);
            expect(MONEY.isComparableWith(STRING)).toBe(false);

            expect(union(NUMBER, STRING).isComparableWith(NUMBER)).toBe(true);
            expect(NUMBER.isComparableWith(union(NUMBER, STRING))).toBe(true);
            expect(union(NUMBER, STRING).isComparableWith(union(MONEY, STRING))).toBe(true);
            expect(union(STRING, NUMBER).isComparableWith(union(MONEY, STRING))).toBe(true);
            expect(union(STRING, NUMBER).isComparableWith(union(STRING, MONEY))).toBe(true);
        });
        it("should be comparable with date-time like types", () => {
            const { DATETIME, STRING } = Type;
            const { array, union } = create;

            expect(DATETIME.isComparableWith(DATETIME)).toBe(true);
            expect(DATETIME.isComparableWith(array(DATETIME))).toBe(false);
            expect(DATETIME.isComparableWith(union(DATETIME, STRING))).toBe(true);

            expect(STRING.isComparableWith(DATETIME)).toBe(false);
            expect(DATETIME.isComparableWith(STRING)).toBe(false);
        });
        it("should be comparable with date like types", () => {
            const { DATE, STRING } = Type;
            const { array, union } = create;

            expect(DATE.isComparableWith(DATE)).toBe(true);
            expect(DATE.isComparableWith(array(DATE))).toBe(false);
            expect(DATE.isComparableWith(union(DATE, STRING))).toBe(true);

            expect(STRING.isComparableWith(DATE)).toBe(false);
            expect(DATE.isComparableWith(STRING)).toBe(false);
        });
        it("generic should be comparable to all types", () => {
            expect(create.generic().isComparableWith(Type.UNKNOWN)).toBe(true);
        });
        it("should be assignable from extended types", () => {
            const { type } = create;
            const { STRING } = Type;

            const info = type({
                name: "Info",
                primitive: false,
                known: true,
                extendedTypes: [STRING],
                properties: SymbolTable.EMPTY
            });
            expect(STRING.isComparableWith(info)).toBe(false);
        });
    });

    describe("resolveCommonTypeOf", () => {
        it("should resolve assignable primitives", () => {
            const { MONEY, NUMBER, ANY, BOOLEAN } = Type;
            expect(MONEY.resolveCommonTypeOf(MONEY)).toBe(MONEY);
            expect(MONEY.resolveCommonTypeOf(NUMBER)).toBe(MONEY);
            expect(MONEY.resolveCommonTypeOf(ANY)).toBe(MONEY);
            expect(MONEY.resolveCommonTypeOf(BOOLEAN)).toBe(undefined);
        });
        it("should resolve common type in extended types", () => {
            const PersonInfo = findType("PersonInfo");
            const Info = findType("Info");
            expect(PersonInfo.resolveCommonTypeOf(Info)?.name).toBe(Info.name);
            expect(Info.resolveCommonTypeOf(PersonInfo)?.name).toBe(Info.name);
        });
    });
});

function findType(name: string): Type | never {
    const { allTypes } = instance.policy;
    const type = allTypes[name];
    if (!type) {
        throw new Error(`Failed to find type '${name}'`);
    }
    return type;
}
