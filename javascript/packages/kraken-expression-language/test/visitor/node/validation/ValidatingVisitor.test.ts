import { createParser } from "kraken-expression-language-visitor";
import { Scope } from "../../../../src/scope/Scope";
import { KelTraversingVisitor } from "../../../../src/visitor/KelTraversingVisitor";
import { ValidatingNodeVisitor } from "../../../../src/visitor/node/validation/ValidatingVisitor";
import { instance } from "../../../test-data/test-data";

describe("ValidatingVisitor", () => {
    it("visit_not_equals", () => {
        tcValid("1 = 0", "Policy");
        tcValid("1 != createdFromPolicyRev", "Policy");
        tcNotValid("'a' != createdFromPolicyRev", "Policy");
    });
    it("visit_matches_reg_exp", () => {
        tcValid("'1' matches 'regexp'", "Policy");
        tcNotValid("1 matches 'regexp'", "Policy");
    });
    it("visit_function", () => {
        tcValid("Today()", "Policy");
        tcValid("Now()", "Policy");
        tcNotValid("Date(1)", "Policy");
        tcNotValid("Date('1','1','1','1','1','1','1','1','1','1','1','1','1','1','1','1')", "Policy");
        tcNotValid("Now(1)", "Policy");
    });
    it("visit_if condition not boolean", () => {
        tcValid("if(true) then 1", "Policy");
        tcValid("if(currentQuoteInd) then 1", "Policy");
        tcNotValid("if(null) then 1", "Policy");
        tcNotValid("if('text') then 1", "Policy");
    });
    it("visit_collection_filter", () => {
        tcValid("policies[this = 'p01']", "Policy");
        tcNotValid("policies?['p01']", "Policy");
    });
    it("prop not identifier or access by index", () => {
        tcNotValid("policies.'p01'", "Policy");
    });
    it("visit access by index prop not identifier or access by index", () => {
        tcNotValid("policies['p01']", "Policy");
    });
    it("general cases", () => {
        tcWithSyntaxErrorCount("a.b.c.d", "AnubisCoverage", 1);
        tcValid("this in Policy.riskItems[0].collCoverages", "COLLCoverage");
        tcValid("Policy.riskItems[0].collCoverages[0] in Vehicle.collCoverages", "COLLCoverage");
        tcValid("Vehicle.collCoverages[0] in Vehicle.collCoverages", "COLLCoverage");
        tcValid("this in Vehicle.collCoverages", "COLLCoverage");
        tcValid("BillingAddress = addressInfo", "Insured");
        tcValid("addressInfo != BillingAddress", "Insured");
        tcValid("addressInfo = BillingAddress", "Insured");
        tcValid("addressInfo = AddressInfo", "Insured");
        tcValid("AddressInfo = addressInfo", "Insured");
        tcValid("if(true) then AddressInfo else addressInfo", "Insured");
        tcValid("if(true) then addressInfo else AddressInfo", "Insured");
        tcValid("if(true) then BillingAddress else addressInfo", "Insured");
        tcValid("if(true) then addressInfo else BillingAddress", "Insured");
        tcValid("{AddressInfo} = {addressInfo}", "Insured");
        tcValid("CreditCardInfo.cardCreditLimitAmount = 1", "Insured");
        tcValid("1 = CreditCardInfo.cardCreditLimitAmount", "Insured");
        tcValid("-CreditCardInfo.cardCreditLimitAmount = 1", "Insured");
        tcValid("-CreditCardInfo.cardCreditLimitAmount", "Insured");
    });
    it("iteration", () => {
        tcValid("every c in Vehicle.collCoverages satisfies c.code == 'a'", "COLLCoverage");
        tcValid("some c in Vehicle.collCoverages satisfies c.code == 'a'", "COLLCoverage");
        tcNotValid("every c in Vehicle.collCoverages satisfies c.code", "COLLCoverage");
        tcNotValid("some c in Vehicle.collCoverages satisfies c.code", "COLLCoverage");
        tcValid("for c in Vehicle.collCoverages return c.code", "COLLCoverage");
        tcNotValid("for code in Vehicle.collCoverages[*].code return code", "COLLCoverage");
        tcNotValid("some code in Vehicle.collCoverages[*].code satisfies code", "COLLCoverage");
        tcNotValid("every code in Vehicle.collCoverages[*].code satisfies code", "COLLCoverage");
        tcNotValid("for r in Policy.riskItems return (for r in r.collCoverages return r.limitAmount)", "COLLCoverage");
        tcValid("for r in Policy.riskItems return (for c in r.collCoverages return c.limitAmount)", "COLLCoverage");

    });
    it("should find cyclomatic complexity", () => {
        const invalid = `Policy.riskItems[1 > Sum(for i in {1} return
              i + Sum(for j in {10} return
                i + j + Sum(for k in {100} return i + j + k)
              )
            )]`;
        tcNotValid(invalid, "COLLCoverage");
        const valid = `Policy.riskItems[1 > Sum(for i in {1} return
              i + Sum(for j in {10} return
                i + j + Sum(Policy.riskItems[*].modelYear)
              )
            )]`;
        tcValid(valid, "COLLCoverage");
    });
    it("should validate partial iteration expression", () => {
        tcValid("for i", "Policy");
        tcValid("for i in ", "Policy");
        tcValid("for i in riskItems", "Policy");
        tcValid("for i in riskItems return ", "Policy");
        tcValid("for i in riskItems return ''", "Policy");
        tcValid("some i", "Policy");
        tcValid("some i in ", "Policy");
        tcValid("some i in riskItems", "Policy");
        tcValid("some i in riskItems satisfies ", "Policy");
    });
    it("should validate negative decimal literal access by index", () => {
        tcNotValid("policies[-1]", "Policy");
        tcValid("policies[1]", "Policy");
    });
    it("in", () => {
        tcNotValid("Vehicle.collCoverages[0] in Vehicle.collCoverages[0]", "COLLCoverage");
        tcNotValid("Vehicle.collCoverages in Vehicle.collCoverages[0]", "COLLCoverage");
        tcNotValid("Vehicle.collCoverages[0] in COLLCoverage", "COLLCoverage");
    });
    it("CCR", () => {
        // AnubisCoverage is not child of any context and it is only used as a field type,
        // therefore it cannot be used as a CCR
        tcNotValid("AnubisCoverage.limitAmount", "Vehicle");
    });
    it("equality", () => {
        tcNotValid("Policy = addressInfo", "Insured");
        tcNotValid("Policy == addressInfo", "Insured");
    });
    it("unary", () => {
        tcNotValid("-Policy", "Insured");
        tcNotValid("-addressInfo", "Insured");
    });
    it("math", () => {
        tcNotValid("Policy - addressInfo", "Insured");
        tcNotValid("Policy + addressInfo", "Insured");
        tcNotValid("Policy / addressInfo", "Insured");
        tcNotValid("Policy * addressInfo", "Insured");
        tcNotValid("Policy % addressInfo", "Insured");
        tcNotValid("Policy ** addressInfo", "Insured");
    });
    it("logical", () => {
        tcNotValid("Policy in addressInfo", "Insured");
        tcNotValid("Policy > addressInfo", "Insured");
        tcNotValid("Policy >= addressInfo", "Insured");
        tcNotValid("Policy < addressInfo", "Insured");
        tcNotValid("Policy <= addressInfo", "Insured");
    });
    it("propositional", () => {
        tcNotValid("Policy and addressInfo", "Insured");
        tcNotValid("Policy && addressInfo", "Insured");
        tcNotValid("Policy or addressInfo", "Insured");
        tcNotValid("Policy || addressInfo", "Insured");
    });
    it("if", () => {
        tcNotValid("if(true) then Policy else addressInfo", "Insured");
    });
    it("dynamic context", () => {
        tcValid("context.externalData.limitAmount > cardCreditLimitAmount", "CreditCardInfo");
        tcValid("context.externalData.limitAmount > limitAmount", "COLLCoverage");
        // tslint:disable-next-line: max-line-length
        tcValid("some limit in context.externalData.limitAmounts satisfies limit > limitAmount && limit == CreditCardInfo.cardCreditLimitAmount", "COLLCoverage");
        // tslint:disable-next-line: max-line-length
        tcValid("some limit in context.externalData[*].coverages[*].limitAmount satisfies limit > limitAmount && limit == CreditCardInfo.cardCreditLimitAmount", "COLLCoverage");
        tcValid("context.externalData.value - limitAmount", "COLLCoverage");
        tcValid("context.externalData.value + limitAmount", "COLLCoverage");
        tcValid("context.externalData.value / limitAmount", "COLLCoverage");
        tcValid("context.externalData.value * limitAmount", "COLLCoverage");
        tcValid("context.externalData.value % limitAmount", "COLLCoverage");
        tcValid("context.externalData.value ** limitAmount", "COLLCoverage");
        tcValid("limitAmount in context.externalData.values", "COLLCoverage");
        tcValid("context.externalData.value < limitAmount", "COLLCoverage");
        tcValid("context.externalData.value <= limitAmount", "COLLCoverage");
        tcValid("context.externalData.value >= limitAmount", "COLLCoverage");
        tcValid("context.externalData.value > limitAmount", "COLLCoverage");
        tcValid("-context.externalData.value", "COLLCoverage");
        tcValid("context.externalData.value and haveChildren", "Insured");
        tcValid("context.externalData.value or haveChildren", "Insured");
        tcValid("!context.externalData.value", "Insured");
    });
});

