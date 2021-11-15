export type NodeType =
    "PARTIAL"
    | "ADDITION"
    | "SUBTRACTION"
    | "MULTIPLICATION"
    | "DIVISION"
    | "MODULUS"
    | "EXPONENT"

    | "AND"
    | "OR"
    | "EQUALS"
    | "NOT_EQUALS"
    | "MORE_THAN"
    | "MORE_THAN_OR_EQUALS"
    | "LESS_THAN"
    | "LESS_THAN_OR_EQUALS"
    | "IN"
    | "MATCHES_REG_EXP"

    | "NEGATION"
    | "NEGATIVE"

    | "TYPE"
    | "STRING"
    | "BOOLEAN"
    | "DECIMAL"
    | "DATE"
    | "DATETIME"
    | "NULL"

    | "INLINE_MAP"
    | "INLINE_ARRAY"

    | "REFERENCE"
    | "THIS"
    | "IDENTIFIER"
    | "FUNCTION"
    | "IF"

    | "PATH"
    | "ACCESS_BY_INDEX"
    | "COLLECTION_FILTER"
    | "SOME"
    | "EVERY"
    | "FOR"

    | "INSTANCEOF"
    | "TYPEOF"
    | "CAST"

    | "TEMPLATE";
