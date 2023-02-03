import { Range } from './NodeUtils'
import { Type } from '../type/Types'
import { Scope } from '../scope/Scope'
import { ErrorNode, ParserRuleContext } from 'kraken-expression-language-visitor'

export type NodeType =
    | 'ERROR'
    | 'VALUE_BLOCK'
    | 'VARIABLE'
    | 'VARIABLE_NAME'
    | 'EMPTY'
    | 'ADDITION'
    | 'SUBTRACTION'
    | 'MULTIPLICATION'
    | 'DIVISION'
    | 'MODULUS'
    | 'EXPONENT'
    | 'AND'
    | 'OR'
    | 'EQUALS'
    | 'NOT_EQUALS'
    | 'MORE_THAN'
    | 'MORE_THAN_OR_EQUALS'
    | 'LESS_THAN'
    | 'LESS_THAN_OR_EQUALS'
    | 'IN'
    | 'MATCHES_REG_EXP'
    | 'NEGATION'
    | 'NEGATIVE'
    | 'TYPE'
    | 'STRING'
    | 'BOOLEAN'
    | 'DECIMAL'
    | 'DATE'
    | 'DATETIME'
    | 'NULL'
    | 'INLINE_MAP'
    | 'INLINE_ARRAY'
    | 'REFERENCE'
    | 'THIS'
    | 'IDENTIFIER'
    | 'FUNCTION'
    | 'IF'
    | 'PATH'
    | 'ACCESS_BY_INDEX'
    | 'COLLECTION_FILTER'
    | 'SOME'
    | 'EVERY'
    | 'FOR'
    | 'INSTANCEOF'
    | 'TYPEOF'
    | 'CAST'
    | 'TEMPLATE'

export type Node = ValidKelNode | ErrorKelNode | EmptyKelNode | AccessByIndexNode
export interface EmptyKelNode {
    range?: Range
    nodeType: 'EMPTY'
    evaluationType: Type
    scope: Scope
    children: Node[]
}
export interface ErrorKelNode {
    errorNode: ErrorNode
    nodeType: 'ERROR'
    /** `Type.UNKNOWN`
     * @see {@link Type}
     */
    evaluationType: Type
    scope: Scope
    /** no children */
    children: Node[]
}
export interface ValidKelNode {
    context: ParserRuleContext
    nodeType: NodeType
    evaluationType: Type
    scope: Scope
    deducedTypeFacts?: Record<string, TypeFact>
    /**
     * nodes in order, in which they occur in expression.
     * If expression is ` if true then 1 else 2 `, then child nodes will be `[true, 1, 2]` nodes
     * If expression is ` if true then 1 `, then child nodes will be `[true, 1]` nodes
     */
    children: Node[]
}
export interface AccessByIndexNode extends ValidKelNode {
    nodeType: 'ACCESS_BY_INDEX'
    maybeFilterPredicate: Node
}

export function isEmptyKelNode(node: Node): node is EmptyKelNode {
    return node.nodeType === 'EMPTY'
}
export function isValidKelNode(node: Node): node is ValidKelNode {
    return node.nodeType !== 'ERROR' && node.nodeType !== 'EMPTY'
}
export function isErrorKelNode(node: Node): node is ErrorKelNode {
    return node.nodeType === 'ERROR'
}
export function isAccessByIndex(node: Node): node is AccessByIndexNode {
    return node.nodeType === 'ACCESS_BY_INDEX'
}

export type TypeFact = {
    type: Type
    expression: Node
}
