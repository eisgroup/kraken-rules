import { Sort } from 'declarative-js'
import { Scope } from '../../scope/Scope'
import { ScopeType } from '../../scope/ScopeType'
import { Type } from '../../type/Types'
import { isAccessByIndex, Node } from '../Node'
import { BaseNodeVisitor, PartialNodeVisitor } from '../BaseNodeVisitor'
import {
    Cursor,
    isCursorInsideNode,
    isCursorSomewhereAfterNode,
    isCursorImmediatelyAfterNode,
    isCursorOnOrAfterSecondCursor,
    getRange,
} from '../NodeUtils'
import {
    Completion,
    CompletionItem,
    CompletionTypeInfo,
    FunctionCompletionItem,
    KeywordCompletionItem,
    ReferenceCompletionItem,
} from './CompletionItem'
import ascendingBy = Sort.ascendingBy

export class AutocompleteNodeVisitor extends BaseNodeVisitor {
    private matchedNode: Node | undefined

    constructor(private readonly cursor: Cursor) {
        super()
    }

    visit(node: Node): void {
        // take the root node as a fallback
        if (!this.matchedNode) {
            this.matchedNode = node
        }

        if (isCursorInsideNode(this.cursor, node)) {
            this.matchedNode = node
        }
        super.visit(node)
    }

    protected getNodeVisitor(): PartialNodeVisitor {
        const cursor = this.cursor
        const visit = (node: Node) => this.visit(node)
        const visitChildren = (node: Node) => this.visitChildren(node)
        const setMatchedNode = (node: Node) => (this.matchedNode = node)
        const matchedNode = this.matchedNode

        function isNodeCloserToCursorThanMatchedNode(candidateNode: Node): boolean {
            const candidateNodeRange = getRange(candidateNode)
            if (!candidateNodeRange) {
                // do not consider nodes that cannot have range determined
                // KelTraversingVisitor guarantees that every important scope producing node has range
                return false
            }
            if (!matchedNode) {
                // if nothing matched yet, then the candidate node is the closest so far
                return true
            }
            const matchedNodeRange = getRange(matchedNode)
            if (!matchedNodeRange) {
                return true
            }

            return (
                !isCursorSomewhereAfterNode(cursor, matchedNode) ||
                isCursorOnOrAfterSecondCursor(candidateNodeRange.end, matchedNodeRange.end)
            )
        }

        return {
            visit_access_by_index(node: Node): void {
                const collection = node.children[0]
                const predicate = isAccessByIndex(node) ? node.maybeFilterPredicate : node.children[1]
                visit(collection)
                visit(predicate)
            },
            visit_identifier(node: Node): void {
                if (isCursorImmediatelyAfterNode(cursor, node)) {
                    setMatchedNode(node)
                }
                visitChildren(node)
            },
            visit_empty(node: Node): void {
                if (isCursorSomewhereAfterNode(cursor, node) && isNodeCloserToCursorThanMatchedNode(node)) {
                    setMatchedNode(node)
                }
                visitChildren(node)
            },
            visit_variable_name(node: Node): void {
                if (isCursorImmediatelyAfterNode(cursor, node)) {
                    setMatchedNode(node)
                }
                visitChildren(node)
            },
        }
    }

