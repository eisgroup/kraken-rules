
export class Deque<T> {

    #values: T[];

    constructor(init: T[]) {
        this.#values = init;
    }

    /**
     * if elements are [3,2,1] returns 1
     * @returns last element
     */
    last(): T {
        const v = this.#values;
        return v[v.length - 1];
    }

    /**
     * if elements are [3,2,1] returns 3
     * @returns first element
     */
    peek(): T {
        const v = this.#values;
        return v[0];
    }

    /**
     * if elements are [3,2,1] returns 3 and removes this element
     * after pop elements are [2,1]
     * @returns first element and removes it
     */
    pop(): T | undefined {
        return this.#values.shift();
    }

    /**
     * if elements are [2,1] #push(3)
     * then elements will be [3,2,1]
     * @param v element to insert in the start
     */
    push(v: T): void {
        this.#values = [v, ...this.#values];
    }

    /**
     * @returns an Array of elements in the Deque
     */
    toArray(): T[] {
        return [...this.#values];
    }
}
