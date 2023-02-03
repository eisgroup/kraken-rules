import { Type } from './type/Types'
/**
 * key      - is type name
 * value    - is Type implementation
 */
export type TypeRegistry = Record<string, Type>
