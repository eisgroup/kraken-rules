import { Objects } from '../../src/factory/Objects'

describe('Objects', () => {
    describe('hasOwnProp', () => {
        it('should check negative cases', () => {
            expect(Objects.hasOwnProp(undefined, 'key')).toBe(false)
            expect(Objects.hasOwnProp(null, 'key')).toBe(false)
            expect(Objects.hasOwnProp('object', 'key')).toBe(false)
            expect(Objects.hasOwnProp({ notKey: 'value' }, 'key')).toBe(false)
        })
    })
})
