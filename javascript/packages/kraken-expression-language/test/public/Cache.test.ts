import { Cache } from '../../src/public/Cache'

describe('Cache', () => {
    it('should check caching', () => {
        const cache = new Cache<number, number>()
        function compute(n: number): number {
            return n * n
        }
        const mock = jest.fn(compute)
        const r = cache.getOrCompute(2, mock)
        expect(r).toBe(4)

        cache.getOrCompute(2, mock)
        cache.getOrCompute(2, mock)
        expect(mock).toHaveBeenCalledTimes(1)

        cache.clear()
        cache.getOrCompute(2, mock)
        expect(mock).toHaveBeenCalledTimes(2)

        cache.maxEntries = 0
        cache.getOrCompute(3, mock)
        cache.getOrCompute(4, mock)
        expect(mock).toHaveBeenCalledTimes(4)
    })
})
