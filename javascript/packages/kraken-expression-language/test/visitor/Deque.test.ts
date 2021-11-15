import { Deque } from "../../src/visitor/Deque";

describe("Deque", () => {
    it("should peek empty deque", () => {
        const d = new Deque([]);
        expect(d.peek()).toBe(undefined);
    });
    it("should peek with value", () => {
        const d = new Deque([1, 2, 3]);
        expect(d.peek()).toBe(1);
    });
    it("should return last", () => {
        const d = new Deque([1, 2, 3]);
        expect(d.last()).toBe(3);
    });
    it("should pop first element from ", () => {
        const d = new Deque([1, 2, 3]);
        expect(d.pop()).toBe(1);
        expect(d.toArray()).toMatchObject([2, 3]);
    });
    it("should convert to an array", () => {
        const d = new Deque([1, 2, 3]);
        expect(d.toArray()).toMatchObject([1, 2, 3]);
    });
    it("should push to a start of an array", () => {
        const d = new Deque<number>([]);
        d.push(1);
        d.push(2);
        d.push(3);
        expect(d.toArray()).toMatchObject([3, 2, 1]);
    });
});