function findScope(contextDefinition: string): Scope {
    const i = instance;
    const scope = Object.values(i).find(s => s.type.name === contextDefinition);
    if (!scope) throw new Error(`Cannot find scope for a context definition ${contextDefinition}`);
    return scope;
}

function findErrors(expression: string, contextDefinition: string): any[] {
    const scope = findScope(contextDefinition);
    const parser = createParser(expression);
    const tree = parser.expression();
    const traversingVisitor = new KelTraversingVisitor(scope);
    const node = traversingVisitor.visit(tree);
    const visitor = new ValidatingNodeVisitor();
    visitor.visit(node);
    const errors = visitor.getErrors();
    return errors;
}

function tcValid(expression: string, contextDefinition: string): void | never {
    const errors = findErrors(expression, contextDefinition);
    expect(`'${expression}' has 0 errors`).toBe(`'${expression}' has ${errors.length} errors`);
}

function tcWithSyntaxErrorCount(expression: string, contextDefinition: string, errorCount: number): void | never {
    const errors = findErrors(expression, contextDefinition);
    expect(`'${expression}' has ${errorCount} errors`).toBe(`'${expression}' has ${errors.length} errors`);
}

function tcNotValid(expression: string, contextDefinition: string): void | never {
    const errors = findErrors(expression, contextDefinition);
    // tslint:disable-next-line: max-line-length
    let match = "";
    if (errors.length) {
        match = `'${expression}' has errors`;
    } else {
        match = `'${expression}' has no errors`;
    }
    expect(`'${expression}' has errors`).toBe(match);
}