    getCompletionItems(): Completion {
        if (this.matchedNode) {
            return {
                completions: resolveCompletion(this.matchedNode.scope).concat(
                    completionsByScopeType[this.matchedNode.scope.scopeType],
                ),
                node: this.matchedNode,
            }
        }
        return {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            node: this.matchedNode!,
            completions: [],
        }
    }
}
function resolveCompletion(
    scope: Scope,
    relevanceOrder = 1,
    collectedReferences: Set<string> = new Set<string>(),
): CompletionItem[] {
    function getCompletionTypeFromType(type: Type): CompletionTypeInfo {
        return {
            assignableToArray: type.isAssignableToArray(),
            typeName: type.stringify(),
        }
    }
    const refs: ReferenceCompletionItem[] = Object.values(scope.type.properties.references)
        // skip references that are already collected from child scope
        .filter(r => !collectedReferences.has(r.name))
        .map(f => {
            collectedReferences.add(f.name)
            return f
        })
        .sort(ascendingBy(f => f.name))
        .map(f => ({
            text: f.name,
            type: 'reference',
            evaluationType: getCompletionTypeFromType(f.type),
            relevanceOrder,
        }))
    const functions: FunctionCompletionItem[] = Object.values(scope.type.properties.functions)
        .sort(
            ascendingBy(
                f => f.name,
                f => f.parameters.length,
            ),
        )
        .map(f => ({
            text: f.name,
            info: `${f.name}(${f.parameters.map(p => p.type.stringify()).join(', ')})`,
            type: 'function',
            evaluationType: getCompletionTypeFromType(f.type),
            parameterTypes: f.parameters.map(p => getCompletionTypeFromType(p.type)),
            relevanceOrder,
        }))

    let parentCompletions: CompletionItem[] = []
    if (scope.parentScope) {
        parentCompletions = resolveCompletion(scope.parentScope, relevanceOrder + 1, collectedReferences)
    }

    return [...refs, ...functions, ...parentCompletions]
}

const iterationCompletions: KeywordCompletionItem[] = [
    {
        info:
            'starts iteration block which returns a result obtained by applying a function on each element ' +
            'of the collection',
        text: 'for',
        type: 'keyword',
    },
    {
        info: 'starts iteration block which performs a predicate logic using existential quantifier',
        text: 'some',
        type: 'keyword',
    },
    {
        info: 'starts iteration block which performs a predicate logic using universal quantifier',
        text: 'every',
        type: 'keyword',
    },
]
const flowControlCompletions: KeywordCompletionItem[] = [
    {
        info: 'starts a flow control',
        text: 'if',
        type: 'keyword',
    },
    {
        info: 'flow control keyword used to select branch for truthy expression',
        text: 'then',
        type: 'keyword',
    },
    {
        info: 'flow control keyword used to select branch for falsy expression',
        text: 'else',
        type: 'keyword',
    },
]
const commonKeywords: KeywordCompletionItem[] = [
    {
        text: 'matches',
        info: 'matches string value against the regular expression',
        type: 'keyword',
    },
    {
        text: 'not',
        info: 'negates the boolean value',
        type: 'keyword',
    },
    {
        text: 'in',
        info: 'checks if the value is in the collection',
        type: 'keyword',
    },
    {
        text: 'and',
        info: 'checks if both sides are true, same as `&&`',
        type: 'keyword',
    },
    {
        text: 'or',
        info: 'checks if at least one side is true, same as `||`',
        type: 'keyword',
    },
    {
        text: 'null',
        info:
            'equivalent to no value, which can be used to check if property has no value, ' +
            'for example `currencyCd = null`',
        type: 'keyword',
    },
    {
        text: 'true',
        info: 'positive boolean value',
        type: 'keyword',
    },
    {
        text: 'false',
        info: 'negative boolean value',
        type: 'keyword',
    },
    {
        text: 'this',
        info: 'current scope object',
        type: 'keyword',
    },
    {
        text: 'return',
        info: 'returns value from iteration or from expression',
        type: 'keyword',
    },
    {
        text: 'instanceof',
        info: 'checks if object is an instance of expected type',
        type: 'keyword',
    },
    {
        text: 'typeof',
        info: 'checks if object type is equal to expected type',
        type: 'keyword',
    },
    {
        text: 'set',
        info: 'defines a variable, must be followed by a variable name and then keyword `to`',
        type: 'keyword',
    },
    {
        text: 'to',
        info: 'assigns value to a variable, must be followed by a value statement',
        type: 'keyword',
    },
]

const completionsByScopeType: Record<ScopeType, KeywordCompletionItem[]> = {
    VARIABLES_MAP: [...iterationCompletions, ...commonKeywords, ...flowControlCompletions],
    FILTER: [...iterationCompletions, ...commonKeywords, ...flowControlCompletions],
    GLOBAL: [...iterationCompletions, ...commonKeywords, ...flowControlCompletions],
    LOCAL: [...iterationCompletions, ...commonKeywords, ...flowControlCompletions],
    PATH: [],
}
