import { Node } from "../../KelTraversingVisitor";

export interface Completion {
    node: Node;
    completions: CompletionItem[];
}

export type CompletionItem = FunctionCompletionItem | ReferenceCompletionItem | KeywordCompletionItem;

export type FunctionCompletionItem = {
    type: "function"
    text: string,
    info: string,
    evaluationType: string
    evaluationTypeCardinality: "single" | "multiple"
};

export type ReferenceCompletionItem = {
    type: "reference"
    text: string
    info: string
    evaluationType: string
    evaluationTypeCardinality: "single" | "multiple"
};

export type KeywordCompletionItem = {
    type: "keyword"
    text: string
    info: string
};
