import { Type } from "./type/Type";
/**
 * key      - is type name
 * value    - is Type implementation
 */
export type TypeRegistry = Record<string, Type>;
