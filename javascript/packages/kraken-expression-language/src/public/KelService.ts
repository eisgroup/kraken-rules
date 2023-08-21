import { BooleanContext, KelParser, SyntaxError as KELSyntaxError } from 'kraken-expression-language-visitor'
import { Scope } from '../scope/Scope'
import { AstGeneratingVisitor } from '../visitor/AstGeneratingVisitor'
import { isEmptyKelNode, Node } from '../visitor/Node'
import { AutocompleteNodeVisitor } from '../visitor/autocomplete/AutocompleteNodeVisitor'
import { Completion } from '../visitor/autocomplete/CompletionItem'
import { Cursor, Range, getRange } from '../visitor/NodeUtils'
import { LocationInfo } from '../visitor/type-at-location/LocationInfo'
import { InfoAtLocationNodeVisitor } from '../visitor/type-at-location/NodeAtLocationNodeVisitor'
import { ValidatingNodeVisitor } from '../visitor/validation/ValidatingVisitor'
import { Cache } from './Cache'
import { AstMessage } from '../visitor/validation/AstMessage'
import { Type } from '../type/Types'
import { TypeHintVisitor } from '../visitor/hint/TypeHintVisitor'
import { TypeHint } from '../visitor/hint/TypeHint'

export interface Validation {
    messages: ValidationMessage[]
}

export interface ValidationConfiguration {
    /**
     * Configure validation for expression having a simple 'true' literal and
     * no additional logic.
     */
    allowsTrue: boolean
}

export interface ValidationMessage {
    message: string
    /**
     * if value is `undefined`, then it is impossible to
     * determine range of error node in expression.
     */
    range: Range | undefined
    severity: 'ERROR' | 'WARNING' | 'INFO'
}

export interface ReturnTypeError {
    error: string
}

/**
 * Service that provides completion items and validation errors from a string expression.
 * It is constructed with the deserialized scope instance. This instance can be created with
 * {@link ScopeDeserializer}. Json that is passed to the {@link ScopeDeserializer} is provided
 * by a Kraken backend deserializer and shouldn't be mocked or constructed manually.
 * ```
 * const scopeDeserializer = new ScopeDeserializer(typeRegistryJson)
 * const service = new KelService(
 *   scopeDeserializer.provideScope(scopeJson)
 * );
 * const validation = service.provideValidation("1 > amount")
 * const validation = service.provideValidation("1 > amount", { allowsTrue: false })
 * const completion = service.provideCompletion("1 > amount", {line: 1, column: 11})
 * const returnTypeError = service.validateReturnType("1 > amount", "Boolean")
 * ```
 *
 * @see {@link ScopeDeserializer}
 */
export class KelService {
    parserCache: Cache<string, { node: Node; parser: KelParser }> = new Cache()

    constructor(private readonly scope: Scope) {}

    provideTypeHints(expression: string): TypeHint[] {
        const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e))
        const visitor = new TypeHintVisitor()
        visitor.visit(node)
        return visitor.getHints()
    }

    provideCompletion(expression: string, cursor: Cursor): Completion {
        const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e))
        const visitor = new AutocompleteNodeVisitor(cursor)
        visitor.visit(node)
        return visitor.getCompletionItems()
    }

    provideValidation(expression: string, config?: ValidationConfiguration): Validation {
        const { node, parser } = this.parserCache.getOrCompute(expression, e => this.compute(e))
        const validation = new ValidatingNodeVisitor()
        validation.visit(node)
        const semantic = validation.getMessages().map(err => KelService.convertAstMessageToValidationMessage(err))
        const syntax = parser.getSyntaxErrors().map(err => KelService.convertSyntaxErrorToValidationMessage(err))
        const additional = this.provideAdditionalValidations(expression, config)

        return {
            messages: [...syntax, ...semantic, ...additional],
        }
    }

    /**
     * Resolves information about node at provided cursor.
     * For primitive, operators and keywords this method will return `undefined`.
     * For other type of nodes it will return either function information either type information.
     *
     * @param {string} expression
     * @param {Cursor} cursor
     * @returns {(LocationInfo | undefined)}
     * @see {@link LocationInfo)
     */
    provideInfoAtLocation(expression: string, cursor: Cursor): LocationInfo | undefined {
        const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e))
        const visitor = new InfoAtLocationNodeVisitor(cursor)
        visitor.visit(node)
        return visitor.getLocationInfo()
    }

    /**
     * Returns true if expression is semantically empty.
     * Semantically empty expression is when it does not have any logic.
     * Semantically empty expression can be when expression consists only of symbols that does not express logic,
     * like empty spaces, comments or new lines.
     * Note, that semantically empty expression is still considered as a valid expression in KEL.
     *
     * @param expression KEL expression
     */
    isEmpty(expression: string): boolean {
        const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e))
        return isEmptyKelNode(node)
    }

    resolveType(typeName: string): Type | undefined {
        return this.scope.resolveTypeOf(typeName)
    }

    resolveExpressionEvaluationType(expression: string): Type {
        return this.parserCache.getOrCompute(expression, e => this.compute(e)).node.evaluationType
    }

    private provideAdditionalValidations(expression: string, config?: ValidationConfiguration): ValidationMessage[] {
        const messages: ValidationMessage[] = []

        if (!config?.allowsTrue) {
            const { node } = this.parserCache.getOrCompute(expression, e => this.compute(e))

            if (
                node.nodeType === 'BOOLEAN' &&
                node.context instanceof BooleanContext &&
                node.context.BOOL().text.toLowerCase() === 'true'
            ) {
                messages.push({
                    message: "Redundant literal value 'true'. Expression is 'true' by default.",
                    range: getRange(node),
                    severity: 'INFO',
                })
            }
        }

        return messages
    }

    private compute(expression: string): { node: Node; parser: KelParser } {
        const parser = new KelParser(expression)
        const tree = parser.parseExpression()
        const visitor = new AstGeneratingVisitor(this.scope)
        return { node: visitor.visit(tree), parser: parser }
    }

    private static convertAstMessageToValidationMessage(err: AstMessage): ValidationMessage {
        return {
            message: err.message,
            range: err.range,
            severity: err.severity,
        }
    }

    private static convertSyntaxErrorToValidationMessage(err: KELSyntaxError): ValidationMessage {
        return {
            message: err.message,
            range: {
                start: {
                    line: err.offendingSymbol.line,
                    column: err.offendingSymbol.startIndex,
                },
                end: {
                    line: err.offendingSymbol.line,
                    column: err.offendingSymbol.stopIndex,
                },
            },
            severity: 'ERROR',
        }
    }
}
