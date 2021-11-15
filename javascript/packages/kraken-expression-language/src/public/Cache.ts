export class Cache<KEY, VALUE> {
    private readonly cache = new Map<KEY, VALUE>();
    maxEntries: number = 50;

    getOrCompute(key: KEY, compute: (key: KEY) => VALUE): VALUE {
        if (this.cache.has(key)) {
            return this.cache.get(key)!;
        }
        const value = compute(key);

        // comply with the cache size
        if (this.cache.size >= this.maxEntries) {
            this.cache.delete(
                this.cache.keys().next().value
            );
        }

        this.cache.set(key, value);
        return value;
    }

    clear(): void {
        this.cache.clear();
    }

}
