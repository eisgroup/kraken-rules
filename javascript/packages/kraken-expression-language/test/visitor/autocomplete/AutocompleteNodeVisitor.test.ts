import { KelParser } from 'kraken-expression-language-visitor'
import { Scope } from '../../../src/scope/Scope'
import { AstGeneratingVisitor } from '../../../src/visitor/AstGeneratingVisitor'
import { AutocompleteNodeVisitor } from '../../../src/visitor/autocomplete/AutocompleteNodeVisitor'
import { Completion, CompletionItem } from '../../../src/visitor/autocomplete/CompletionItem'
import { instance } from '../../test-data/test-data'

describe('AutocompleteNodeVisitor', () => {
    it('should find node on empty expression', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //           █
            expression: '',
            cursor: '1:0',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should find node on valid expression start', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //           █
            expression: 'Policy',
            cursor: '1:0',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should find node on valid expression middle', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //              █
            expression: 'Policy.policyNumber',
            cursor: '1:3',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should find node on valid path expression middle of property', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                         █
            expression: 'Policy.policyNumber',
            cursor: '1:14',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should find node after binary expression', () => {
        const completion = getCompletionItems({
            //                     1111111111222
            //           01234567890123456789012
            //                                 █
            expression: 'Policy.policyNumber > ',
            cursor: '1:22',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve from incomplete path in incomplete property', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                  █
            expression: 'Policy.',
            cursor: '1:7',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve from incomplete path in object', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                 █
            expression: 'Policy.',
            cursor: '1:6',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it("should resolve autocomplete from 'this'", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                █
            expression: 'this.a',
            cursor: '1:5',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it("should resolve autocomplete from incomplete 'this'", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                █
            expression: 'this.',
            cursor: '1:5',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it("should resolve autocomplete from incomplete 'this' from object", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //               █
            expression: 'this.',
            cursor: '1:4',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it("should resolve autocomplete from 'this' in filter", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                           █
            expression: 'riskItems?[this.a]',
            cursor: '1:16',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve autocomplete from path in filter', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                             █
            expression: 'riskItems?[Policy.a]',
            cursor: '1:18',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve autocomplete from incomplete path in filter', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                            █
            expression: 'riskItems?[Policy.]',
            cursor: '1:17',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it("should resolve autocomplete from 'this' in filter or access by index", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                          █
            expression: 'riskItems[this.a]',
            cursor: '1:15',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it("should resolve autocomplete from empty 'this' in filter or access by index", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                          █
            expression: 'riskItems[this.]',
            cursor: '1:15',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve path and resolve path scope variables with nested property', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                          █
            expression: 'Policy.referer.',
            cursor: '1:15',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve filter', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                             █
            expression: 'riskItems[model = v]',
            cursor: '1:18',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve explicit filter', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                      █
            expression: 'riskItems?[m]',
            cursor: '1:11',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve implicit filter or access by index', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                     █
            expression: 'riskItems[m]',
            cursor: '1:10',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve explicit empty filter', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                      █
            expression: 'riskItems?[]',
            cursor: '1:11',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve implicit empty filter or access by index', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //                     █
            //           012345678901234567890
            expression: 'riskItems[]',
            cursor: '1:10',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })

    it('should resolve from global when in filter collection', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //                    █
            //           012345678901234567890
            expression: 'riskItems[]',
            cursor: '1:9',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve from incomplete filter or access by index', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //                     █
            //           012345678901234567890
            expression: 'riskItems[',
            cursor: '1:10',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve from incomplete filter or access by index and path', () => {
        const completion = getCompletionItems({
            //                     11111111112
            //                         █
            //           012345678901234567890
            expression: 'riskItems[this.',
            cursor: '1:14',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve from error node parent', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                            █
            expression: 'riskItems[this > ]',
            cursor: '1:17',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve nothing on for iteration variable', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //               █
            expression: 'for r in riskItems return r',
            cursor: '1:4',
            scopeName: 'Policy',
        })
        expect(completion.completions).toHaveLength(0)
    })
    it('should resolve for iteration', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                     █
            expression: 'for r in riskItems return ',
            cursor: '1:26',
            scopeName: 'Policy',
        })
        expect(completion.completions.map(c => c.text)).toContain('r')
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve closest node correctly', () => {
        const completion = getCompletionItems({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                                █
            expression: 'Count(for r in riskItems return r) > ',
            cursor: '1:37',
            scopeName: 'Policy',
        })
        expect(completion.completions.find(c => c.text === 'r')).toBeUndefined()
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve closest node correctly from nested with space', () => {
        const completion = getCompletionItems({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                             █
            expression: 'IsEmpty(for r in riskItems return r) = true',
            cursor: '1:34',
            scopeName: 'Policy',
        })
        expect(completion.completions.map(c => c.text)).toContain('r')
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve for iteration collection', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                    █
            expression: 'for r in ',
            cursor: '1:9',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve for iteration with path', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                       █
            expression: 'for r in riskItems return r.',
            cursor: '1:28',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve for iteration with non existing path', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                       █
            expression: 'for r in riskItems return o.',
            cursor: '1:28',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve array type element or inline array keyword for known in operation', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                  █
            expression: "'a' in policies",
            cursor: '1:7',
            scopeName: 'Policy',
        })
        expect(toString(completion)).toMatchSnapshot()
    })
    it('should resolve variables for autocomplete', () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                     █
            expression: 'set r to riskItems return ',
            cursor: '1:26',
            scopeName: 'Policy',
        })
        expect(completion.completions.map(c => c.text)).toContain('r')
        expect(toString(completion)).toMatchSnapshot()
    })
})

function toString(c: Completion | undefined): string[] {
    function comparable(com: CompletionItem): string {
        let info = ''
        if (com.type === 'function') {
            const { evaluationType, parameterTypes } = com
            info = `(${parameterTypes.map(p => p.typeName)}): ${evaluationType.typeName}`
        }
        if (com.type === 'reference') {
            const { evaluationType } = com
            info = ` : ${evaluationType.typeName}`
        }
        return `[${com.type}] ${com.text}${info}`
    }

    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    return c!.completions
        .sort((c1, c2) => {
            return comparable(c1).localeCompare(comparable(c2), 'en', { sensitivity: 'variant' })
        })
        .map(ci => comparable(ci))
}

function getCompletionItems(p: { expression: string; scopeName: string; cursor: string }): Completion {
    const [line, column] = p.cursor.split(':')
    const visitor = new AutocompleteNodeVisitor({ column: Number(column), line: Number(line) })
    const parser = new KelParser(p.expression)
    const traversingVisitor = new AstGeneratingVisitor(findScope(p.scopeName))
    const tree = traversingVisitor.visit(parser.parseExpression())
    visitor.visit(tree)
    return visitor.getCompletionItems()
}

function findScope(contextDefinition: string): Scope {
    const scope = Object.values(instance).find(s => s.type.name === contextDefinition)
    if (!scope) throw new Error(`Cannot find scope for a context definition ${contextDefinition}`)
    return scope
}
