import { AstGeneratingVisitor } from '../../src/visitor/AstGeneratingVisitor'
import { NodeType, ValidKelNode, Node } from '../../src/visitor/Node'
import { instance } from '../test-data/test-data'
import { KelParser } from 'kraken-expression-language-visitor'
import { Type } from '../../src/type/Types'

describe('AstGeneratingVisitor', () => {
    describe('node type', () => {
        function parseNode(expression: string): Node {
            const v = new AstGeneratingVisitor(instance.policy)
            const parse = (ex: string) => new KelParser(ex).parseExpression()
            return v.visit(parse(expression))
        }
        function nodeType(expression: string): NodeType {
            return parseNode(expression).nodeType
        }
        it('should resolve empty type', () => {
            expect(nodeType('')).toBe<NodeType>('EMPTY')
            expect(nodeType(' ')).toBe<NodeType>('EMPTY')
            expect(nodeType('// comment')).toBe<NodeType>('EMPTY')
            expect(nodeType('\n')).toBe<NodeType>('EMPTY')
        })
        it('should resolve math types', () => {
            expect(nodeType('p+1')).toBe<NodeType>('ADDITION')
            expect(nodeType('1-1')).toBe<NodeType>('SUBTRACTION')
            expect(nodeType('2*a')).toBe<NodeType>('MULTIPLICATION')
            expect(nodeType('2/a')).toBe<NodeType>('DIVISION')
            expect(nodeType('2%a')).toBe<NodeType>('MODULUS')
            expect(nodeType('x**3')).toBe<NodeType>('EXPONENT')
        })
        it('should resolve junction and conjunction', () => {
            expect(nodeType('tom and jerry')).toBe<NodeType>('AND')
            expect(nodeType('tom && jerry')).toBe<NodeType>('AND')
            expect(nodeType('red or blue')).toBe<NodeType>('OR')
            expect(nodeType('red || blue')).toBe<NodeType>('OR')
        })
        it('should resolve comparison', () => {
            expect(nodeType('2 + 2 = 4')).toBe<NodeType>('EQUALS')
            expect(nodeType("'black' != 'white'")).toBe<NodeType>('NOT_EQUALS')
            expect(nodeType('x > 1')).toBe<NodeType>('MORE_THAN')
            expect(nodeType('x >= 1')).toBe<NodeType>('MORE_THAN_OR_EQUALS')
            expect(nodeType('x < 1')).toBe<NodeType>('LESS_THAN')
            expect(nodeType('x <= 1')).toBe<NodeType>('LESS_THAN_OR_EQUALS')
        })
        it('should resolve negation', () => {
            expect(nodeType('not(cool)')).toBe<NodeType>('NEGATION')
            expect(nodeType('not cool')).toBe<NodeType>('NEGATION')
            expect(nodeType('! cool')).toBe<NodeType>('NEGATION')
            expect(nodeType('!cool')).toBe<NodeType>('NEGATION')
            expect(nodeType('! (cool)')).toBe<NodeType>('NEGATION')
        })
        it('should resolve literals', () => {
            expect(nodeType("'string'")).toBe<NodeType>('STRING')
            expect(nodeType('true')).toBe<NodeType>('BOOLEAN')
            expect(nodeType('false')).toBe<NodeType>('BOOLEAN')
            expect(nodeType('FALSE')).toBe<NodeType>('BOOLEAN')
            expect(nodeType('TRUE')).toBe<NodeType>('BOOLEAN')
            expect(nodeType('True')).toBe<NodeType>('BOOLEAN')
            expect(nodeType('FALSE')).toBe<NodeType>('BOOLEAN')
            expect(nodeType('1')).toBe<NodeType>('DECIMAL')
            expect(nodeType('1.1')).toBe<NodeType>('DECIMAL')
            expect(nodeType('2020-02-02')).toBe<NodeType>('DATE')
            expect(nodeType('2020-01-01T00:00:00Z')).toBe<NodeType>('DATETIME')
            expect(nodeType('null')).toBe<NodeType>('NULL')
            expect(nodeType('Null')).toBe<NodeType>('NULL')
            expect(nodeType('NULL')).toBe<NodeType>('NULL')
        })
        it('should resolve type operators', () => {
            expect(nodeType('billingAddress typeof Address')).toBe<NodeType>('TYPEOF')
            expect(nodeType('billingAddress instanceof Address')).toBe<NodeType>('INSTANCEOF')
            expect(nodeType('(Address) address')).toBe<NodeType>('REFERENCE')
        })
        it('should resolve iteration', () => {
            expect(nodeType('for v')).toBe<NodeType>('FOR')
            expect(nodeType('for v in')).toBe<NodeType>('FOR')
            expect(nodeType('for v in vehicles')).toBe<NodeType>('FOR')
            expect(nodeType('for v in vehicles satisfies')).toBe<NodeType>('FOR')
            expect(nodeType('some v in vehicles satisfies v.coverage = null')).toBe<NodeType>('SOME')
            expect(nodeType('for v in vehicles return v.coverage')).toBe<NodeType>('FOR')
            expect(nodeType('some v in vehicles satisfies v.coverage != null')).toBe<NodeType>('SOME')
            expect(nodeType('every v in vehicles satisfies v.coverage != null')).toBe<NodeType>('EVERY')
        })
        it('should resolve incomplete iteration children', () => {
            const nodeFor = parseNode('for')
            expect(nodeFor.children).toHaveLength(1)
            expect(nodeFor.children[0].nodeType).toBe('EMPTY')

            const nodeForV = parseNode('for v')
            expect(nodeForV.children).toHaveLength(1)
            expect(nodeForV.children[0].nodeType).toBe('VARIABLE_NAME')

            const nodeForVIn = parseNode('for v in')
            expect(nodeForVIn.children).toHaveLength(2)
            expect(nodeForVIn.children[0].nodeType).toBe('VARIABLE_NAME')
            expect(nodeForVIn.children[1].nodeType).toBe('EMPTY')

            const nodeForVInVehicles = parseNode('for v in vehicles')
            expect(nodeForVInVehicles.children).toHaveLength(2)
            expect(nodeForVInVehicles.children[0].nodeType).toBe('VARIABLE_NAME')
            expect(nodeForVInVehicles.children[1].nodeType).toBe('REFERENCE')

            const nodeForVInVehiclesReturn = parseNode('for v in vehicles return')
            expect(nodeForVInVehiclesReturn.children).toHaveLength(3)
            expect(nodeForVInVehiclesReturn.children[0].nodeType).toBe('VARIABLE_NAME')
            expect(nodeForVInVehiclesReturn.children[1].nodeType).toBe('REFERENCE')
            expect(nodeForVInVehiclesReturn.children[2].nodeType).toBe('EMPTY')
        })
        it('should resolve incomplete path children', () => {
            const path = parseNode('riskItems.').children[0]
            expect(path.children).toHaveLength(2)
            expect(path.children[0].nodeType).toBe('IDENTIFIER')
            expect(path.children[1].nodeType).toBe('EMPTY')

            const thisPath = parseNode('this.').children[0]
            expect(thisPath.children).toHaveLength(2)
            expect(thisPath.children[0].nodeType).toBe('THIS')
            expect(thisPath.children[1].nodeType).toBe('EMPTY')
        })
        it('should resolve incomplete access by index children', () => {
            const accessByIndexWithEmptyBracket = parseNode('vehicles[').children[0]
            expect(accessByIndexWithEmptyBracket.children).toHaveLength(2)
            expect(accessByIndexWithEmptyBracket.children[0].nodeType).toBe('IDENTIFIER')
            expect(accessByIndexWithEmptyBracket.children[1].nodeType).toBe('EMPTY')

            const accessByIndexWithBrackets = parseNode('vehicles[]').children[0]
            expect(accessByIndexWithBrackets.children).toHaveLength(2)
            expect(accessByIndexWithBrackets.children[0].nodeType).toBe('IDENTIFIER')
            expect(accessByIndexWithBrackets.children[1].nodeType).toBe('EMPTY')
        })
        it('should resolve incomplete filter children', () => {
            const filterWithEmptyBracket = parseNode('vehicles?[').children[0]
            expect(filterWithEmptyBracket.children).toHaveLength(2)
            expect(filterWithEmptyBracket.children[0].nodeType).toBe('IDENTIFIER')
            expect(filterWithEmptyBracket.children[1].nodeType).toBe('EMPTY')

            const filterWithBrackets = parseNode('vehicles?[]').children[0]
            expect(filterWithBrackets.children).toHaveLength(2)
            expect(filterWithBrackets.children[0].nodeType).toBe('IDENTIFIER')
            expect(filterWithBrackets.children[1].nodeType).toBe('EMPTY')
        })
        it('should resolve collection types', () => {
            expect(nodeType('a.b')).toBe<NodeType>('REFERENCE')
            expect(nodeType('vehicles[10]')).toBe<NodeType>('REFERENCE')
            expect(nodeType('coverages[coverages[1] > 99]')).toBe<NodeType>('REFERENCE')
        })
        it('should resolve inline data structures', () => {
            expect(nodeType('{ 1,2,3 }')).toBe<NodeType>('INLINE_ARRAY')
            expect(nodeType('{ "a":1, "b":2 }')).toBe<NodeType>('INLINE_MAP')
        })
        it('should resolve node type correctly', () => {
            expect(nodeType('x in {x, y}')).toBe<NodeType>('IN')
            expect(nodeType("code matches 'w{2}'")).toBe<NodeType>('MATCHES_REG_EXP')
            expect(nodeType('-5')).toBe<NodeType>('NEGATIVE')
            expect(nodeType('Policy')).toBe<NodeType>('REFERENCE')
            expect(nodeType('this')).toBe<NodeType>('REFERENCE')
            expect(nodeType('this.')).toBe<NodeType>('REFERENCE')
            expect(nodeType('FromMoney(amt)')).toBe<NodeType>('REFERENCE')
            expect(nodeType('if true then false')).toBe<NodeType>('IF')
            expect(nodeType('if true then false else true')).toBe<NodeType>('IF')
        })
        it('should build value block', () => {
            const valueBlock = parseNode('set a to 1 set b to 2 return a+b')

            expect(valueBlock.nodeType).toBe<NodeType>('VALUE_BLOCK')
            expect(valueBlock.children[0].nodeType).toBe<NodeType>('VARIABLE')
            expect(valueBlock.children[1].nodeType).toBe<NodeType>('VARIABLE')
            expect(valueBlock.children[2].nodeType).toBe<NodeType>('ADDITION')
        })
    })
    describe('scope and type', () => {
        const createVisitor = () => new AstGeneratingVisitor(instance.policy)
        const parse = (ex: string) => new KelParser(ex).parseExpression()
        const visit = (expression: string) => createVisitor().visit(parse(expression))
        const expectType = (expression: string, typeName: string) =>
            expect(visit(expression).evaluationType.name).toBe(typeName)
        it('should resolve path', () => {
            const node = visit('Policy.versionDescription')
            expect((node as ValidKelNode).context?.text).toBe('Policy.versionDescription')
            expect(node.evaluationType.name).toBe(Type.STRING.name)
            expect(node.nodeType).toBe('REFERENCE')
            expect(node.scope).toBe(instance.policy)
        })
        it('should resolve primitives filter', () => {
            const node = visit("Policy.policies[this != 'abc']")
            expect(node.evaluationType.name).toBe(Type.STRING.name + '[]')
            expect(node.nodeType).toBe('REFERENCE')
            expect(node.scope).toBe(instance.policy)
        })
        it('should resolve type by path', () => {
            const node = visit('Policy.referer.superReferer.superReferer.superReferer.superReferer')
            expect(node.evaluationType.name).toBe('SuperReferer')
            expect(node.nodeType).toBe('REFERENCE')
            expect(node.scope).toBe(instance.policy)
        })
        it('should resolve types correctly from paths and access by index', () => {
            expectType('Policy.riskItems', 'Vehicle[]')
            expectType('Policy.riskItems[0]', 'Vehicle')
            expectType('Policy.riskItems[0].rentalCoverage', 'RRCoverage')
            expectType('Policy.riskItems.rentalCoverage', 'RRCoverage[]')
            expectType('Policy.riskItems[0].rentalCoverage.combinedLimit', 'String')
            expectType('Policy.riskItems.rentalCoverage.combinedLimit', 'String[]')
            expectType('Policy.riskItems[0].rentalCoverage.limitAmount', 'Number')
            expectType('Policy.riskItems[0].rentalCoverage.none', 'Unknown')
            expectType('Policy.riskItems[2**2]', 'Vehicle')
            expectType('Policy.riskItems[3+3]', 'Vehicle')
            expectType('Policy.riskItems[3*3]', 'Vehicle')
            expectType('Policy.riskItems[1-2]', 'Vehicle')
            expectType('Policy.riskItems[1]', 'Vehicle')
            expectType('Policy.riskItems[-1]', 'Vehicle')
            expectType('Policy.riskItems[this.createdFromPolicyRev]', 'Vehicle')
        })
        it('should resolve common type in inline array', () => {
            expectType('Policy.riskItems[Count(Policy.riskItems)]', 'Vehicle')
            expectType('Policy.riskItems[Policy.createdFromPolicyRev]', 'Vehicle')
            expectType('Policy.riskItems[0].anubisCoverages[0]', 'AnubisCoverage')
            expectType('Policy.riskItems[0].collCoverages[0]', 'COLLCoverage')
            expectType('{}', 'Any[]')
            expectType('{ a, b }', 'Unknown[]')
            expectType('{ Policy, true }', 'Any[]')
            expectType(
                '{ Policy.riskItems[0].anubisCoverages[0], Policy.riskItems[0].collCoverages[0] }',
                'CarCoverage[]',
            )
            expectType('{ (CarCoverage) Policy, (AnubisCoverage) Policy }', 'CarCoverage[]')
            expectType('{ (CarCoverage[]) Policy, (AnubisCoverage[]) Policy, (Any[]) Policy }', 'Any[][]')
            expectType(
                '{ (CarCoverage) Policy, (AnubisCoverage) Policy, (AnubisSecretCoverage) Policy }',
                'CarCoverage[]',
            )
            expectType('{ (AnubisCoverage) Policy, (AnubisSecretCoverage) Policy }', 'AnubisCoverage[]')
        })
        it('should resolve generic function return', () => {
            expectType('Union(Policy.riskItems.rentalCoverage, Policy.riskItems[0].anubisCoverages)', 'CarCoverage[]')
            expectType("Union(Policy.policies, {'a'})", 'String[]')
        })
        it('should resolve defined function return', () => {
            expectType('FromMoney(CreditCardInfo.cardCreditLimitAmount)', 'Number')
        })
        it('should return unknown type for non existing function', () => {
            expectType('Union(1,2,3,4)', 'Unknown')
        })
        it('should return Policy for this', () => {
            expectType('this', 'Policy')
        })
        it('should return string[] in this scope', () => {
            expectType('this.policies', 'String[]')
        })
        it('should resolve predicate in collection', () => {
            expectType('Policy.riskItems?[included]', 'Vehicle[]')
            expectType('Policy.riskItems?[(included and included) && (included and included)]', 'Vehicle[]')
            expectType('Policy.riskItems?[included and included]', 'Vehicle[]')
            expectType('Policy.riskItems[(included or included) = true]', 'Vehicle[]')
            expectType('Policy.riskItems[true in {included, included}]', 'Vehicle[]')
            expectType('Policy.riskItems?[All(Policy.riskItems.included)]', 'Vehicle[]')
            expectType('Policy.riskItems[Count(serviceHistory) = 1]', 'Vehicle[]')
            expectType('Policy.riskItems[every s in serviceHistory satisfies s != null]', 'Vehicle[]')
            expectType('Policy.riskItems[some s in serviceHistory satisfies s != null]', 'Vehicle[]')
            expectType('Policy.riskItems[Count(for sh in serviceHistory return sh) = 1]', 'Vehicle[]')
            expectType("Policy.riskItems[model matches 'w+']", 'Vehicle[]')
            expectType("Policy.riskItems[!(model matches 'w+')]", 'Vehicle[]')
            expectType("Policy.riskItems[!(this.model matches 'w+')]", 'Vehicle[]')
        })
        it('should visit this', () => {
            expectType('this.riskItems', 'Vehicle[]')
            expectType('this', 'Policy')
            expectType('this.', 'Any')
            expectType('Policy.riskItems[this. = null]', 'Vehicle[]')
        })
        it('should resolve in local scope', () => {
            expectType('riskItems', 'Vehicle[]')
        })
        it('should resolve for each iteration type', () => {
            expectType('for r in Policy.riskItems return r.model', 'String[]')
            expectType('for r in Policy.riskItems return r.serviceHistory', 'Date[]')
            expectType('for r in Policy.riskItems return r.none', 'Unknown')
            expectType('for r in Policy.riskItems return true', 'Boolean[]')
            expectType('for r in context.riskItems return r.model', 'Any')
        })
        it('should resolve control flow', () => {
            expectType('if true then 1 else 2', 'Number')
            expectType("if true then 1 else 'a'", 'Unknown')
        })
        it('should resolve for every iteration type', () => {
            expectType("every r in Policy.riskItems satisfies r.model = 'honda'", 'Boolean')
        })
        it('should resolve for some iteration type', () => {
            expectType("some r in Policy.riskItems satisfies r.model = 'honda'", 'Boolean')
        })
        it('should visit primitives', () => {
            expectType('1', 'Number')
            expectType('-1', 'Number')
            expectType('-1.1', 'Number')
            expectType('1.1', 'Number')
            expectType('true', 'Boolean')
            expectType('false', 'Boolean')
            expectType('!(true)', 'Boolean')
            expectType("'a'", 'String')
            expectType('2020-02-02', 'Date')
            expectType('2020-01-01T00:00:00Z', 'DateTime')
            expectType('null', 'Any')
            expectType('Null', 'Any')
            expectType('NULL', 'Any')
        })
        it('should flat map path', () => {
            expectType('((COLLCoverage | COLLCoverage[]) coverage).limitAmount', 'Number | Number[]')
            expectType('((Any | COLLCoverage) coverage).limitAmount', 'Any')
            expectType('((Any | COLLCoverage | COLLCoverage[]) coverage).limitAmount', 'Any')
            expectType('((COLLCoverage[]) coverage).limitAmount', 'Number[]')
            expectType('((Something | Something2) coverage).limitAmount', 'Unknown')
            expectType('((Vehicle[]) vehicle).serviceHistory', 'Date[]')
        })
        it('should type guard within if', () => {
            expectType('if(RiskItem instanceof COLLCoverage) then RiskItem.limitAmount', 'Number')
        })
        it('should type guard within filter', () => {
            expectType('parties[this instanceof Vehicle].model', 'String[]')
            expectType('parties[this instanceof AddressLine and this instanceof Vehicle].model', 'String[]')
            expectType('parties[this instanceof Vehicle][0].model', 'String')
            expectType('parties[this instanceof AddressLine or this instanceof Vehicle].model', 'Unknown[]')
        })
        it('should type guard within nested conjunction', () => {
            const node = visit('Vehicle[0] instanceof COLLCoverage and Vehicle[0].limitAmount > 10')
            // type of 'Vehicle[0].limitAmount' node
            expect(node.children[1].children[0].evaluationType.name).toBe(Type.NUMBER.name)
        })
        it('should handle scope reset in nested type guard and filter', () => {
            const node = visit(
                '' +
                    'if(this instanceof CreditCardInfo) ' +
                    'then this.cardCreditLimitAmount + parties[this instanceof Vehicle][0].odometerReading',
            )

            // type of 'this.limitAmount' node
            expect(node.children[1].children[0].evaluationType.name).toBe(Type.MONEY.name)
            // type of 'parties[this instanceof Vehicle][0].odometerReading' node
            expect(node.children[1].children[1].evaluationType.name).toBe(Type.NUMBER.name)
        })
        it('should build scope for variables', () => {
            expectType('set a to 1 set b to 2 return a+b', 'Number')
            expectType('set parties to parties[this instanceof Vehicle] return parties.model', 'String[]')
        })
    })
})
