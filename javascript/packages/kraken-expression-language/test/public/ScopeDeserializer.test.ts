import { ScopeDeserializer } from '../../src/public/ScopeDeserializer'
import { Type } from '../../src/type/Types'
import { json } from '../test-data/test-data'

describe('ScopeDeserializer', () => {
    it('should deserialize scope', () => {
        const sd = new ScopeDeserializer(json.typeRegistry)
        const scope = sd.provideScope(json.scope.policy)
        expect(scope).toBeDefined()
        expect(scope.name).toContain('Policy_GLOBAL->Policy')
        expect(scope.type.name).toBe('Policy')
        expect(scope.type).toBeInstanceOf(Type)
    })
})
