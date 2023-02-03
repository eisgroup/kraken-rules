import { FunctionContext } from 'kraken-expression-language-visitor'
import { isValidKelNode, Node } from '../Node'
import { Cursor, getRange, isCursorInsideNode } from '../NodeUtils'
import { BaseNodeVisitor, PartialNodeVisitor } from '../BaseNodeVisitor'
import { LocationInfo } from './LocationInfo'

export class InfoAtLocationNodeVisitor extends BaseNodeVisitor {
    private matchedNode: Node | undefined

    constructor(private readonly cursor: Cursor) {
        super()
    }

    visit(node: Node): void {
        if (isCursorInsideNode(this.cursor, node)) {
            this.matchedNode = node

            // tree pruning - only continue visiting if we are within possible option, do not visit unrelated nodes
            super.visit(node)
        }
    }

    protected getNodeVisitor(): PartialNodeVisitor {
        return {}
    }

    getLocationInfo(): LocationInfo | undefined {
        if (!this.matchedNode || !isValidKelNode(this.matchedNode)) {
            return
        }
        const matchedNode = this.matchedNode

        // do not show info when hovering over Type node in cast, instanceof, typeof because it is redundant info
        if (matchedNode.nodeType === 'TYPE') {
            return undefined
        }

        if (matchedNode.nodeType === 'FUNCTION') {
            if (!(matchedNode.context instanceof FunctionContext)) {
                throw new Error(`Node with type ${matchedNode.context} must contain 'FunctionContext'`)
            }
            const parametersCount = matchedNode.children.length
            const functionName = matchedNode.context.functionCall()._functionName.text
            return {
                type: 'function',
                functionName,
                parametersCount,
                range: getRange(matchedNode),
            }
        }

        const evaluationType = matchedNode.evaluationType.stringify()
        return {
            type: 'type',
            evaluationType,
            range: getRange(matchedNode),
        }
    }
}
