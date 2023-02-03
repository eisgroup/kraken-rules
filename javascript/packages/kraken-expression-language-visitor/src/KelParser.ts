import {
    ANTLRErrorListener,
    CharStreams,
    CommonTokenStream,
    DefaultErrorStrategy,
    InputMismatchException,
    NoViableAltException,
    Parser,
    RecognitionException,
    Recognizer,
    RuleContext,
    Token,
    Vocabulary,
} from 'antlr4ts'

import { ExpressionContext, Kel, TypeContext } from './antlr/generated/Kel'
import { KelLexer } from './KelLexer'

/**
 * Parses KEL expression to tree based structure with antlr4 generated context.
 * This tree can be traversed with visitor {@link KelVisitor} implementation.
 */
export class KelParser {
    private parserEl: KelParserErrorListener
    private lexerEl: KelLexerErrorListener
    private parser: Kel

    constructor(expression: string) {
        const inputStream = CharStreams.fromString(expression)

        const lexer = new KelLexer(inputStream)
        const lexerErrorListener = new KelLexerErrorListener()
        lexer.removeErrorListeners()
        lexer.addErrorListener(lexerErrorListener)
        const tokenStream = new CommonTokenStream(lexer)

        const kelParser = new Kel(tokenStream)
        const parserErrorListener = new KelParserErrorListener()
        kelParser.removeErrorListeners()
        kelParser.addErrorListener(parserErrorListener)

        // error strategy
        kelParser.errorHandler = new ErrorParsingStrategy()

        this.parser = kelParser
        this.parserEl = parserErrorListener
        this.lexerEl = lexerErrorListener
    }
    getSyntaxErrors(): SyntaxError[] {
        return [...this.lexerEl.getSyntaxErrors(), ...this.parserEl.getSyntaxErrors()]
    }
    parseExpression(): ExpressionContext {
        return this.parser.expression()
    }
    parseType(): TypeContext {
        return this.parser.type()
    }
}

export class KelParserErrorListener implements ANTLRErrorListener<Token> {
    private errors: SyntaxError[] = []

    syntaxError(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        _recognizer: Recognizer<Token, any>,
        offendingSymbol: Token | undefined,
        line: number,
        charPositionInLine: number,
        msg: string,
        e: RecognitionException | undefined,
    ): void {
        let os
        if (offendingSymbol) {
            os = {
                line: offendingSymbol.line,
                startIndex: offendingSymbol.startIndex,
                stopIndex: offendingSymbol.stopIndex,
            }
        } else {
            os = {
                line: line,
                startIndex: charPositionInLine,
                stopIndex: charPositionInLine,
            }
        }
        this.errors.push({
            context: e?.context,
            message: msg,
            offendingSymbol: os,
        })
    }

    getSyntaxErrors(): SyntaxError[] {
        return this.errors
    }
}

export class KelLexerErrorListener implements ANTLRErrorListener<number> {
    private errors: SyntaxError[] = []

    syntaxError(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        _recognizer: Recognizer<number, any>,
        _offendingSymbol: number | undefined,
        line: number,
        charPositionInLine: number,
        msg: string,
        e: RecognitionException | undefined,
    ): void {
        const os = {
            line: line,
            startIndex: charPositionInLine,
            stopIndex: charPositionInLine,
        }
        this.errors.push({
            context: e?.context,
            message: msg,
            offendingSymbol: os,
        })
    }

    getSyntaxErrors(): SyntaxError[] {
        return this.errors
    }
}

export interface OffendingSymbol {
    line: number
    startIndex: number
    stopIndex: number
}

export interface SyntaxError {
    context?: RuleContext
    message: string
    offendingSymbol: OffendingSymbol
}

