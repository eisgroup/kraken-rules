import { KelParser } from 'kraken-expression-language-visitor'
import { Scope } from '../../../src/scope/Scope'
import { AstGeneratingVisitor } from '../../../src/visitor/AstGeneratingVisitor'
import { AstMessage } from '../../../src/visitor/validation/AstMessage'
import { ValidatingNodeVisitor } from '../../../src/visitor/validation/ValidatingVisitor'
import { instance } from '../../test-data/test-data'

describe('ValidatingVisitor', () => {
    it('should find error on third line', () => {
        const expression = `
Policy.createdFromPolicyRev = null
and
policies[0]
`
        const errors = findMessages(expression, 'Policy')
        expect(errors).toHaveLength(1)
        expect(errors[0].range?.start.line).toBe(4)
        expect(errors[0].range?.start.column).toBe(0)
        expect(errors[0].range?.end.line).toBe(4)
        expect(errors[0].range?.end.column).toBe(11)
    })
    it('should find error in function', () => {
        const expression = `
Count(false
and
'abc'
)
`
        const errors = findMessages(expression, 'Policy')
        expect(errors).toHaveLength(1)
        expect(errors[0].range?.start.line).toBe(4)
        expect(errors[0].range?.start.column).toBe(0)
        expect(errors[0].range?.end.line).toBe(4)
        expect(errors[0].range?.end.column).toBe(5)
    })
    it('should find multiline error', () => {
        const expression = `
Count(false
>
'abc'
)
`
        const errors = findMessages(expression, 'Policy')
        expect(errors).toHaveLength(1)
        expect(errors[0].range?.start.line).toBe(2)
        expect(errors[0].range?.start.column).toBe(6)
        expect(errors[0].range?.end.line).toBe(4)
        expect(errors[0].range?.end.column).toBe(5)
    })
    it('visit_not_equals', () => {
        tcValid('1 = 0', 'Policy')
        tcValid('1 != createdFromPolicyRev', 'Policy')
        tcNotValid("'a' != createdFromPolicyRev", 'Policy')
    })
    it('visit_matches_reg_exp', () => {
        tcValid("'1' matches 'regexp'", 'Policy')
        tcNotValid("1 matches 'regexp'", 'Policy')
    })
    it('visit_function', () => {
        tcValid('Today()', 'Policy')
        tcValid('Now()', 'Policy')
        tcNotValid('Date(1)', 'Policy')
        tcNotValid("Date('1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1')", 'Policy')
        tcNotValid('Now(1)', 'Policy')

        tcValid('Min(this.limitAmount, 10) + 20', 'COLLCoverage')
        tcValid('Min(2022-01-01, 2022-01-01) > 2022-01-01', 'COLLCoverage')
        tcNotValid("Min('10', '10')", 'COLLCoverage')
        tcNotValid('Min(2022-01-01, 2022-01-01) > 10', 'COLLCoverage')
        tcValid('Min(Min(10, 20), Min(10, 20)) + 20', 'COLLCoverage')
        tcNotValid('Min(Min(10, 20), Min(2022-01-01, 2022-01-01)) + 20', 'COLLCoverage')
    })
    it('visit_if condition not boolean', () => {
        tcValid('if(true) then 1', 'Policy')
        tcValid('if(currentQuoteInd) then 1', 'Policy')
        tcNotValid('if(null) then 1', 'Policy')
        tcNotValid("if('text') then 1", 'Policy')
    })
    it('visit_collection_filter', () => {
        tcNotValid('policies?[', 'Policy')
        tcNotValid('policies[', 'Policy')
        tcNotValid('policies?[]', 'Policy')
        tcNotValid('policies[]', 'Policy')
        tcNotValid("policies?['p01']", 'Policy')
        tcValid("policies[this = 'p01']", 'Policy')
        tcNotValid("context.external.data[limitAmount > 'string']", 'COLLCoverage')
        tcValid("context.external.data[unknownLimitAmount > 'string']", 'COLLCoverage')
        tcValid("context.external.data[this.limitAmount > 'string']", 'COLLCoverage')
        tcValid("context.external.data[this.unknownLimitAmount > 'string']", 'COLLCoverage')
        tcValid("context.external.data[this > 'string']", 'COLLCoverage')
    })
    it('prop not identifier or access by index', () => {
        tcNotValid("policies.'p01'", 'Policy')
    })
    it('visit access by index prop not identifier or access by index', () => {
        tcNotValid("policies['p01']", 'Policy')
    })
    it('general cases', () => {
        tcWithSyntaxErrorCount('a.b.c.d', 'AnubisCoverage', 1)
        tcValid('this in Policy.riskItems[0].collCoverages', 'COLLCoverage')
        tcValid('Policy.riskItems[0].collCoverages[0] in Vehicle.collCoverages', 'COLLCoverage')
        tcValid('Vehicle.collCoverages[0] in Vehicle.collCoverages', 'COLLCoverage')
        tcValid('this in Vehicle.collCoverages', 'COLLCoverage')
        tcValid('BillingAddress = addressInfo', 'Insured')
        tcValid('addressInfo != BillingAddress', 'Insured')
        tcValid('addressInfo = BillingAddress', 'Insured')
        tcValid('addressInfo = AddressInfo', 'Insured')
        tcValid('AddressInfo = addressInfo', 'Insured')
        tcValid('if(true) then AddressInfo else addressInfo', 'Insured')
        tcValid('if(true) then addressInfo else AddressInfo', 'Insured')
        tcValid('if(true) then BillingAddress else addressInfo', 'Insured')
        tcValid('if(true) then addressInfo else BillingAddress', 'Insured')
        tcValid('{AddressInfo} = {addressInfo}', 'Insured')
        tcValid('CreditCardInfo.cardCreditLimitAmount = 1', 'Insured')
        tcValid('1 = CreditCardInfo.cardCreditLimitAmount', 'Insured')
        tcValid('-CreditCardInfo.cardCreditLimitAmount = 1', 'Insured')
        tcValid('-CreditCardInfo.cardCreditLimitAmount', 'Insured')
        tcNotValid('this.', 'Insured')
    })
    it('iteration', () => {
        tcValid("every c in Vehicle.collCoverages satisfies c.code == 'a'", 'COLLCoverage')
        tcValid("some c in Vehicle.collCoverages satisfies c.code == 'a'", 'COLLCoverage')
        tcNotValid('every c in Vehicle.collCoverages satisfies c.code', 'COLLCoverage')
        tcNotValid('some c in Vehicle.collCoverages satisfies c.code', 'COLLCoverage')
        tcValid('for c in Vehicle.collCoverages return c.code', 'COLLCoverage')
        tcNotValid('for code in Vehicle.collCoverages[*].code return code', 'COLLCoverage')
        tcNotValid('some code in Vehicle.collCoverages[*].code satisfies code', 'COLLCoverage')
        tcNotValid('every code in Vehicle.collCoverages[*].code satisfies code', 'COLLCoverage')
        tcNotValid('for r in Policy.riskItems return (for r in r.collCoverages return r.limitAmount)', 'COLLCoverage')
        tcValid('for r in Policy.riskItems return (for c in r.collCoverages return c.limitAmount)', 'COLLCoverage')
        tcValid('for r in Policy.riskItems return this.limitAmount == limitAmount', 'COLLCoverage')
    })
    it('should find cyclomatic complexity', () => {
        const invalid = `Policy.riskItems[1 > Sum(for i in {1} return
              i + Sum(for j in {10} return
                i + j + Sum(for k in {100} return i + j + k)
              )
            )]`
        tcNotValid(invalid, 'COLLCoverage')
        const valid = `Policy.riskItems[1 > Sum(for i in {1} return
              i + Sum(for j in {10} return
                i + j + Sum(Policy.riskItems[*].modelYear)
              )
            )]`
        tcValid(valid, 'COLLCoverage')
    })
    it('should validate partial iteration expression', () => {
        // partial for is handled by Kel parser at syntax level, so ValidatingVisitor should not return errors
        tcValid('for i', 'Policy')
        tcValid('for i in ', 'Policy')
        tcValid('for i in riskItems', 'Policy')
        tcValid('for i in riskItems return ', 'Policy')
        tcValid("for i in riskItems return ''", 'Policy')
        tcValid('some i', 'Policy')
        tcValid('some i in ', 'Policy')
        tcValid('some i in riskItems', 'Policy')
        tcValid('some i in riskItems satisfies ', 'Policy')
    })
    it('should validate partial variable', () => {
        tcNotValid('set a', 'Policy')
        tcNotValid('set a to', 'Policy')
        tcNotValid('set a to return a', 'Policy')
    })
    it('should validate partial value block', () => {
        tcNotValid('set a to 1', 'Policy')
        tcNotValid('set a to 1 return', 'Policy')
    })
    it('should validate variable type compatibility', () => {
        tcValid('' + 'set a to 1 ' + 'set b to 2 ' + 'return a+b', 'Policy')
        tcNotValid('' + 'set a to 1 ' + "set b to '2' " + 'return a+b', 'Policy')
    })
    it('should allow to use variables defined above', () => {
        tcValid('' + 'set a to 1 ' + 'set b to a ' + 'return a+b', 'Policy')
        tcNotValid('' + 'set a to b ' + 'set b to 1 ' + 'return a+b', 'Policy')
    })
    it('should resolve CCR and complex collection type of variable', () => {
        tcValid('' + 'set p to Policy ' + 'set ri to p.riskItems ' + 'return ri[0].modelYear', 'Policy')
    })
    it('should allow to assign complex iteration to variable', () => {
        tcValid('' + 'set old to some i in riskItems satisfies i.modelYear < 2000 ' + 'return old', 'Policy')
    })
    it('should not allow variable name clashes', () => {
        tcNotValid('' + 'set a to 1 ' + 'set a to 2 ' + 'return a', 'Policy')
        tcNotValid('' + 'set i to 0 ' + 'set j to for i in {1} return i ' + 'return j', 'Policy')
    })
    it('should validate negative decimal literal access by index', () => {
        tcNotValid('policies[-1]', 'Policy')
        tcValid('policies[1]', 'Policy')
    })
    it('in', () => {
        tcNotValid('Vehicle.collCoverages[0] in Vehicle.collCoverages[0]', 'COLLCoverage')
        tcNotValid('Vehicle.collCoverages in Vehicle.collCoverages[0]', 'COLLCoverage')
        tcNotValid('Vehicle.collCoverages[0] in COLLCoverage', 'COLLCoverage')
    })
    it('CCR', () => {
        // AnubisCoverage is not child of any context and it is only used as a field type,
        // therefore it cannot be used as a CCR
        tcNotValid('AnubisCoverage.limitAmount', 'Vehicle')
    })
    it('equality', () => {
        tcNotValid('Policy = addressInfo', 'Insured')
        tcNotValid('Policy == addressInfo', 'Insured')
    })
    it('unary', () => {
        tcNotValid('-Policy', 'Insured')
        tcNotValid('-addressInfo', 'Insured')
    })
    it('math', () => {
        tcNotValid('Policy - addressInfo', 'Insured')
        tcNotValid('Policy + addressInfo', 'Insured')
        tcNotValid('Policy / addressInfo', 'Insured')
        tcNotValid('Policy * addressInfo', 'Insured')
        tcNotValid('Policy % addressInfo', 'Insured')
        tcNotValid('Policy ** addressInfo', 'Insured')
    })
    it('logical', () => {
        tcNotValid('Policy in addressInfo', 'Insured')
        tcNotValid('Policy > addressInfo', 'Insured')
        tcNotValid('Policy >= addressInfo', 'Insured')
        tcNotValid('Policy < addressInfo', 'Insured')
        tcNotValid('Policy <= addressInfo', 'Insured')
    })
    it('propositional', () => {
        tcNotValid('Policy and addressInfo', 'Insured')
        tcNotValid('Policy && addressInfo', 'Insured')
        tcNotValid('Policy or addressInfo', 'Insured')
        tcNotValid('Policy || addressInfo', 'Insured')
    })
    it('decimal precision', () => {
        tcHasNoMessages('1234567890.123456', 'Insured')
        tcHasNoMessages('1234567890.', 'Insured')
        tcHasNoMessages('1234567890.000000000000000000000', 'Insured')
        tcHasWarningMessages('1234567890.1234567', 'Insured')
    })
    it('dynamic context', () => {
        tcValid('context.externalData.limitAmount > cardCreditLimitAmount', 'CreditCardInfo')
        tcValid('context.externalData.limitAmount > limitAmount', 'COLLCoverage')

        tcValid(
            'some limit in context.externalData.limitAmounts satisfies limit > limitAmount && limit == CreditCardInfo.cardCreditLimitAmount',
            'COLLCoverage',
        )

        tcValid(
            'some limit in context.externalData[*].coverages[*].limitAmount satisfies limit > limitAmount && limit == CreditCardInfo.cardCreditLimitAmount',
            'COLLCoverage',
        )
        tcValid('context.externalData.value - limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value + limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value / limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value * limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value % limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value ** limitAmount', 'COLLCoverage')
        tcValid('limitAmount in context.externalData.values', 'COLLCoverage')
        tcValid('context.externalData.value < limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value <= limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value >= limitAmount', 'COLLCoverage')
        tcValid('context.externalData.value > limitAmount', 'COLLCoverage')
        tcValid('-context.externalData.value', 'COLLCoverage')
        tcValid('context.externalData.value and haveChildren', 'Insured')
        tcValid('context.externalData.value or haveChildren', 'Insured')
        tcValid('!context.externalData.value', 'Insured')
    })
    it('unknown context', () => {
        tcNotValid('Vehicle.refsToDriver[this.name = null]', 'Policy')
        tcValid('Vehicle.refsToDriver[this = null]', 'Policy')
    })
    it('redundant cast', () => {
        tcHasInfoMessages('(COLLCoverage) this', 'COLLCoverage')
        tcHasInfoMessages('(CarCoverage) this', 'COLLCoverage')
        tcHasWarningMessages('(Policy) this', 'COLLCoverage')
        tcNotValid('(Whatever) this', 'COLLCoverage')
        tcValid('(FullCoverage) this', 'COLLCoverage')
        tcValid('(FullCoverage[]) this', 'COLLCoverage')
        tcNotValid('(FullCoverage | CarCoverage) this', 'COLLCoverage')
        tcNotValid('(<T>) this', 'COLLCoverage')
    })
})

