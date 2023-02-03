import { ContextInstanceMapIterator } from '../../src/rule-engine/spi/iterators/ContextInstanceMapIterator'

describe('ContextInstanceMapIterator', () => {
    it('should iterate throw map with numbers', () => {
        const iterator = new ContextInstanceMapIterator({ one: 1, two: 2 })
        expect(iterator.next()).toBe(1)
        expect(iterator.hasNext()).toBeTruthy()
        expect(iterator.index()).toBe('one')
        expect(iterator.next()).toBe(2)
        expect(iterator.hasNext()).toBeFalsy()
    })
})