class ErrorParsingStrategy extends DefaultErrorStrategy {
    reportInputMismatch(recognizer: Parser, e: InputMismatchException): void {
        const offendingToken = e.getOffendingToken(recognizer)
        const invalidTokenText = offendingToken?.text
        const expected = e.expectedTokens

        let expectedTokens: string[] = []

        for (const interval of expected?.intervals ?? []) {
            const { a, b } = interval
            if (a === b) {
                const l = this.resolveText(recognizer.vocabulary, a)
                if (l) {
                    expectedTokens = expectedTokens.concat(l)
                }
            } else {
                for (let i = a; i <= b; i++) {
                    // expected token should not be same as offending token
                    if (i !== offendingToken?.type) {
                        continue
                    }
                    const intervalLiterals = this.resolveText(recognizer.vocabulary, i)
                    if (intervalLiterals) {
                        expectedTokens = expectedTokens.concat(intervalLiterals)
                    }
                }
            }
        }
        let message = ''
        if (offendingToken && offendingToken.type === Kel.EOF) {
            message = 'Unexpected end of expression'
        } else if (!expectedTokens.length) {
            message = 'Syntax error occurred, please check the correctness of an expression'
        } else {
            message = `Invalid token '${invalidTokenText}'. Expected ${expectedTokens.join(', ')}`
        }

        recognizer.notifyErrorListeners(message, offendingToken ?? null, e)
    }

    private resolveText(vocabulary: Vocabulary, tokenType: number): string[] {
        const literal = vocabulary.getLiteralName(tokenType)
        if (literal) {
            return [literal]
        }

        const q = (...t: string[]) => t.map(text => `'${text}'`)
        switch (tokenType) {
            case Kel.OP_IN:
            case Kel.IN:
                return q('in')
            case Kel.DOT:
                return q('.')
            case Kel.ELSE:
                return q('else')
            case Kel.EVERY:
                return q('every')
            case Kel.FALSE:
                return q('false')
            case Kel.FOR:
                return q('for')
            case Kel.IF:
                return q('if')
            case Kel.MATCHES:
                return q('matches')
            case Kel.NOT:
                return q('not')
            case Kel.NULL:
                return q('null')
            case Kel.OP_ADD:
                return q('+')
            case Kel.OP_AND:
                return q('and', '&&')
            case Kel.OP_DIV:
                return q('/')
            case Kel.OP_EQUALS:
                return q('=', '==')
            case Kel.OP_EXP:
                return q('**')
            case Kel.OP_INSTANCEOF:
                return q('instanceof')
            case Kel.OP_LESS:
                return q('<')
            case Kel.OP_LESS_EQUALS:
                return q('<=')
            case Kel.OP_MINUS:
                return q('-')
            case Kel.OP_MOD:
                return q('%')
            case Kel.OP_MORE:
                return q('>')
            case Kel.OP_MORE_EQUALS:
                return q('>=')
            case Kel.OP_MULT:
                return q('*')
            case Kel.OP_NEGATION:
                return q('not', '!')
            case Kel.OP_NOT_EQUALS:
                return q('!=')
            case Kel.OR:
            case Kel.OP_OR:
                return q('or', '||')
            case Kel.OP_PIPE:
                return q('|')
            case Kel.OP_QUESTION:
                return q('?')
            case Kel.OP_TYPEOF:
                return q('typeof')
            case Kel.RETURN:
                return q('return')
            case Kel.SATISFIES:
                return q('satisfies')
            case Kel.THIS:
                return q('this')
            case Kel.TRUE:
                return q('true')
        }
        return []
    }

    reportNoViableAlternative(recognizer: Parser, e: NoViableAltException): void {
        const tokens = recognizer.inputStream
        let input
        if (tokens) {
            if (e.startToken.type === Kel.EOF) {
                input = ''
            } else {
                input = tokens.getTextFromRange(e.startToken, e.getOffendingToken())
            }
        } else {
            input = ''
        }

        const message = `Invalid token ${this.escapeWSAndQuote(input)}`
        this.notifyErrorListeners(recognizer, message, e)
    }
}
