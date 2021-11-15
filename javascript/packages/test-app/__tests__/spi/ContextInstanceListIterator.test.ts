import { ContextInstanceListIterator } from "../../src/rule-engine/spi/iterators/ContextInstanceListIterator";

describe("ContextInstanceListIterator", () => {
    it("should throw error when used index() before next()", () => {
        const iterator = new ContextInstanceListIterator([1, 2, 3]);
        expect(() => iterator.index());
    });
    it("should iterate through list", () => {
        const iterator = new ContextInstanceListIterator([1, 2, 3]);
        expect(iterator.next()).toBe(1);
        expect(iterator.hasNext()).toBeTruthy();
        iterator.next();
        iterator.next();
        expect(iterator.hasNext()).toBeFalsy();
        expect(iterator.index()).toBe(2);
    });
});
