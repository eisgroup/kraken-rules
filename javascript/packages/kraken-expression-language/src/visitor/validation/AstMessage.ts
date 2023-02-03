import { Range } from '../NodeUtils'
import { Node } from '../Node'

export interface AstMessage {
    message: string
    node: Node
    /**
     * if value is `undefined`, then it is impossible to
     * determine range of error node in expression.
     */
    range: Range | undefined
    severity: AstMessageSeverity
}

export type AstMessageSeverity = 'ERROR' | 'WARNING' | 'INFO'
