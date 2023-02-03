import { KelParser } from 'kraken-expression-language-visitor'
import { Scope } from '../../../src/scope/Scope'
import { AstGeneratingVisitor } from '../../../src/visitor/AstGeneratingVisitor'
import { LocationInfo } from '../../../src/visitor/type-at-location/LocationInfo'
import { InfoAtLocationNodeVisitor } from '../../../src/visitor/type-at-location/NodeAtLocationNodeVisitor'
import { instance } from '../../test-data/test-data'

describe('NodeAtLocationVisitor', () => {
    it('should find nested this', () => {
        const info = getLocationInfoAt({
            //                     1111111111222222222233333333334444444444555555555566666666667777777777
            //           01234567890123456789012345678901234567890123456789012345678901234567890123456789
            //                                                                                        █
            expression:
                'for i in riskItems return this.riskItems[Count(some v in riskItems satisfies this.referer != null) = 2]',
            cursor: '1:77',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Vehicle')
    })
    it('should find filter by bracket', () => {
        const info = getLocationInfoAt({
            //                     1111111111222222222233333333334444444444
            //           01234567890123456789012345678901234567890123456789
            //                                                   █
            expression:
                'for i in riskItems return this.riskItems[Count(some v in riskItems satisfies this.referer != null) = 2]',
            cursor: '1:40',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Vehicle[]')
    })
    it('should find nested for in return expression alias', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                              █
            expression: 'for i in riskItems return for c in i.anubisCoverages return c.code',
            cursor: '1:35',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Vehicle')
    })
    it('should find for alias property and it must be single', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                                  █
            expression: 'for ri in Policy.riskItems return ri.odometerReading',
            cursor: '1:39',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Number')
    })
    it('should find for declaration alias', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //               █
            expression: 'for ri in Policy.riskItems return ri.serviceHistory',
            cursor: '1:4',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Vehicle')
    })
    it('should not find info for return keyword', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                        █
            expression: 'for ri in Policy.riskItems return ri.serviceHistory',
            cursor: '1:29',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Date[]')
    })
    it('should find path for nested property for alias property filter predicate this', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222333333333344444444445555555555
            //           012345678901234567890123456789012345678901234567890123456789
            //                                                                      █
            expression: 'for ri in Policy.riskItems return Count(ri.serviceHistory[this > Today()]) = 1',
            cursor: '1:59',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Date')
    })
    it('should find path for nested property for alias property', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                                  █
            expression: 'for ri in Policy.riskItems return ri.included',
            cursor: '1:39',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Boolean')
    })
    it('should find path for nested property for alias', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                             █
            expression: 'for ri in Policy.riskItems return ri.included',
            cursor: '1:34',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Vehicle')
    })
    it('should find path filter nested property', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                               █
            expression: 'Policy.riskItems?[included]',
            cursor: '1:20',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Boolean')
    })
    it('should find path nested property', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                               █
            expression: 'Policy.riskItems.included',
            cursor: '1:20',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Boolean')
    })
    it('should find path property', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                      █
            expression: "Policy.accountName = 'savings'",
            cursor: '1:11',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('String')
    })
    it('should find object property', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //              █
            expression: "Policy.accountName = 'savings'",
            cursor: '1:3',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Policy')
    })
    it('should find type from this path property', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                      █
            expression: "this.accountName = 'savings'",
            cursor: '1:11',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('String')
    })
    it('should find type from this path object ', () => {
        const info = getLocationInfoAt({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //             █
            expression: "this.accountName = 'savings'",
            cursor: '1:2',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Policy')
    })
    it('should resolve for variable', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                                              █
            expression: 'for vehicle in Vehicle return vehicle = null',
            cursor: '1:35',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Vehicle')
    })
    it('should resolve primitive number', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //            █
            expression: '123',
            cursor: '1:2',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Number')
    })
    it('should not find location info', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                  █
            expression: 'Policy',
            cursor: '1:7',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe(undefined)
    })
    it('should not resolve info in error expression', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                █
            expression: 'in in.',
            cursor: '1:5',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe(undefined)
    })
    it('should resolve inline array type', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //               █
            expression: '{1,2,3}',
            cursor: '1:4',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Number[]')
        expect(info?.range?.start.column).toBe(0)
        expect(info?.range?.end.column).toBe(7)
    })
    it('should resolve inline array element type', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //              █
            expression: '{1,2,3}',
            cursor: '1:3',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Number')
        expect(info?.range?.start.column).toBe(3)
        expect(info?.range?.end.column).toBe(4)
    })
    it('should resolve function', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //             █
            expression: "Date('2020-02-02')",
            cursor: '1:2',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBe('Date(1)')
    })
    it('should not show info over Type', () => {
        const info = getLocationInfoAt({
            //                     111111111122222222223333333333
            //           0123456789012345678901234567890123456789
            //                 █

            expression: '(Policy) this',
            cursor: '1:6',
            scopeName: 'Policy',
        })
        expect(toString(info)).toBeUndefined()
    })
})

function toString(info: LocationInfo | undefined): string | undefined {
    if (info) {
        if (info.type === 'type') {
            return info.evaluationType
        }
        if (info.type === 'function') {
            return `${info.functionName}(${info.parametersCount})`
        }
    }
    return
}

function getLocationInfoAt(opts: { expression: string; cursor: string; scopeName: string }): LocationInfo | undefined {
    const [line, column] = opts.cursor.split(':').map(Number)
    const traversingVisitor = new AstGeneratingVisitor(findScope(opts.scopeName))
    const parser = new KelParser(opts.expression)
    const tree = traversingVisitor.visit(parser.parseExpression())
    const locationVisitor = new InfoAtLocationNodeVisitor({ column, line })
    locationVisitor.visit(tree)
    return locationVisitor.getLocationInfo()
}

function findScope(contextDefinition: string): Scope {
    const scope = Object.values(instance).find(s => s.type.name === contextDefinition)
    if (!scope) throw new Error(`Cannot find scope for a context definition ${contextDefinition}`)
    return scope
}
