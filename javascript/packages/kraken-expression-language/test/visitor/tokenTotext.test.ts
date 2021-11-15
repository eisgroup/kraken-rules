import { createParser } from "kraken-expression-language-visitor";
import { KelTraversingVisitor } from "../../src/visitor/KelTraversingVisitor";
import { token } from "../../src/visitor/NodeUtils";
import { instance } from "../test-data/test-data";

describe("tokenToText", () => {
    it("should resolve text", () => {
        const node = new KelTraversingVisitor(instance.policy).visit(
            createParser(
                "a."
            ).expression()
        );
        const ref = node.children[0];
        expect(token(ref)).toBe("a.");
        expect(token(ref.children[0])).toBe("a");
        expect(token(ref.children[1])).toBe("");
    });
});
