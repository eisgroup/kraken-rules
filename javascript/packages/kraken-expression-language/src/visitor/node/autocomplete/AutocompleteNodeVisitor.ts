import { Scope } from "../../../scope/Scope";
import { ScopeType } from "../../../scope/ScopeType";
import { ArrayType } from "../../../type/ArrayType";
import {
    isErrorKelNode, isValidKelNode, Node
} from "../../KelTraversingVisitor";
import { token } from "../../NodeUtils";
import { BaseNodeVisitor, PartialNodeVisitor } from "../BaseNodeVisitor";
import {
    Completion, CompletionItem, FunctionCompletionItem, KeywordCompletionItem,
    ReferenceCompletionItem
} from "./CompletionItem";
import { CursorLocation } from "../CursorLocation";

export class AutocompleteNodeVisitor extends BaseNodeVisitor {

    private matchedLocationNode: Completion | undefined;
    private readonly allVisited: Node[] = [];

    constructor(private readonly location: CursorLocation) {
        super();
    }

    /** @override */
    visit(node: Node): void {
        this.allVisited.push(node);

        const nodeLocation = resolveLocation(node);
        if (nodeLocation && isInside(this.location, nodeLocation)) {
            this.matchedLocationNode = { node, completions: resolveCompletion(node.scope) };
            super.visit(node);
            return;
        }

        const lastLocation = this.getLocationFromLastValid();
        if (lastLocation && !nodeLocation) {
            // add .
            // add [
            if (isInside(this.location, lastLocation.addColumn(1))) {
                this.matchedLocationNode = { node, completions: resolveCompletion(node.scope) };
                super.visit(node);
                return;
            }
            // add ?[
            if (isInside(this.location, lastLocation.addColumn(2))) {
                this.matchedLocationNode = { node, completions: resolveCompletion(node.scope) };
                super.visit(node);
                return;
            }
        } else {
            super.visit(node);
        }
    }

    protected getNodeVisitor(): PartialNodeVisitor {
        return {};
    }

    getCompletionItems(): Completion | undefined {
        if (this.matchedLocationNode) {
            const element = this.matchedLocationNode;
            return {
                completions: element.completions
                    .concat(completionsByScopeType[element.node.scope.scopeType])
                    .concat(resolveCompletionsByScope(element.node.scope)),
                node: element.node
            };
        }
        const node = this.allVisited[this.allVisited.length - 1];
        return {
            node,
            completions: resolveCompletion(node.scope)
                .concat(completionsByScopeType[node.scope.scopeType])
                .concat(resolveCompletionsByScope(node.scope))
        };
    }

    private getLocationFromLastValid(depth: number = 1): Location | undefined {
        const lastInQueue = this.queue[this.queue.length - depth];
        if (!lastInQueue) {
            return;
        }
        let partialIndex = 0;
        for (let index = 0; index < lastInQueue.children.length; index++) {
            const element = lastInQueue.children[index];
            if (element.nodeType === "PARTIAL") {
                partialIndex = index;
            }
        }
        if (partialIndex === 0) {
            return this.getLocationFromLastValid(depth + 1);
        }
        return resolveLocation(lastInQueue.children[partialIndex - 1])!;
    }
}

function resolveCompletionsByScope(scope: Scope): CompletionItem[] {
    if (scope.scopeType === "FILTER") {
        return [{
            info: `${scope.type.stringify()}`,
            text: "this",
            type: "keyword"
        }];
    }
    return [];
}

function isInside(c: CursorLocation, l: Location): boolean {
    return l.line === c.line
        && l.start <= c.column
        && l.end > c.column;
}

const iterationCompletions: KeywordCompletionItem[] = [
    {
        info: "for iteration start",
        text: "for",
        type: "keyword"
    },
    {
        info: "some iteration start",
        text: "some",
        type: "keyword"
    },
    {
        info: "every iteration start",
        text: "every",
        type: "keyword"
    }
];

const ifCompletion: KeywordCompletionItem = {
    info: "if block",
    text: "if",
    type: "keyword"
};

const completionsByScopeType: Record<ScopeType, KeywordCompletionItem[]> = {
    "FOR RETURN EXPRESSION": [...iterationCompletions, ifCompletion],
    "FILTER": [...iterationCompletions, ifCompletion],
    "GLOBAL": [...iterationCompletions, ifCompletion],
    "LOCAL": [...iterationCompletions, ifCompletion],
    "PATH": []
};

function resolveCompletion(scope: Scope): CompletionItem[] {
    const refs: ReferenceCompletionItem[] = Object.values(scope.type.properties.references).map(r => ({
        text: r.name,
        info: r.type.stringify(),
        type: "reference",
        evaluationType: r.type.stringify(),
        evaluationTypeCardinality: r.type instanceof ArrayType ? "multiple" : "single"
    }));
    const functions: FunctionCompletionItem[] = Object.values(scope.type.properties.functions).map(f => ({
        text: f.name,
        info: `${f.name}(${f.parameters.map(p => p.type.stringify()).join(", ")})`,
        type: "function",
        evaluationType: f.type.stringify(),
        evaluationTypeCardinality: f.type instanceof ArrayType ? "multiple" : "single"
    }));
    let parentCompletions: CompletionItem[] = [];

    if (scope.parentScope) {
        parentCompletions = resolveCompletion(scope.parentScope);
    }

    return [...refs, ...functions, ...parentCompletions];

}

class Location {
    static fromObject(o: {
        line: number,
        start: number,
        end: number
    }): Location {
        return new Location(o.line, o.start, o.end);
    }
    constructor(
        readonly line: number,
        readonly start: number,
        readonly end: number
    ) { }

    addColumn(add: number): Location {
        return new Location(this.line, this.start, this.end + add);
    }
}

function resolveLocation(node: Node): Location | undefined {
    if (isValidKelNode(node)) {
        return Location.fromObject({
            line: node.context._start.line,
            start: node.context._start.startIndex,
            end: node.context._start.startIndex + token(node).length
        });
    }
    if (isErrorKelNode(node)) {
        return Location.fromObject({
            line: node.errorNode.symbol.line,
            start: node.errorNode.symbol.startIndex,
            end: node.errorNode.symbol.startIndex + token(node).length
        });
    }
    return;
}
