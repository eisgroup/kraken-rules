import { KelParser } from 'kraken-expression-language-visitor'
import { AstGeneratingVisitor } from '../../../src/visitor/AstGeneratingVisitor'
import { TypeHintVisitor } from '../../../src/visitor/hint/TypeHintVisitor'
import { instance } from '../../test-data/test-data'

describe('TypeHintVisitor', () => {
    function getHints(expression: string) {
        const v = new AstGeneratingVisitor(instance.policy)
        const parse = (ex: string) => new KelParser(ex).parseExpression()
        const node = v.visit(parse(expression))
        const visitor = new TypeHintVisitor()
        visitor.visit(node)
        return visitor.getHints()
    }

    it('should not find hints for Policy', () => {
        const hints = getHints('Policy')
        expect(hints).toHaveLength(0)
    })
    it('should find hints for parties.roles', () => {
        const hints = getHints('parties.roles')
        expect(hints).toHaveLength(1)
        const [parties] = hints
        expect(parties.hint).toBe('[*]')
        expect(parties.location.line).toBe(1)
        expect(parties.location.column).toBe(7)
    })
    it('should find hints for parties.roles.limit', () => {
        const hints = getHints('parties.roles.limit')
        expect(hints).toHaveLength(2)
        const [roles, parties] = hints
        expect(parties.hint).toBe('[*]')
        expect(parties.location.line).toBe(1)
        expect(parties.location.column).toBe(7)

        expect(roles.hint).toBe('[*]')
        expect(roles.location.line).toBe(1)
        expect(roles.location.column).toBe(13)
    })
    it('should skip when [*] is present in parties[*].roles.limit', () => {
        const hints = getHints('parties[*].roles.limit')
        expect(hints).toHaveLength(1)
        const [parties] = hints
        expect(parties.hint).toBe('[*]')
        expect(parties.location.line).toBe(1)
        expect(parties.location.column).toBe(16)
    })
    it('should skip when [Count(roles) = 1] is present in parties[Count(roles) = 1].roles.limit', () => {
        const hints = getHints('parties[Count(roles) = 1].roles.limit')
        expect(hints).toHaveLength(1)
        const [parties] = hints
        expect(parties.hint).toBe('[*]')
        expect(parties.location.line).toBe(1)
        expect(parties.location.column).toBe(31)
    })
    it('should find hints for GetCoverages(Policy).code', () => {
        const hints = getHints('GetCoverages(Policy).code')
        expect(hints).toHaveLength(1)
        const [parties] = hints
        expect(parties.hint).toBe('[*]')
        expect(parties.location.line).toBe(1)
        expect(parties.location.column).toBe(20)
    })
})
