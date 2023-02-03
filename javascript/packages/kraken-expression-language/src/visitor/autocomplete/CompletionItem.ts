import { Node } from '../Node'

export interface Completion {
    node: Node
    completions: CompletionItem[]
}

export type RelevanceOrder = {
    /**
     * the lower this values,
     * more closer in the scope (more relevant) this completion item is
     */
    relevanceOrder: number
}

export type CompletionItem = FunctionCompletionItem | ReferenceCompletionItem | KeywordCompletionItem

export type CompletionTypeInfo = {
    /** true if this type can be assigned to array */
    assignableToArray: boolean
    /** type token */
    typeName: string
}

export type FunctionCompletionItem = {
    type: 'function'
    text: string
    parameterTypes: CompletionTypeInfo[]
    evaluationType: CompletionTypeInfo
} & RelevanceOrder

export type ReferenceCompletionItem = {
    type: 'reference'
    text: string
    evaluationType: CompletionTypeInfo
} & RelevanceOrder

export type KeywordCompletionItem = {
    type: 'keyword'
    text: string
    info: string
}
