import { Node, NodeType } from './Node'

export type VisitorMethod = (node: Node) => void
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type PartialRecord<K extends keyof any, T> = { [P in K]?: T }
export type PartialNodeVisitor = PartialRecord<MethodTemplate, VisitorMethod>
export type MethodTemplate = Lowercase<`visit_${NodeType}`>

export abstract class BaseNodeVisitor {
    protected queue: Node[] = []

    visitChildren(node: Node): void {
        for (const child of node.children) {
            this.visit(child)
        }
    }

    visit(node: Node): void {
        this.queue.push(node)
        this._visit(node)
        this.queue.pop()
    }

    private _visit(node: Node): void {
        const nt = `visit_${node.nodeType.toLowerCase()}` as MethodTemplate
        const visitNode = this.getNodeVisitor()[nt]
        if (visitNode) {
            return visitNode(node)
        } else {
            return this.visitChildren(node)
        }
    }

    protected abstract getNodeVisitor(): PartialNodeVisitor
}
