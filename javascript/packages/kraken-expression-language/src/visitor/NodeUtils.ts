import { Interval } from "kraken-expression-language-visitor";
import { isValidKelNode } from "..";
import { ErrorRange } from "../ErrorRange";
import { isErrorKelNode, Node } from "./KelTraversingVisitor";

export function token(node: Node): string {
    const range = getRange(node);
    if (!range) {
        return "";
    }
    let inputStream;
    if (isValidKelNode(node)) {
        inputStream = node.context.start.inputStream;
    }
    if (isErrorKelNode(node)) {
        inputStream = node.errorNode.symbol.inputStream;
    }
    if (!inputStream) {
        return "";
    }
    return inputStream.getText(Interval.of(range.start.column, range.end.column));
}

export function getRange(node: Node): ErrorRange | undefined {
    if (isValidKelNode(node)) {
        return {
            start: {
                line: node.context.start.line,
                column: node.context.start.startIndex
            },
            end: {
                line: node.context.stop?.line ?? node.context.start.line,
                column: node.context.stop?.stopIndex ?? node.context.start.startIndex + 1
            }
        };
    }
    if (isErrorKelNode(node)) {
        return {
            start: {
                line: node.errorNode.symbol.line,
                column: node.errorNode.symbol.startIndex
            },
            end: {
                line: node.errorNode.symbol.line,
                column: node.errorNode.symbol.stopIndex
            }
        };
    }
    return;
}
