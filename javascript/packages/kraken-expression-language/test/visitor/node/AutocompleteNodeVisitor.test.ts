import { createParser } from "kraken-expression-language-visitor";
import { Scope } from "../../../src/scope/Scope";
import { KelTraversingVisitor } from "../../../src/visitor/KelTraversingVisitor";
import { AutocompleteNodeVisitor } from "../../../src/visitor/node/autocomplete/AutocompleteNodeVisitor";
import { Completion, CompletionItem } from "../../../src/visitor/node/autocomplete/CompletionItem";
import { instance } from "../../test-data/test-data";

describe("AutocompleteNodeVisitor", () => {
    it("should find node on empty expression", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //           █
            expression: "",
            cursor: "1:0",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should find node on valid expression start", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //           █
            expression: "Policy",
            cursor: "1:0",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should find node on valid expression middle", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //              █
            expression: "Policy.policyNumber",
            cursor: "1:3",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should find node on valid path expression middle of property", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                         █
            expression: "Policy.policyNumber",
            cursor: "1:14",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve path and resolve path scope variables with no property ", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                 █
            expression: "Policy.",
            cursor: "1:6",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve path and resolve path scope variables with nested property ", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                         █
            expression: "Policy.referer.",
            cursor: "1:14",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve filter ", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                             █
            expression: "riskItems[model = v]",
            cursor: "1:18",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve explicit filter", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //           012345678901234567890
            //                      █
            expression: "riskItems?[m]",
            cursor: "1:11",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve explicit empty filter", () => {
        const completion = getCompletionItems({
            //                     11111111112
            //                     █
            //           012345678901234567890
            expression: "riskItems?[]",
            cursor: "1:10",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve for iteration", () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                     █
            expression: "for r in riskItems return ",
            cursor: "1:26",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve for iteration with path", () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                      █
            expression: "for r in riskItems return r.",
            cursor: "1:27",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
    it("should resolve for iteration with non existing path", () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                                      █
            expression: "for r in riskItems return o.",
            cursor: "1:27",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });

    it("should resolve array type element or inline array keyword for known in operation", () => {
        const completion = getCompletionItems({
            //                     11111111112222222222
            //           012345678901234567890123456789
            //                          █
            expression: "'a' in policies",
            cursor: "1:7",
            scopeName: "Policy"
        });
        expect(toString(completion)).toMatchSnapshot();
    });
});

function toString(c: Completion | undefined): string[] {
    function comparable(com: CompletionItem): string {
        return `[${com.type}] ${com.text} : ${com.info}`;
    }

    return c!.completions
        .sort((c1, c2) => {
            return comparable(c1).localeCompare(comparable(c2), "en", { sensitivity: "variant" });
        })
        .map(ci => `[${ci.type}] ${ci.text} : ${ci.info}`);
}

function getCompletionItems(p: {
    expression: string,
    scopeName: string,
    cursor: string
}
): Completion | undefined {
    const [line, column] = p.cursor.split(":");
    const visitor = new AutocompleteNodeVisitor({ column: Number(column), line: Number(line) });
    const parser = createParser(p.expression);
    const traversingVisitor = new KelTraversingVisitor(findScope(p.scopeName));
    const tree = traversingVisitor.visit(parser.expression());
    visitor.visit(tree);
    return visitor.getCompletionItems();
}

function findScope(contextDefinition: string): Scope {
    const scope = Object.values(instance).find(s => s.type.name === contextDefinition);
    if (!scope) throw new Error(`Cannot find scope for a context definition ${contextDefinition}`);
    return scope;
}
