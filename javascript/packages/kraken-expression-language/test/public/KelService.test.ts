import { KelService } from "../../src";
import { PrimitiveTypeName } from "../../src/type/PrimitiveTypeName";
import { instance } from "../test-data/test-data";

describe("KelService", () => {
    it("should provide completion", () => {
        const service = new KelService(instance.policy);
        const completion = service.provideCompletion("Policy.", { line: 1, column: 7 });
        expect(completion.completions).toMatchSnapshot();
        expect(completion.completions.filter(c => c.type === "function")).toHaveLength(0);
    });
    it("should provide no completion", () => {
        const service = new KelService(instance.policy);
        const completion = service.provideCompletion("a.b", { line: 1, column: 17 });
        expect(completion.completions).toHaveLength(0);
    });
    it("should provide validation with semantic errors", () => {
        const service = new KelService(instance.policy);
        const validation = service.provideValidation("Policy.none");
        expect(validation.semantic).toHaveLength(1);
    });
    it("should provide validation with syntax errors", () => {
        const service = new KelService(instance.policy);
        const validation = service.provideValidation("Policy.");
        expect(validation.syntax).toHaveLength(1);
    });
    it("should check expression type", () => {
        const service = new KelService(instance.policy);
        const validation = service.validateReturnType("Policy", "Boolean");
        expect(validation).toBeDefined();
        expect(validation?.error).toBe("Expected return type is 'Boolean', actual is 'Policy'");
    });
    it("should check all types of expressions", () => {
        function hasNoError(e: string, typeName: PrimitiveTypeName): void {
            const service = new KelService(instance.creditCardInfo);
            const validation = service.validateReturnType(e, typeName);
            expect(validation?.error).toBeUndefined();
        }
        hasNoError("cardType", "String");
        hasNoError("cardCreditLimitAmount", "Money");
        hasNoError("cardCreditLimitAmount", "Number");
        hasNoError("cvv", "Money");
        hasNoError("cvv", "Number");
        hasNoError("expirationDate", "Date");
    });
});
