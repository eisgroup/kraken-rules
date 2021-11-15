export type PrimitiveTypeName =
    "Boolean"
    | "String"
    | "Number"
    | "Date"
    | "Money"
    | "DateTime"
    | "Type";

const primitives =
    new Set(["Boolean", "String", "Number", "Date", "Money", "DateTime", "Type"]);

export function isPrimitiveTypeName(typeName: string): typeName is PrimitiveTypeName {
    return primitives.has(typeName);
}

export function convertToPrimitiveTypeName(typeName: string): PrimitiveTypeName | undefined {
    for (const primitive of primitives) {
        if (primitive.toLowerCase() === typeName.toLowerCase()) {
            return primitive as PrimitiveTypeName;
        }
    }
    return;
}
