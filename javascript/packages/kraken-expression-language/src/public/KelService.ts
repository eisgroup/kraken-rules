import { SyntaxError as KELSyntaxError, createParser } from "kraken-expression-language-visitor";
import { KrakenParser } from "kraken-expression-language-visitor/target/dist/definitions/KrakenParser";
import { ErrorRange } from "../ErrorRange";
import { Scope } from "../scope/Scope";
import { PrimitiveTypeName } from "../type/PrimitiveTypeName";
import { Type } from "../type/Type";
import { KelTraversingVisitor, Node } from "../visitor/KelTraversingVisitor";
import { AutocompleteNodeVisitor } from "../visitor/node/autocomplete/AutocompleteNodeVisitor";
import { Completion } from "../visitor/node/autocomplete/CompletionItem";
import { CursorLocation } from "../visitor/node/CursorLocation";
import { AstError } from "../visitor/node/validation/AstError";
import { ValidatingNodeVisitor } from "../visitor/node/validation/ValidatingVisitor";
import { Cache } from "./Cache";

export interface Validation {
    syntax: SyntaxError[];
    semantic: AstError[];
}

export interface SyntaxError {
    error: string;
    range: ErrorRange | undefined;
}

export interface ReturnTypeError {
    error: string;
}

/**
 * Service that provides completion items and validation errors from a string expression.
 * It is constructed with the deserialized scope instance. This instance can created with
 * {@link ScopeDeserializer}. Json that is passed to the {@link ScopeDeserializer} is provided
 * by a Kraken backend deserializer and shouldn't be mocked or constructed manually.
 * ```
 * const scopeDeserializer = new ScopeDeserializer(typeRegistryJson)
 * const service = new KelService(
 *   scopeDeserializer.provideScope(scopeJson)
 * );
 * const validation = service.provideValidation("1 > amount")
 * const completion = service.provideCompletion("1 > amount", {line: 1, column: 11})
 * const returnTypeError = service.validateReturnType("1 > amount", "Boolean")
 * ```
 *
 * @see {@link ScopeDeserializer}
 */
export class KelService {

    parserCache: Cache<string, { node: Node, parser: KrakenParser }> = new Cache();

    constructor(private readonly scope: Scope) { }

    provideCompletion(expression: string, location: CursorLocation): Completion {
        const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e));
        const visitor = new AutocompleteNodeVisitor(location);
        visitor.visit(node);
        const completion = visitor.getCompletionItems();
        if (!completion) {
            return {
                node, completions: []
            };
        }
        return completion;
    }

    provideValidation(expression: string): Validation {
        const { node, parser } = this.parserCache.getOrCompute(expression, e => this.compute(e));
        const validation = new ValidatingNodeVisitor();
        validation.visit(node);
        const semantic = validation.getErrors();
        const syntax: SyntaxError[] = parser.getSyntaxErrors().map(err => this.getSyntaxError(err));
        return { semantic, syntax };
    }

    /**
     * Validates expression type against provided type name
     *
     * @param {string} expression                   KEL expression
     * @param {PrimitiveTypeName} expectedTypeName  only primitive data types can be asserted
     * @returns {(ReturnTypeError | undefined)}     if types matches, `undefined` will be returned.
     *                                              Otherwise error will be returned
     */
    validateReturnType(expression: string, expectedTypeName: PrimitiveTypeName): ReturnTypeError | undefined {
        const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e));
        const expectedType = Type.createPrimitive(expectedTypeName);
        const assignable = expectedType.isAssignableFrom(node.evaluationType);
        if (!assignable) {
            return {
                error: `Expected return type is '${expectedTypeName}', actual is '${node.evaluationType.stringify()}'`
            };
        }
        return;
    }

    private getSyntaxError(err: KELSyntaxError): SyntaxError {
        if (err.offendingSymbol) {

            const s: SyntaxError = {
                error: err.message,
                range: {
                    start: {
                        line: err.offendingSymbol.line,
                        column: err.offendingSymbol.startIndex
                    },
                    end: {
                        line: err.offendingSymbol.line,
                        column: err.offendingSymbol.stopIndex
                    }
                }
            };
            return s;
        }
        return { error: err.message, range: undefined };
    }

    private compute(expression: string): { node: Node, parser: KrakenParser } {
        const parser = createParser(expression);
        const tree = parser.expression();
        const visitor = new KelTraversingVisitor(this.scope);
        return { node: visitor.visit(tree), parser: parser };
    }
}
