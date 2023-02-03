import { KelParser } from 'kraken-expression-language-visitor'
import { AstGeneratingVisitor } from '../../src/visitor/AstGeneratingVisitor'
import {
    getText,
    emptyRangeAt,
    rangeBetween,
    cursorAtTheBeginningOf,
    cursorAtTheEndOf,
    isCursorOnOrAfterSecondCursor,
    isCursorInsideNode,
    isCursorImmediatelyAfterNode,
    isCursorSomewhereAfterNode,
    getRange,
} from '../../src/visitor/NodeUtils'
import { instance } from '../test-data/test-data'

describe('NodeUtils', () => {
    it('should resolve text', () => {
        const node = new AstGeneratingVisitor(instance.policy).visit(new KelParser('a.').parseExpression())
        const ref = node.children[0]
        expect(getText(ref)).toBe('a.')
        expect(getText(ref.children[0])).toBe('a')
        expect(getText(ref.children[1])).toBe('')
    })
    it('should create range', () => {
        const cursor = { line: 1, column: 1 }
        const endCursor = { line: 2, column: 2 }
        expect(emptyRangeAt(cursor)).toStrictEqual({ start: cursor, end: cursor })
        expect(rangeBetween(cursor, endCursor)).toStrictEqual({ start: cursor, end: endCursor })
    })
    it('should create cursor', () => {
        const token = { line: 1, charPositionInLine: 1, text: '123' }
        expect(cursorAtTheBeginningOf(token)).toStrictEqual({ line: 1, column: 1 })
        expect(cursorAtTheEndOf(token)).toStrictEqual({ line: 1, column: 4 })
    })
    it('should create range', () => {
        const node = new AstGeneratingVisitor(instance.policy).visit(new KelParser('a').parseExpression())
        expect(getRange(node)).toStrictEqual({ start: { line: 1, column: 0 }, end: { line: 1, column: 1 } })
    })
    it('should compare cursor positions', () => {
        const cursorStart = { line: 1, column: 1 }
        const cursorEnd = { line: 1, column: 2 }

        expect(isCursorOnOrAfterSecondCursor(cursorStart, cursorStart)).toBeTruthy()
        expect(isCursorOnOrAfterSecondCursor(cursorStart, cursorEnd)).toBeFalsy()
        expect(isCursorOnOrAfterSecondCursor(cursorEnd, cursorStart)).toBeTruthy()
    })
    it('should check if cursor inside node', () => {
        const node = new AstGeneratingVisitor(instance.policy).visit(new KelParser('a').parseExpression())

        expect(isCursorInsideNode({ line: 1, column: 0 }, node)).toBeTruthy()
        expect(isCursorInsideNode({ line: 1, column: 1 }, node)).toBeFalsy()
        expect(isCursorInsideNode({ line: 2, column: 0 }, node)).toBeFalsy()
        expect(isCursorImmediatelyAfterNode({ line: 1, column: 1 }, node)).toBeTruthy()
        expect(isCursorImmediatelyAfterNode({ line: 2, column: 0 }, node)).toBeFalsy()
        expect(isCursorImmediatelyAfterNode({ line: 1, column: 0 }, node)).toBeFalsy()
        expect(isCursorSomewhereAfterNode({ line: 1, column: 1 }, node)).toBeTruthy()
        expect(isCursorSomewhereAfterNode({ line: 2, column: 0 }, node)).toBeTruthy()
        expect(isCursorSomewhereAfterNode({ line: 2, column: 2 }, node)).toBeTruthy()
        expect(isCursorSomewhereAfterNode({ line: 1, column: 0 }, node)).toBeFalsy()
    })
})
