import { KelService } from '../../src/public/KelService'
import { instance } from '../test-data/test-data'
import { ReferenceLocationInfo } from '../../src/visitor/type-at-location/LocationInfo'
import { Type } from '../../src/type/Types'

describe('KelService', () => {
    it('should provide hover info on cursor', () => {
        const service = new KelService(instance.policy)
        const info = service.provideInfoAtLocation('Policy.policyNumber', { line: 1, column: 10 })
        expect((info as ReferenceLocationInfo).evaluationType).toBe('String')
    })
    it('should provide hover info on cursor for incomplete this', () => {
        const service = new KelService(instance.policy)
        const info = service.provideInfoAtLocation('this.', { line: 1, column: 1 })
        expect((info as ReferenceLocationInfo).evaluationType).toBe('Policy')
    })
    it('should provide completion for incomplete path end', () => {
        const service = new KelService(instance.policy)
        const completion = service.provideCompletion('Policy.', { line: 1, column: 7 })
        expect(completion.completions).toMatchSnapshot()
        expect(completion.completions.filter(c => c.type === 'function')).toHaveLength(0)
    })
    it('should provide completion for incomplete path within', () => {
        const service = new KelService(instance.policy)
        const completion = service.provideCompletion('Policy.', { line: 1, column: 6 })
        expect(completion.completions).toMatchSnapshot()
        const functions = completion.completions.filter(c => c.type === 'function')
        expect(functions.length).toBeTruthy()
    })
    it('should provide completion for current scope', () => {
        const service = new KelService(instance.policy)
        const completion = service.provideCompletion('P', { line: 1, column: 1 })
        const functions = completion.completions.filter(c => c.type === 'function')
        expect(functions.length).toBeTruthy()
        if (!functions.length) throw new Error()
        if (functions[0].type !== 'function') throw new Error()
        expect(functions[0].relevanceOrder).toBe(2)

        const policy = completion.completions.find(c => c.text.startsWith('Policy'))
        expect(policy).toBeDefined()
        if (!policy) throw new Error()
        if (policy.type !== 'reference') throw new Error()
        expect(policy.relevanceOrder).toBe(2)

        const policyNumber = completion.completions.find(c => c.text.startsWith('policyNumber'))
        expect(policyNumber).toBeDefined()
        if (!policyNumber) throw new Error()
        if (policyNumber.type !== 'reference') throw new Error()
        expect(policyNumber.relevanceOrder).toBe(1)

        expect(completion.completions).toMatchSnapshot()
    })
    it('should provide global completions', () => {
        const service = new KelService(instance.policy)
        const completion = service.provideCompletion('a.b', { line: 1, column: 17 })
        expect(completion.completions).toHaveLength(158)
    })
    it('should provide path completion', () => {
        const service = new KelService(instance.policy)
        const completion = service.provideCompletion('Policy.', { line: 1, column: 17 })
        expect(completion.completions).toHaveLength(33)
    })
    it('should provide validation with semantic errors', () => {
        const service = new KelService(instance.policy)
        const validation = service.provideValidation('Policy.none')
        expect(validation.messages).toHaveLength(1)
    })
    it('should provide validation with syntax errors', () => {
        const service = new KelService(instance.policy)
        const validation = service.provideValidation('Policy.')
        expect(validation.messages).toHaveLength(1)
    })
    it('should provide validation with parse errors', () => {
        const service = new KelService(instance.policy)
        const validation = service.provideValidation('"""')
        expect(validation.messages).toHaveLength(1)
    })
    it('should return if expression is empty', () => {
        function isEmpty(e: string): boolean {
            const service = new KelService(instance.creditCardInfo)
            return service.isEmpty(e)
        }
        expect(isEmpty('')).toBe(true)
        expect(isEmpty(' ')).toBe(true)
        expect(isEmpty('// comment')).toBe(true)
        expect(isEmpty('\n')).toBe(true)
        expect(isEmpty('cardType')).toBe(false)
        expect(isEmpty('10')).toBe(false)
    })
    it('should return an error when attribute is not found in the scope', () => {
        const kel = new KelService(instance.creditCardInfo)
        const validations = kel.provideValidation('CreditCardInfo.cardTyoe')

        expect(validations.messages).toHaveLength(1)
        expect(validations.messages[0].message).toBe(
            "Attribute 'cardTyoe' not found in CreditCardInfo. Did you mean 'cardType'?",
        )
    })
    it('should return an error when attribute is not found in the parent scope', () => {
        const kel = new KelService(instance.creditCardInfo)
        const validations = kel.provideValidation('Policu')

        expect(validations.messages).toHaveLength(1)
        expect(validations.messages[0].message).toBe("Reference 'Policu' not found. Did you mean 'Policy'?")
    })
    it('should return an error when expression has invalid syntax', () => {
        const kel = new KelService(instance.creditCardInfo)
        const validations = kel.provideValidation('in in')

        expect(validations.messages).toHaveLength(1)
        expect(validations.messages[0].message).toContain("Invalid token 'in'")
        expect(validations.messages[0].message).not.toContain('_')
        expect(validations.messages[0].message).not.toContain('OP')
        expect(validations.messages[0].message).not.toContain('EOF')
    })
    it('should return info message for expression with literal true', () => {
        const kel = new KelService(instance.creditCardInfo)
        const validations = kel.provideValidation('true', { allowsTrue: false })

        expect(validations.messages).toHaveLength(1)
        expect(validations.messages[0].message).toBe("Redundant literal value 'true'. Expression is 'true' by default.")
    })
    it('should resolve type from type name string', () => {
        const kel = new KelService(instance.policy)
        const date = kel.resolveType('Date')
        const datetime = kel.resolveType('DateTime')
        const any = kel.resolveType('Any')

        expect(date).toBe(Type.DATE)
        expect(datetime).toBe(Type.DATETIME)
        expect(any).toBe(Type.ANY)
    })
    it('should resolve type from expression', () => {
        const kel = new KelService(instance.policy)
        const date = kel.resolveExpressionEvaluationType('expirationDate')
        const datetime = kel.resolveExpressionEvaluationType('txEffectiveDate')

        expect(date.name).toBe('Date')
        expect(datetime.name).toBe('DateTime')
        expect(Type.DATE.isAssignableFrom(date)).toBeTruthy()
        expect(Type.DATETIME.isAssignableFrom(datetime)).toBeTruthy()
    })
})
