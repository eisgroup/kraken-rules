import { BaseNodeVisitor, PartialNodeVisitor } from '../BaseNodeVisitor'
import { getRange } from '../NodeUtils'
import { TypeHint } from './TypeHint'
import { Node } from '../Node'

export class TypeHintVisitor extends BaseNodeVisitor {
    private hints: TypeHint[] = []

    protected getNodeVisitor(): PartialNodeVisitor {
        const visitChildren = (node: Node) => super.visitChildren(node)

        return {
            visit_path: (node: Node) => {
                const [object] = node.children

                const range = getRange(object)
                if (object.nodeType !== 'COLLECTION_FILTER' && range && object.evaluationType.isAssignableToArray()) {
                    this.hints.push({
                        hint: '[*]',
                        location: { ...range.end },
                    })
                }
                visitChildren(node)
            },
        }
    }
    getHints(): TypeHint[] {
        return this.hints
    }
}
