import { Call } from "./test-utils/MethodListener";
import { KelTraversingVisitor, ValidKelNode } from "../../src/visitor/KelTraversingVisitor";
import { createTestVisitor } from "./test-utils/createTestVisitor";
import { NodeType } from "../../src/visitor/NodeType";
import { instance } from "../test-data/test-data";
import { createParser } from "kraken-expression-language-visitor";
import { Type } from "../../src/type/Type";

const testVisitor = createTestVisitor(KelTraversingVisitor, {
    constructorParams: [instance.policy]
});

describe("KelTraversingVisitor", () => {
    describe("context queues", () => {
        function noVisit(x: Call): boolean {
            return x.method !== "visit";
        }
        it("should visit num comparison", () => {
            const events = testVisitor.visit("1 < Coverage.amt");
            expect(events.events.filter(noVisit)).toMatchSnapshot();
        });
        it("should visit num comparison [inverted]", () => {
            const events = testVisitor.visit("Coverage.amt < 1");
            expect(events.events.filter(noVisit)).toMatchSnapshot();
        });
        it("should visit nested reference values", () => {
            const events = testVisitor.visit("Coverage[this.amt < Policy.info.maxAmt]");
            expect(events.events.filter(noVisit)).toMatchSnapshot();
        });
        it("should visit function -> ccr -> filter -> ccr ", () => {
            const events = testVisitor.visit("Count(Coverage[this.amt < Policy.info.maxAmt])");
            expect(events.events.filter(noVisit)).toMatchSnapshot();
        });
    });
    describe("node type", () => {
        function nodeType(expression: string): NodeType {
            const v = new KelTraversingVisitor(instance.policy);
            const parse = (ex: string) => createParser(ex).expression();
            return v.visit(parse(expression)).nodeType!;
        }
        it("should resolve math types", () => {
            expect(nodeType("p+1")).toBe<NodeType>("ADDITION");
            expect(nodeType("1-1")).toBe<NodeType>("SUBTRACTION");
            expect(nodeType("2*a")).toBe<NodeType>("MULTIPLICATION");
            expect(nodeType("2/a")).toBe<NodeType>("DIVISION");
            expect(nodeType("2%a")).toBe<NodeType>("MODULUS");
            expect(nodeType("x**3")).toBe<NodeType>("EXPONENT");
        });
        it("should resolve junction and conjunction", () => {
            expect(nodeType("tom and jerry")).toBe<NodeType>("AND");
            expect(nodeType("tom && jerry")).toBe<NodeType>("AND");
            expect(nodeType("red or blue")).toBe<NodeType>("OR");
            expect(nodeType("red || blue")).toBe<NodeType>("OR");
        });
        it("should resolve comparison", () => {
            expect(nodeType("2 + 2 = 4")).toBe<NodeType>("EQUALS");
            expect(nodeType("'black' != 'white'")).toBe<NodeType>("NOT_EQUALS");
            expect(nodeType("x > 1")).toBe<NodeType>("MORE_THAN");
            expect(nodeType("x >= 1")).toBe<NodeType>("MORE_THAN_OR_EQUALS");
            expect(nodeType("x < 1")).toBe<NodeType>("LESS_THAN");
            expect(nodeType("x <= 1")).toBe<NodeType>("LESS_THAN_OR_EQUALS");
        });
        it("should resolve negation", () => {
            expect(nodeType("not(cool)")).toBe<NodeType>("NEGATION");
            expect(nodeType("not cool")).toBe<NodeType>("NEGATION");
            expect(nodeType("! cool")).toBe<NodeType>("NEGATION");
            expect(nodeType("!cool")).toBe<NodeType>("NEGATION");
            expect(nodeType("! (cool)")).toBe<NodeType>("NEGATION");
        });
        it("should resolve literals", () => {
            expect(nodeType("'string'")).toBe<NodeType>("STRING");
            expect(nodeType("true")).toBe<NodeType>("BOOLEAN");
            expect(nodeType("false")).toBe<NodeType>("BOOLEAN");
            expect(nodeType("FALSE")).toBe<NodeType>("BOOLEAN");
            expect(nodeType("TRUE")).toBe<NodeType>("BOOLEAN");
            expect(nodeType("True")).toBe<NodeType>("BOOLEAN");
            expect(nodeType("FALSE")).toBe<NodeType>("BOOLEAN");
            expect(nodeType("1")).toBe<NodeType>("DECIMAL");
            expect(nodeType("1.1")).toBe<NodeType>("DECIMAL");
            expect(nodeType("2020-02-02")).toBe<NodeType>("DATE");
            expect(nodeType("2020-01-01T00:00:00Z")).toBe<NodeType>("DATETIME");
            expect(nodeType("null")).toBe<NodeType>("NULL");
            expect(nodeType("Null")).toBe<NodeType>("NULL");
            expect(nodeType("NULL")).toBe<NodeType>("NULL");
        });
        it("should resolve type operators", () => {
            expect(nodeType("billingAddress typeof Address")).toBe<NodeType>("TYPEOF");
            expect(nodeType("billingAddress instanceof Address")).toBe<NodeType>("INSTANCEOF");
            expect(nodeType("(Address) address")).toBe<NodeType>("REFERENCE");
        });
        it("should resolve iteration", () => {
            expect(nodeType("for v")).toBe<NodeType>("FOR");
            expect(nodeType("for v in")).toBe<NodeType>("FOR");
            expect(nodeType("for v in")).toBe<NodeType>("FOR");
            expect(nodeType("for v in vehicles")).toBe<NodeType>("FOR");
            expect(nodeType("for v in vehicles satisfies")).toBe<NodeType>("FOR");
            expect(nodeType("some v in vehicles satisfies v.coverage = null")).toBe<NodeType>("SOME");
            expect(nodeType("for v in vehicles return v.coverage")).toBe<NodeType>("FOR");
            expect(nodeType("some v in vehicles satisfies v.coverage != null")).toBe<NodeType>("SOME");
            expect(nodeType("every v in vehicles satisfies v.coverage != null")).toBe<NodeType>("EVERY");
        });
        it("should resolve collection types", () => {
            expect(nodeType("a.b")).toBe<NodeType>("REFERENCE");
            expect(nodeType("vehicles[10]")).toBe<NodeType>("REFERENCE");
            expect(nodeType("coverages[coverages[1] > 99]")).toBe<NodeType>("REFERENCE");
        });
        it("should resolve inline data structures", () => {
            expect(nodeType("{ 1,2,3 }")).toBe<NodeType>("INLINE_ARRAY");
            expect(nodeType("{ \"a\":1, \"b\":2 }")).toBe<NodeType>("INLINE_MAP");
        });
        it("should resolve node type correctly", () => {
            expect(nodeType("x in {x, y}")).toBe<NodeType>("IN");
            expect(nodeType("code matches '\w{2}'")).toBe<NodeType>("MATCHES_REG_EXP");
            expect(nodeType("-5")).toBe<NodeType>("NEGATIVE");
            expect(nodeType("Policy")).toBe<NodeType>("REFERENCE");
            expect(nodeType("this")).toBe<NodeType>("THIS");
            expect(nodeType("FromMoney(amt)")).toBe<NodeType>("REFERENCE");
            expect(nodeType("if true then false")).toBe<NodeType>("IF");
            expect(nodeType("if true then false else true")).toBe<NodeType>("IF");

        });
    });
    describe("visiting", () => {
        it("should visit equality comparison", () => {
            expectCalled(
                "1 = 'one'",
                { method: "visitEqualityComparison", text: "1='one'" },
                { method: "visitDecimal", text: "1" },
                { method: "visitString", text: "'one'" }
            );
        });
        it("should visit numerical comparison", () => {
            expectCalled(
                "1 < a.b",
                { method: "visitNumericalComparison", text: "1<a.b" },
                { method: "visitDecimal", text: "1" },
                { method: "visitPath", text: "a.b" }
            );
        });
        it.skip("should visit function", () => {
            expectCalled(
                "Count(1)",
                { method: "visitFunctionCall", text: "Count(1)" },
                { method: "visitDecimal", text: "1" }
            );
        });
    });
    describe("scope and type", () => {
        const createVisitor = () => new KelTraversingVisitor(instance.policy);
        const parse = (ex: string) => createParser(ex).expression();
        const visit = (expression: string) => createVisitor().visit(parse(expression));
        const expectType = (expression: string, typeName: string) =>
            expect(visit(expression).evaluationType.name).toBe(typeName);
        it("should resolve path", () => {
            const node = visit("Policy.versionDescription");
            expect((node as ValidKelNode).context?.text).toBe("Policy.versionDescription");
            expect(node.evaluationType.name).toBe(Type.STRING.name);
            expect(node.nodeType).toBe("REFERENCE");
            expect(node.scope).toBe(instance.policy);
        });
        it("should resolve primitives filter", () => {
            const node = visit("Policy.policies[this != 'abc']");
            expect(node.evaluationType.name).toBe(Type.STRING.name + "[]");
            expect(node.nodeType).toBe("REFERENCE");
            expect(node.scope).toBe(instance.policy);
        });
        it("should resolve type by path", () => {
            const node = visit("Policy.referer.superReferer.superReferer.superReferer.superReferer");
            expect(node.evaluationType.name).toBe("SuperReferer");
            expect(node.nodeType).toBe("REFERENCE");
            expect(node.scope).toBe(instance.policy);
        });
        it("should resolve types correctly from paths and access by index", () => {
            expectType("Policy.riskItems", "Vehicle[]");
            expectType("Policy.riskItems[0]", "Vehicle");
            expectType("Policy.riskItems[0].rentalCoverage", "RRCoverage");
            expectType("Policy.riskItems.rentalCoverage", "RRCoverage[]");
            expectType("Policy.riskItems[0].rentalCoverage.combinedLimit", "String");
            expectType("Policy.riskItems.rentalCoverage.combinedLimit", "String[]");
            expectType("Policy.riskItems[0].rentalCoverage.limitAmount", "Number");
            expectType("Policy.riskItems[0].rentalCoverage.none", "Unknown");
            expectType("Policy.riskItems[2**2]", "Vehicle");
            expectType("Policy.riskItems[3+3]", "Vehicle");
            expectType("Policy.riskItems[3*3]", "Vehicle");
            expectType("Policy.riskItems[1-2]", "Vehicle");
            expectType("Policy.riskItems[1]", "Vehicle");
            expectType("Policy.riskItems[-1]", "Vehicle");
            expectType("Policy.riskItems[this.createdFromPolicyRev]", "Vehicle");
        });
        it("should resolve common type in inline array", () => {
            expectType("Policy.riskItems[Count(Policy.riskItems)]", "Vehicle");
            expectType("Policy.riskItems[Policy.createdFromPolicyRev]", "Vehicle");
            expectType("Policy.riskItems[0].anubisCoverages[0]", "AnubisCoverage");
            expectType("Policy.riskItems[0].collCoverages[0]", "COLLCoverage");
            expectType("{}", "Any[]");
            expectType("{ a, b }", "Unknown[]");
            expectType("{ Policy, true }", "Any[]");
            expectType(
                "{ Policy.riskItems[0].anubisCoverages[0], Policy.riskItems[0].collCoverages[0] }",
                "CarCoverage[]"
            );
            expectType("{ (CarCoverage) Policy, (AnubisCoverage) Policy }", "CarCoverage[]");
            expectType("{ (CarCoverage[]) Policy, (AnubisCoverage[]) Policy, (Any[]) Policy }", "CarCoverage[][]");
            expectType(
                "{ (CarCoverage) Policy, (AnubisCoverage) Policy, (AnubisSecretCoverage) Policy }",
                "CarCoverage[]"
            );
            expectType(
                "{ (AnubisCoverage) Policy, (AnubisSecretCoverage) Policy }",
                "AnubisCoverage[]"
            );
        });
        it("should resolve generic function return", () => {
            expectType("Union(Policy.riskItems.rentalCoverage, Policy.riskItems[0].anubisCoverages)", "RRCoverage[]");
            expectType("Union(Policy.policies, {'a'})", "String[]");
        });
        it("should resolve defined function return", () => {
            expectType("FromMoney(CreditCardInfo.cardCreditLimitAmount)", "Number");
        });
        it("should return unknown type for non existing function", () => {
            expectType("Union(1,2,3,4)", "Unknown");
        });
        it("should return Policy for this", () => {
            expectType("this", "Policy");
        });
        it("should return string[] in this scope", () => {
            expectType("this.policies", "String[]");
        });
        it("should resolve predicate in collection", () => {
            expectType("Policy.riskItems?[included]", "Vehicle[]");
            expectType("Policy.riskItems?[(included and included) && (included and included)]", "Vehicle[]");
            expectType("Policy.riskItems?[included and included]", "Vehicle[]");
            expectType("Policy.riskItems[(included or included) = true]", "Vehicle[]");
            expectType("Policy.riskItems[true in {included, included}]", "Vehicle[]");
            expectType("Policy.riskItems?[All(Policy.riskItems.included)]", "Vehicle[]");
            expectType("Policy.riskItems[Count(serviceHistory) = 1]", "Vehicle[]");
            expectType("Policy.riskItems[every s in serviceHistory satisfies s != null]", "Vehicle[]");
            expectType("Policy.riskItems[some s in serviceHistory satisfies s != null]", "Vehicle[]");
            expectType("Policy.riskItems[Count(for sh in serviceHistory return sh) = 1]", "Vehicle[]");
            expectType("Policy.riskItems[model matches '\w+']", "Vehicle[]");
            expectType("Policy.riskItems[!(model matches '\w+')]", "Vehicle[]");
            expectType("Policy.riskItems[!(this.model matches '\w+')]", "Vehicle[]");
        });
        it("should visit this", () => {
            expectType("this.riskItems", "Vehicle[]");
            expectType("this", "Policy");
        });
        it("should resolve in local scope", () => {
            expectType("riskItems", "Vehicle[]");
        });
        it("should resolve for each iteration type", () => {
            expectType("for r in Policy.riskItems return r.model", "String[]");
            expectType("for r in Policy.riskItems return r.serviceHistory", "Date[]");
            expectType("for r in Policy.riskItems return r.none", "Unknown[]");
            expectType("for r in Policy.riskItems return true", "Boolean[]");
            expectType("for r in context.riskItems return r.model", "Any[]");
        });
        it("should resolve control flow", () => {
            expectType("if true then 1 else 'a'", "Number");
        });
        it("should resolve for every iteration type", () => {
            expectType("every r in Policy.riskItems satisfies r.model = 'honda'", "Boolean");
        });
        it("should resolve for some iteration type", () => {
            expectType("some r in Policy.riskItems satisfies r.model = 'honda'", "Boolean");
        });
        it("should visit primitives", () => {
            expectType("1", "Number");
            expectType("-1", "Number");
            expectType("-1.1", "Number");
            expectType("1.1", "Number");
            expectType("true", "Boolean");
            expectType("false", "Boolean");
            expectType("!(true)", "Boolean");
            expectType("'a'", "String");
            expectType("2020-02-02", "Date");
            expectType("2020-01-01T00:00:00Z", "DateTime");
            expectType("null", "Any");
            expectType("Null", "Any");
            expectType("NULL", "Any");
        });
    });
});

function expectCalled(expression: string, ...calls: Call[]): void {
    const v = testVisitor.visit(expression);
    const copy = [...calls];
    for (const event of v.events) {
        if (copy.length && event.method === copy[0].method) {
            expect(`${event.method}(${event.text})`).toBe(`${copy[0].method}(${copy[0].text})`);
            copy.shift();
        }
    }
    if (copy.length > 0) {
        throw new Error(`
        Expected that all events will match in order, but these events didn't match:
            ${JSON.stringify(copy, null, 2)}
            all events: ${JSON.stringify(v, null, 2)}
        `);
    }
}