function tcHasNoMessages(expression: string, contextDefinition: string): void | never {
    const errors = findMessages(expression, contextDefinition)
    const actual = `'${expression}' has ${errors.length} validation messages`
    expect(actual).toBe(`'${expression}' has 0 validation messages`)
}

function tcValid(expression: string, contextDefinition: string): void | never {
    const errors = findMessages(expression, contextDefinition).filter(err => err.severity === 'ERROR')
    const actual = `'${expression}' has ${errors.length} errors`
    expect(actual).toBe(`'${expression}' has 0 errors`)
}

function tcWithSyntaxErrorCount(expression: string, contextDefinition: string, errorCount: number): void | never {
    const errors = findMessages(expression, contextDefinition).filter(err => err.severity === 'ERROR')
    const actual = `'${expression}' has ${errors.length} errors`
    expect(actual).toBe(`'${expression}' has ${errorCount} errors`)
}

function tcNotValid(expression: string, contextDefinition: string): void | never {
    const errors = findMessages(expression, contextDefinition).filter(err => err.severity === 'ERROR')
    const actual = errors.length ? `'${expression}' has errors` : `'${expression}' has no errors`
    expect(actual).toBe(`'${expression}' has errors`)
}

function tcHasInfoMessages(expression: string, contextDefinition: string): void | never {
    const infos = findMessages(expression, contextDefinition).filter(err => err.severity === 'INFO')
    const actual = infos.length ? `'${expression}' has info messages` : `'${expression}' has no info messages`
    expect(actual).toBe(`'${expression}' has info messages`)
}

function tcHasWarningMessages(expression: string, contextDefinition: string): void | never {
    const warnings = findMessages(expression, contextDefinition).filter(err => err.severity === 'WARNING')
    const actual = warnings.length ? `'${expression}' has warning messages` : `'${expression}' has no warning messages`
    expect(actual).toBe(`'${expression}' has warning messages`)
}

function findMessages(expression: string, contextDefinition: string): AstMessage[] {
    const scope = findScope(contextDefinition)
    const parser = new KelParser(expression)
    const tree = parser.parseExpression()
    const traversingVisitor = new AstGeneratingVisitor(scope)
    const node = traversingVisitor.visit(tree)
    const visitor = new ValidatingNodeVisitor()
    visitor.visit(node)
    return visitor.getMessages()
}

function findScope(contextDefinition: string): Scope {
    const scope = Object.values(instance).find(s => s.type.name === contextDefinition)
    if (!scope) throw new Error(`Cannot find scope for a context definition ${contextDefinition}`)
    return scope
}
