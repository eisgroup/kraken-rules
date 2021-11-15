import { ScopeDeserializer } from "../../src";
import { Type } from "../../src/type/Type";
import { json } from "../test-data/test-data";

describe("ScopeDeserializer", () => {
    it("should deserialize scope", () => {
        const sd = new ScopeDeserializer(json.typeRegistry);
        const scope = sd.provideScope(json.scope.policy);
        expect(scope).toBeDefined();
        expect(scope.name).toBe("Policy:GLOBAL->Policy");
        expect(scope.type.name).toBe("Policy");
        expect(scope.type).toBeInstanceOf(Type);
    });
});
