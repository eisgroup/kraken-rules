import { ErrorNode, Interval, ParserRuleContext } from 'kraken-expression-language-visitor'
import { isEmptyKelNode, isErrorKelNode, isValidKelNode, Node } from './Node'

/**
 * Represents a cursor position inside expression text
 */
export type Cursor = {
    line: number
    column: number
}

/**
 * Represents a tokenized part of expression text.
 * Token will never take more than a single line because there are no tokens defined in KEL grammar with new lines
 */
export type Token = {
    line: number
    charPositionInLine: number
    text?: string
}

/**
 * Represents a range of text between two cursors. If start and end cursors are equal then range is empty.
 * Range can take multiple lines.
 */
export type Range = {
    start: Cursor
    end: Cursor
}

export function emptyRangeAt(cursor: Cursor): Range {
    return { start: cursor, end: cursor }
}
export function rangeBetween(startCursor: Cursor, endCursor: Cursor): Range {
    return { start: startCursor, end: endCursor }
}
export function cursorAtTheBeginningOf(token: Token): Cursor {
    return { line: token.line, column: token.charPositionInLine }
}
export function cursorAtTheEndOf(token: Token): Cursor {
    return { line: token.line, column: token.charPositionInLine + (token.text ?? '').length }
}
export function isCursorOnOrAfterSecondCursor(cursor1: Cursor, cursor2: Cursor): boolean {
    return cursor1.line > cursor2.line || (cursor1.line === cursor2.line && cursor1.column >= cursor2.column)
}
export function isCursorInsideNode(cursor: Cursor, node: Node): boolean {
    const range = getRange(node)
    if (!range) {
        return false
    }
    return (
        range.start.line <= cursor.line &&
        range.start.column <= cursor.column &&
        range.end.line >= cursor.line &&
        range.end.column > cursor.column
    )
}
export function isCursorImmediatelyAfterNode(cursor: Cursor, node: Node): boolean {
    const range = getRange(node)
    if (!range) {
        return false
    }
    return range.end.line === cursor.line && range.end.column === cursor.column
}
export function isCursorSomewhereAfterNode(cursor: Cursor, node: Node): boolean {
    const range = getRange(node)
    if (!range) {
        return false
    }
    return cursor.line > range.end.line || (cursor.line === range.end.line && cursor.column >= range.end.column)
}

/**
 *
 * @param node
 * @return a text that was used to parse this node from.
 *         Note, that text will not contain skipped symbols like new lines or empty spaces
 */
export function getText(node: Node): string {
    if (isValidKelNode(node)) {
        return getTokenText(node.context)
    }
    if (isErrorKelNode(node)) {
        return getErrorNodeText(node.errorNode)
    }
    return ''
}

export function getTokenText(ctx: ParserRuleContext): string {
    const start = ctx.start.startIndex
    const end = ctx.stop?.stopIndex ?? ctx.start.startIndex + 1
    const inputStream = ctx.start.inputStream
    return inputStream ? inputStream.getText(Interval.of(start, end)) : ''
}

function getErrorNodeText(node: ErrorNode): string {
    const inputStream = node.symbol.inputStream
    const start = node.symbol.startIndex
    const end = node.symbol.stopIndex
    return inputStream ? inputStream.getText(Interval.of(start, end)) : ''
}

/**
 *
 * @param node
 * @return inclusive range of the node
 */
export function getRange(node: Node): Range | undefined {
    if (isValidKelNode(node)) {
        return toRange(node.context.start, node.context.stop)
    }
    if (isErrorKelNode(node)) {
        return toRange(node.errorNode.symbol, node.errorNode.symbol)
    }
    if (isEmptyKelNode(node)) {
        return node.range
    }
    return
}
function toRange(start: Token, stop: Token | undefined): Range {
    const end = stop ?? start
    return {
        start: {
            line: start.line,
            column: start.charPositionInLine,
        },
        end: {
            line: end.line,
            column: end.charPositionInLine + (end.text ? end.text.length : 0),
        },
    }
}
